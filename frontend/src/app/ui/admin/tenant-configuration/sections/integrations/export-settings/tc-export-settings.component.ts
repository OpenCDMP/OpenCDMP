import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginDataType } from '@app/core/common/enum/plugin-data-type';
import { ConfigurationField, PluginConfigurationFieldPersist } from '@app/core/model/plugin-configuration/plugin-configuration';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { TemplateValidationIssueLevel, TemplateValidationResult } from '@app/core/model/file/template-validation-result.model';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { FileTransformerHttpService } from '@app/core/services/file-transformer/file-transformer.http.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import FileSaver from 'file-saver';
import { catchError, map, of, startWith, switchMap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TcKeyValueFieldComponent } from './tc-key-value-field.component';
import { TcOption, TcOrderedListFieldComponent } from './tc-ordered-list-field.component';
import { TcReferenceFieldsFieldComponent } from './tc-reference-fields-field.component';
import { TcSettingsPreviewComponent } from './tc-settings-preview.component';

interface TcSettingGroup {
	name: string;
	fields: ConfigurationField[];
}

/**
 * The settings of a plugin that describes them in groups (the DOCX/PDF exporter, for one): a menu of groups, the
 * group's settings with an editor suited to each, and a live preview. Edits the plugin's entry of the tenant's
 * plugin configuration form in place, so the section's save bar saves it.
 */
@Component({
	selector: 'app-tc-export-settings',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, FileUploadComponent, TcOrderedListFieldComponent, TcKeyValueFieldComponent, TcReferenceFieldsFieldComponent, TcSettingsPreviewComponent],
	templateUrl: './tc-export-settings.component.html',
	styleUrl: './tc-export-settings.component.scss',
})
export class TcExportSettingsComponent {
	readonly pluginCode = input.required<string>();
	/** The plugin's entry of the plugin configuration form: `{ pluginCode, pluginType, fields: [...] }`. */
	readonly pluginForm = input.required<UntypedFormGroup>();
	readonly definitions = input<ConfigurationField[]>([]);
	readonly fileMap = input(new Map<Guid, StorageFile>());
	readonly readOnly = input(false);
	/** Whether the plugin can render a preview (it offers PDF). */
	readonly canPreview = input(false);
	/**
	 * Values an empty setting falls back to before the plugin default, by code: the organisation's settings when
	 * editing a blueprint's. Empty when editing the organisation's own.
	 */
	readonly inheritedValues = input<Record<string, string | null>>({});

	protected readonly types = PluginDataType;
	protected readonly issueLevels = TemplateValidationIssueLevel;
	protected readonly downloading = signal<string | null>(null);
	/** What the plugin found in each template file, by field code; only results with issues are kept. */
	protected readonly validations = signal<Record<string, TemplateValidationResult>>({});
	/** The file each template field was last validated for, so a file is checked once, not on every edit. */
	private readonly validatedFiles: Record<string, Guid | null> = {};

