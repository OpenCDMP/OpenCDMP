import { Component, DestroyRef, inject, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { PluginDataType } from '@app/core/common/enum/plugin-data-type';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { TemplateValidationIssue, TemplateValidationIssueLevel } from '@app/core/model/file/template-validation-result.model';
import { ConfigurationField, PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { FileTransformerHttpService } from '@app/core/services/file-transformer/file-transformer.http.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { PluginConfigurationEditorResolver } from '@app/ui/admin/tenant-configuration/models/plugin-configuration/plugin-configuration-editor.resolver';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { TcExportSettingsComponent } from '../../../tenant-configuration/sections/integrations/export-settings/tc-export-settings.component';
import { PluginEditorComponent } from '../../../../plugin/plugin-editor.component';
import { TranslatePipe } from '@ngx-translate/core';

interface ExportPlugin { code: string, form: UntypedFormGroup, definitions: ConfigurationField[], grouped: boolean }

/** The export template card: the installed export plugin's settings, folded to a one-line summary until asked for. */
@Component({
    selector: 'app-se2-export-card',
    templateUrl: './export-card.component.html',
    styleUrls: ['./export-card.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatIcon, TcExportSettingsComponent, PluginEditorComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class ExportCardComponent implements OnInit {
	@Input() pluginsForm: UntypedFormArray;
	@Input() pluginConfiguration: PluginRepositoryConfiguration[];
	/** The export settings stay folded until an admin asks for them, unless something is already customized. */
	open = false;
	/** The organisation's plugin settings, by plugin and field code: what an empty setting here follows. */
	tenantValues: Record<string, Record<string, string | null>> = {};
	/** What the plugin found in each saved template file of the read-only view, by plugin and field code. */
	templateIssues: Record<string, TemplateValidationIssue[]> = {};
	issueLevels = TemplateValidationIssueLevel;

	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly fileTransformerHttpService = inject(FileTransformerHttpService);
	private readonly destroyRef = inject(DestroyRef);

	constructor(public editor: StructureEditorBase<any, any>) { }

	ngOnInit(): void {
		this.open = this.customizedCount > 0;
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.PluginConfiguration, PluginConfigurationEditorResolver.lookupFields())
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: record => {
					const values: Record<string, Record<string, string | null>> = {};
					record?.pluginConfiguration?.pluginConfigurations?.forEach(plugin => {
						values[plugin.pluginCode] = Object.fromEntries((plugin.fields ?? []).map(x => [x.code, x.textValue ?? null]));
					});
					this.tenantValues = values;
				},
				error: () => this.tenantValues = {},
			});
		// the editable view checks templates in the settings editor; a finalized one still shows why exports fall back
		if (this.editor.readOnly) this.validateTemplates();
	}

	private validateTemplates(): void {
		this.availableControls.forEach(plugin => {
			const code: string = plugin.get('pluginCode')?.value ?? '';
			const definitions = this.definitionsOf(code);
			(plugin.get('fields') as UntypedFormArray)?.controls?.forEach(field => {
				const fieldCode: string = field.get('code')?.value ?? '';
				const definition = definitions.find(x => x.code === fieldCode);
				const fileId = field.get('fileValue')?.value;
				if (!fileId || definition?.type !== PluginDataType.File || !fieldCode.includes('template')) return;
				// same entity type rule as the settings editor
				const entityType = definition.appliesTo?.includes(PluginEntityType.Description) && !definition.appliesTo?.includes(PluginEntityType.Plan) ? PluginEntityType.Description : PluginEntityType.Plan;
				this.fileTransformerHttpService.validateTemplate(fileId, code, entityType)
					.pipe(takeUntilDestroyed(this.destroyRef))
					.subscribe({
						next: result => { if (result?.issues?.length) this.templateIssues = { ...this.templateIssues, [`${code}.${fieldCode}`]: result.issues }; },
						// advisory only: a plugin that cannot validate leaves the summary as it is
						error: () => { },
					});
			});
		});
	}

	t(key: string): string { return this.editor.term(key); }

	private definitionsOf(code: string): ConfigurationField[] { return this.pluginConfiguration?.find(x => x.repositoryId === code)?.fields ?? []; }

	/**
	 * The saved plugin entries whose plugin the organisation still offers. Settings of a plugin switched off in
	 * Plugins stay saved, but are not shown: without the plugin they have no labels and cannot be used.
	 */
	private get availableControls(): UntypedFormGroup[] {
		return ((this.pluginsForm?.controls ?? []) as UntypedFormGroup[]).filter(control => this.definitionsOf(control.get('pluginCode')?.value ?? '').length > 0);
	}

	get hasPlugins(): boolean { return this.availableControls.length > 0; }

	/** The plugin entries, each with the settings its plugin declares; plugins that group their settings get the organisation-style editor. */
	get plugins(): ExportPlugin[] {
		return this.availableControls.map(control => {
			const code: string = control.get('pluginCode')?.value ?? '';
			const definitions = this.definitionsOf(code);
			return { code, form: control as UntypedFormGroup, definitions, grouped: definitions.some(x => !!x.group) };
		});
	}

	/** Plugin settings that hold a value: an uploaded file or a typed value. */
	get customizedCount(): number {
		let count = 0;
		this.availableControls.forEach(plugin => (plugin.get('fields') as UntypedFormArray)?.controls?.forEach(field => {
			if (field.get('fileValue')?.value || (field.get('textValue')?.value ?? '') !== '') count++;
		}));
		return count;
	}

	/** Export settings that hold a value, as label and value, for the read-only view. */
	get settings(): { label: string, value: string, color: boolean, issues?: TemplateValidationIssue[] }[] {
		const result: { label: string, value: string, color: boolean, issues?: TemplateValidationIssue[] }[] = [];
		this.availableControls.forEach(plugin => {
			const code: string = plugin.get('pluginCode')?.value ?? '';
			const definitions = this.definitionsOf(code);
			(plugin.get('fields') as UntypedFormArray)?.controls?.forEach(field => {
				const fieldCode: string = field.get('code')?.value ?? '';
				const definition = definitions.find(x => x.code === fieldCode);
				const key = `PLUGIN-EDITOR.ADMIN-CONFIGURATION.${code.toUpperCase()}.${fieldCode.toUpperCase()}`;
				const translated = this.editor.language.instant(key);
				const label = translated !== key ? translated : (definition?.label ?? fieldCode);
				const fileId = field.get('fileValue')?.value;
				const text = field.get('textValue')?.value;
				// select values read as their option labels ("Question above answer", not "stacked")
				const shown = definition?.options?.find(x => x.value === text)?.label ?? text;
				if (fileId) result.push({ label, value: this.editor.fileMap.get(fileId)?.name ?? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.READ.FILE-UPLOADED'), color: false, issues: this.templateIssues[`${code}.${fieldCode}`] });
				else if (text !== null && text !== undefined && text !== '') result.push({ label, value: String(shown), color: /^#[0-9a-f]{3,8}$/i.test(String(text)) });
			});
		});
		return result;
	}
}