	private readonly http = inject(BaseHttpV2Service);
	private readonly configurationService = inject(ConfigurationService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly fileTransformerHttpService = inject(FileTransformerHttpService);

	protected readonly groups = computed<TcSettingGroup[]>(() => {
		const groups: TcSettingGroup[] = [];
		for (const field of this.definitions()) {
			const name = field.group || '';
			let group = groups.find(x => x.name === name);
			if (!group) groups.push(group = { name, fields: [] });
			group.fields.push(field);
		}
		return groups;
	});
	protected readonly selected = signal<string | null>(null);
	protected readonly group = computed(() => this.groups().find(x => x.name === this.selected()) ?? this.groups()[0]);

	/** The live values of the form, so counts and the preview follow every edit. */
	private readonly formValue = toSignal(toObservable(this.pluginForm).pipe(switchMap(form => form ? form.valueChanges.pipe(startWith(form.getRawValue()), map(() => form.getRawValue())) : of(null))));
	protected readonly previewFields = computed<PluginConfigurationFieldPersist[]>(() =>
		(this.formValue()?.fields ?? []).map((x: PluginConfigurationFieldPersist) => ({ code: x.code, textValue: x.textValue ?? null, fileValue: x.fileValue ?? null })));

	constructor() {
		// checks each template file when it is shown, uploaded or replaced, including one saved earlier
		effect(() => {
			const fields: PluginConfigurationFieldPersist[] = this.formValue()?.fields ?? [];
			const definitions = this.definitions();
			untracked(() => {
				for (const definition of definitions) {
					if (!this.isTemplateField(definition)) continue;
					const fileId = fields.find(x => x.code === definition.code)?.fileValue ?? null;
					if (this.validatedFiles[definition.code] === fileId) continue;
					this.validatedFiles[definition.code] = fileId;
					this.validateTemplate(definition, fileId);
				}
			});
		});
	}

	protected readonly referenceTypes = toSignal(inject(ReferenceTypeService).query(this.referenceTypeLookup()).pipe(
		map(x => (x.items ?? []).sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''))), catchError(() => of([] as ReferenceType[]))), { initialValue: [] as ReferenceType[] });

	protected changedCount(group: TcSettingGroup): number {
		this.formValue();
		return group.fields.filter(x => this.isChanged(x)).length;
	}

	protected fieldGroup(code: string): UntypedFormGroup | null {
		const fields = this.pluginForm()?.get('fields') as UntypedFormArray;
		return (fields?.controls.find(x => x.get('code')?.value === code) as UntypedFormGroup) ?? null;
	}

	protected text(code: string): FormControl<string | null> {
		return this.fieldGroup(code)?.get('textValue') as FormControl<string | null>;
	}

	protected file(code: string): FormControl<Guid | null> {
		return this.fieldGroup(code)?.get('fileValue') as FormControl<Guid | null>;
	}

	protected isChanged(field: ConfigurationField): boolean {
		const group = this.fieldGroup(field.code);
		if (field.authInfo && this.secretState(field) === 'stored') return true;
		return !!group && (!!group.get('fileValue')?.value || !!(group.get('textValue')?.value ?? '').toString().trim());
	}

	protected reset(field: ConfigurationField): void {
		const group = this.fieldGroup(field.code);
		if (!group) return;
		if (field.authInfo) { this.removeSecret(field); return; }
		group.markAsDirty();
		group.patchValue({ textValue: null, fileValue: null });
	}

	// a stored secret never reaches the browser: textValue null keeps it, '' removes it, anything else replaces it
	private readonly replacing = signal<ReadonlySet<string>>(new Set());

	protected secretState(field: ConfigurationField): 'stored' | 'removed' | 'editing' {
		this.formValue();
		const group = this.fieldGroup(field.code);
		if (!group?.get('secretSet')?.value || this.replacing().has(field.code)) return 'editing';
		return group.get('textValue')?.value === '' ? 'removed' : 'stored';
	}

	protected replaceSecret(field: ConfigurationField): void {
		this.replacing.set(new Set([...this.replacing(), field.code]));
		this.setText(field.code, '');
	}

	protected removeSecret(field: ConfigurationField): void {
		this.replacing.set(new Set([...this.replacing()].filter(x => x !== field.code)));
		this.setText(field.code, this.fieldGroup(field.code)?.get('secretSet')?.value ? '' : null);
	}

	protected keepSecret(field: ConfigurationField): void {
		this.replacing.set(new Set([...this.replacing()].filter(x => x !== field.code)));
		this.setText(field.code, null);
	}

	protected setText(code: string, value: string | null): void {
		const control = this.text(code);
		if (!control) return;
		control.markAsDirty();
		control.setValue(value);
	}

	/** Two options meaning on and off are a switch; anything else a list of choices. */
	protected isSwitch(field: ConfigurationField): boolean {
		const values = (field.options ?? []).map(x => x.value).sort().join();
		return values === 'false,true';
	}

	protected effective(field: ConfigurationField): string | null {
		const own = this.text(field.code)?.value;
		return own != null && own !== '' ? own : this.fallback(field);
	}

	/** What applies while the setting is empty. */
	protected fallback(field: ConfigurationField): string | null {
		const inherited = this.inheritedValues()[field.code];
		return inherited != null && inherited.trim() !== '' ? inherited : field.defaultValue ?? null;
	}

	protected isInherited(field: ConfigurationField): boolean {
		const inherited = this.inheritedValues()[field.code];
		return inherited != null && inherited.trim() !== '';
	}

	protected optionLabel(field: ConfigurationField, value: string | null): string | null {
		if (value == null) return null;
		return field.options?.find(x => x.value === value)?.label ?? value;
	}

	/** Options, plus one per reference type when the plugin asks for them. */
	protected options(field: ConfigurationField): TcOption[] {
		const options: TcOption[] = (field.options ?? []).map(x => ({ value: x.value, label: x.label }));
		if (field.optionsSource === 'referenceTypes') {
			for (const type of this.referenceTypes()) {
				if (type.code && !options.some(x => x.value.toLowerCase() === type.code.toLowerCase())) options.push({ value: type.code, label: type.name ?? type.code });
			}
		}
		return options;
	}

	protected isTemplateField(field: ConfigurationField): boolean {
		return field.type === PluginDataType.File && field.code.includes('template');
	}

	private entityType(field: ConfigurationField): PluginEntityType {
		return field.appliesTo?.includes(PluginEntityType.Description) && !field.appliesTo?.includes(PluginEntityType.Plan) ? PluginEntityType.Description : PluginEntityType.Plan;
	}

	/** Asks the plugin to check a template file. Advisory: the file is saved whatever the outcome. */
	private validateTemplate(field: ConfigurationField, fileId: Guid | null): void {
		this.validations.update(x => { const { [field.code]: _, ...rest } = x; return rest; });
		if (!fileId) return;
		this.fileTransformerHttpService.validateTemplate(fileId, this.pluginCode(), this.entityType(field))
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: result => {
					// a newer file may have been chosen while this one was being checked
					if (this.validatedFiles[field.code] !== fileId || !result?.issues?.length) return;
					this.validations.update(x => ({ ...x, [field.code]: result }));
				},
				// a plugin that cannot validate must not block the upload flow
				error: () => { },
			});
	}

	/** The plugin's built-in template for this field, as a starting point for a custom one. */
	protected downloadDefaultTemplate(field: ConfigurationField): void {
		const entityType = this.entityType(field) === PluginEntityType.Description ? 'Description' : 'Plan';
		this.downloading.set(field.code);
		this.http.get<Blob>(`${this.configurationService.server}file-transformer/default-template/${encodeURIComponent(this.pluginCode())}`, { params: { entityType }, responseType: 'blob' })
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: blob => {
					this.downloading.set(null);
					FileSaver.saveAs(blob, `${this.pluginCode()}-${entityType === 'Plan' ? 'plan' : 'asset'}-template.docx`);
				},
				error: (error: HttpErrorResponse) => {
					this.downloading.set(null);
					const key = error.status === 404 ? 'TENANT-CONFIGURATION.EXPORT.NO-DEFAULT-TEMPLATE' : 'TENANT-CONFIGURATION.EXPORT.DEFAULT-TEMPLATE-FAILED';
					this.uiNotificationService.snackBarNotification(this.language.instant(key), SnackBarNotificationLevel.Warning);
				},
			});
	}

	private referenceTypeLookup(): ReferenceTypeLookup {
		const lookup = new ReferenceTypeLookup();
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 500 };
		// the backend refuses a paged query without an order
		lookup.order = { items: [nameof<ReferenceType>(x => x.name)] };
		lookup.project = {
			fields: [
				nameof<ReferenceType>(x => x.id),
				nameof<ReferenceType>(x => x.name),
				nameof<ReferenceType>(x => x.code),
				[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
				[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
			],
		};
		return lookup;
	}
}
