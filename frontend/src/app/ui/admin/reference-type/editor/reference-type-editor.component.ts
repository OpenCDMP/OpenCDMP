import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormArray, UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { ReferenceFieldDataType } from '@app/core/common/enum/reference-field-data-type';
import { ExternalFetcherBaseSourceConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceType, ReferenceTypeDefinitionPersist, ReferenceTypePersist } from '@app/core/model/reference-type/reference-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { StaticEditorModel, StaticOptionEditorModel } from '@app/ui/external-fetcher/external-fetcher-source-editor.model';
import { fromXmlString, hasEnabledAuth, toXmlString, withoutAuthRequestBodies } from '@app/ui/external-fetcher/external-fetcher-xml.util';
import { ImportXmlDialogComponent, ImportXmlDialogData } from '@app/ui/external-fetcher/import-xml-dialog/import-xml.dialog.component';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ReferenceTypeDefinitionEditorModel, ReferenceTypeEditorModel } from './reference-type-editor.model';
import { ReferenceTypeEditorResolver } from './reference-type-editor.resolver';
import { ReferenceTypeEditorService } from './reference-type-editor.service';
import { ReferenceTypeTestDialogComponent } from '../reference-type-test-dialog/reference-type-test-dialog.component';
import { Rt2AddSourceDialogComponent, Rt2AddSourceResult } from './dialogs/add-source/add-source.dialog';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { TenantReadonlyBannerComponent } from '../../../../../common/ui/tenant-shared/tenant-readonly-banner.component';
import { MatFormField, MatLabel, MatError, MatHint } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MultipleAutoCompleteComponent } from '../../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { Rt2SourceEditorComponent } from '../../../external-fetcher/source-editor/source-editor.component';
import { MatRadioGroup, MatRadioButton } from '@angular/material/radio';
import { ReferenceEntriesComponent } from './entries/reference-entries.component';

export type Rt2Tab = 'fields' | 'sources' | 'manual' | 'entries';

// Every entry has these, so every source maps them; the type's own fields come after.
export const RT2_SYSTEM_CODES = ['reference_id', 'label', 'description'];

@Component({
    selector: 'app-reference-type-editor',
    templateUrl: './reference-type-editor.component.html',
    styleUrls: ['./reference-type-editor.component.scss'],
    providers: [ReferenceTypeEditorService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, MatButton, MatIconButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, TenantReadonlyBannerComponent, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, MatHint, MatSelect, MatOption, MatSlideToggle, ReactiveAsteriskDirective, MultipleAutoCompleteComponent, Rt2SourceEditorComponent, MatRadioGroup, MatRadioButton, ReferenceEntriesComponent, TranslatePipe]
})
export class ReferenceTypeEditorComponent extends BaseEditor<ReferenceTypeEditorModel, ReferenceType> implements OnInit {

	formGroup: UntypedFormGroup = null;
	tab: Rt2Tab = 'fields';
	selectedSourceIndex = 0;
	expandedFieldIndex: number = null;
	attemptedSave = false;
	referenceTypes: ReferenceType[] = [];
	sourceKeysMap: Map<Guid, string[]> = new Map<Guid, string[]>();

	readonly sourceType = ExternalFetcherSourceType;
	readonly dataTypes = this.enumUtils.getEnumValues<ReferenceFieldDataType>(ReferenceFieldDataType);
	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	// field codes as they were at the last sync, by position, so a rename carries its mappings along
	private syncedCodes: string[] = [];

	get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(AppPermission.DeleteReferenceType) && this.editorModel.belongsToCurrentTenant != false;
	}

	get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(AppPermission.EditReferenceType) && this.editorModel.belongsToCurrentTenant != false;
	}

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
	}

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected router: Router,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected filterService: FilterService,
		protected route: ActivatedRoute,
		protected queryParamsService: QueryParamsService,
		protected lockService: LockService,
		protected authService: AuthService,
		protected configurationService: ConfigurationService,
		public enumUtils: EnumUtils,
		public referenceTypeService: ReferenceTypeService,
		private logger: LoggingService,
		private referenceTypeEditorService: ReferenceTypeEditorService,
		protected routerUtils: RouterUtilsService,
		public semanticsService: SemanticsService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
		if (this.route.snapshot.queryParamMap.get('tab') === 'entries') this.tab = 'entries';
	}

	// ---------------------------------------------------------------- data

	getItem(itemId: Guid, successFunction: (item: ReferenceType) => void) {
		this.referenceTypeService.getSingle(itemId, ReferenceTypeEditorResolver.lookupFields())
			.pipe(map(data => data as ReferenceType), takeUntil(this._destroyed))
			.subscribe({ next: data => successFunction(data), error: error => this.onCallbackError(error) });
	}

	prepareForm(data: ReferenceType) {
		try {
			this.editorModel = data ? new ReferenceTypeEditorModel().fromModel(data) : new ReferenceTypeEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.getReferenceTypes(this.editorModel.id);
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse referenceType item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditReferenceType));
		this.referenceTypeEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		this.syncedCodes = this.fieldsArray.getRawValue().map(x => x.code);
		if (this.selectedSourceIndex >= this.sourcesArray.length) this.selectedSourceIndex = 0;
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: ReferenceType) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
		if (this.isNew && id) {
			this.router.navigate([this.routerUtils.generateUrl(`/reference-type/${id}`)], { replaceUrl: true });
			return;
		}
		this.refreshData();
	}

	cancel(): void {
		this.router.navigate([this.routerUtils.generateUrl('/reference-type')]);
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.getRawValue()) as ReferenceTypePersist;
		this.enqueueService.enqueueExhaustChannel(this.referenceTypeService.persist(formData, [nameof<ReferenceType>(x => x.id)]))
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error: error => this.onCallbackError(error)
			});
	}

	formSubmit(): void {
		this.attemptedSave = true;
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			this.tab = this.fieldsArray.invalid ? 'fields' : (this.sourcesArray.invalid || this.sourcesArray.length === 0 ? 'sources' : this.tab);
			return;
		}
		this.persistEntity();
	}

	delete() {
		const value = this.formGroup.value;
		if (!value.id) return;
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '300px',
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			}
		}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (!result) return;
			this.enqueueService.enqueueExhaustChannel(this.referenceTypeService.delete(value.id)).pipe(takeUntil(this._destroyed))
				.subscribe({ next: () => { this.formGroup.markAsPristine(); this.onCallbackDeleteSuccess(); }, error: error => this.onCallbackError(error) });
		});
	}

	private getReferenceTypes(excludedId?: Guid): void {
		const lookup = ReferenceTypeService.DefaultReferenceTypeLookup([IsActive.Active]);
		if (excludedId) lookup.excludedIds = [excludedId];
		this.referenceTypeService.query(lookup).pipe(takeUntil(this._destroyed)).subscribe({
			next: response => {
				this.referenceTypes = response.items as ReferenceType[];
				this.referenceTypes.forEach(x => this.sourceKeysMap.set(x.id, x.definition?.sources?.map(s => s.key) ?? []));
			},
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	// ---------------------------------------------------------------- shape

	get definition(): UntypedFormGroup { return this.formGroup.get('definition') as UntypedFormGroup; }
	get fieldsArray(): UntypedFormArray { return this.definition.get('fields') as UntypedFormArray; }
	get sourcesArray(): UntypedFormArray { return this.definition.get('sources') as UntypedFormArray; }
	get selectedSource(): UntypedFormGroup { return this.sourcesArray.at(this.selectedSourceIndex) as UntypedFormGroup; }

	/** System codes first, then the type's own field codes: the columns every source fills. */
	get allCodes(): string[] { return [...RT2_SYSTEM_CODES, ...this.fieldsArray.getRawValue().map(x => x.code).filter(x => !!x)]; }

	isApi(source: UntypedFormGroup): boolean { return source.get('type').value !== ExternalFetcherSourceType.STATIC; }

	/** How many sources fill this code: API sources with a response path, fixed lists with a value in any entry. */
	mappedIn(code: string): number {
		return this.sourcesArray.controls.filter(source => this.isApi(source as UntypedFormGroup)
			? (source.get('results').get('fieldsMapping') as UntypedFormArray).getRawValue().some(m => m.code === code && !!m.responsePath)
			: (source.get('items') as UntypedFormArray).getRawValue().some(item => item.options?.some(o => o.code === code && !!o.value))
		).length;
	}

	get unmappedFields(): { label: string, sources: string[] }[] {
		return this.fieldsArray.getRawValue()
			.filter(f => !!f.code)
			.map(f => ({
				label: f.label || f.code,
				sources: this.sourcesArray.controls.filter(source => this.isApi(source as UntypedFormGroup)
					&& !(source.get('results').get('fieldsMapping') as UntypedFormArray).getRawValue().some(m => m.code === f.code && !!m.responsePath))
					.map(source => source.get('label').value || source.get('key').value)
			}))
			.filter(x => x.sources.length > 0);
	}

	setTab(tab: Rt2Tab): void { this.tab = tab; }

	// ---------------------------------------------------------------- fields

	addField(): void {
		const field = this.editorModel.createChildField(this.fieldsArray.length);
		field.patchValue({ dataType: ReferenceFieldDataType.Text, required: false });
		this.fieldsArray.push(field);
		this.expandedFieldIndex = this.fieldsArray.length - 1;
		this.syncFieldCodes();
	}

	removeField(index: number): void {
		this.fieldsArray.removeAt(index);
		ReferenceTypeEditorModel.reApplyDefinitionFieldsValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
		this.fieldsArray.markAsDirty();
		this.expandedFieldIndex = null;
		this.syncFieldCodes();
	}

	toggleField(index: number): void { this.expandedFieldIndex = this.expandedFieldIndex === index ? null : index; }

	/**
	 * Keeps every source's mapping rows and fixed-list columns in step with the fields, replacing the old editor's
	 * "Update fields" button. A code changed in place keeps its mapping; removed codes lose theirs.
	 */
	syncFieldCodes(): void {
		const codes = this.fieldsArray.getRawValue().map(x => x.code as string);
		const renames = new Map<string, string>();
		if (codes.length === this.syncedCodes.length) {
			codes.forEach((code, i) => { if (this.syncedCodes[i] && code && this.syncedCodes[i] !== code) renames.set(this.syncedCodes[i], code); });
		}
		this.syncedCodes = codes;
		const wanted = [...RT2_SYSTEM_CODES, ...codes.filter(x => !!x)];
		this.sourcesArray.controls.forEach((source, sourceIndex) => {
			this.syncRows(source.get('results').get('fieldsMapping') as UntypedFormArray, wanted, renames, () => this.editorModel.createFieldsMapping(sourceIndex, 0));
			(source.get('items') as UntypedFormArray).controls.forEach((item, itemIndex) =>
				this.syncRows(item.get('options') as UntypedFormArray, wanted, renames, () => new StaticOptionEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath: `definition.sources[${sourceIndex}].items[${itemIndex}].options[0].` })));
		});
		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
	}

	private syncRows(rows: UntypedFormArray, wanted: string[], renames: Map<string, string>, create: () => UntypedFormGroup): void {
		rows.controls.forEach(row => { const renamed = renames.get(row.get('code').value); if (renamed) row.get('code').setValue(renamed); });
		for (let i = rows.length - 1; i >= 0; i--) {
			if (!wanted.includes(rows.at(i).get('code').value)) rows.removeAt(i);
		}
		wanted.forEach(code => {
			if (rows.controls.some(row => row.get('code').value === code)) return;
			const row = create();
			row.get('code').setValue(code);
			rows.push(row);
		});
	}

	// ---------------------------------------------------------------- sources

	selectSource(index: number): void { this.selectedSourceIndex = index; }

	openAddSource(): void {
		this.dialog.open(Rt2AddSourceDialogComponent, { width: 'min(760px, 92vw)', data: { typeName: this.formGroup.get('name').value || this.language.instant('REFERENCE-TYPE-EDITOR.FIELDS.THIS-TYPE'), existingKeys: this.sourcesArray.getRawValue().map(x => x.key) } })
			.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Rt2AddSourceResult) => {
				if (result === 'import') this.openImportXmlDialog();
				else if (result) this.addSource(result);
			});
	}

	addSource(params: { type: ExternalFetcherSourceType, label: string, key: string, url?: string }): void {
		const index = this.sourcesArray.length;
		const source = this.editorModel.createChildSource(index);
		source.patchValue({ type: params.type, label: params.label, key: params.key, url: params.url ?? null });
		this.sourcesArray.push(source);
		if (params.type === ExternalFetcherSourceType.STATIC) this.addStaticItem(index);
		this.syncFieldCodes();
		this.sourcesArray.markAsDirty();
		this.selectedSourceIndex = index;
		this.tab = 'sources';
	}

	removeSource(index: number): void {
		this.sourcesArray.removeAt(index);
		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
		this.sourcesArray.controls.forEach((source, i) => source.get('ordinal').setValue(i + 1));
		this.sourcesArray.markAsDirty();
		this.selectedSourceIndex = Math.max(0, Math.min(this.selectedSourceIndex, this.sourcesArray.length - 1));
	}

	addStaticItem(sourceIndex: number): void {
		const items = this.sourcesArray.at(sourceIndex).get('items') as UntypedFormArray;
		items.push(new StaticEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath: `definition.sources[${sourceIndex}].items[${items.length}].` }));
		this.syncFieldCodes();
		items.markAsDirty();
	}

	removeStaticItem(sourceIndex: number, itemIndex: number): void {
		const items = this.sourcesArray.at(sourceIndex).get('items') as UntypedFormArray;
		items.removeAt(itemIndex);
		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
		items.markAsDirty();
	}

	openReferenceTestDialog(): void {
		const sources = (this.sourcesArray as FormArray<UntypedFormGroup>).getRawValue();
		this.dialog.open(ReferenceTypeTestDialogComponent, {
			data: { sources: JSON.parse(JSON.stringify(sources)) as ExternalFetcherBaseSourceConfigurationPersist[], key: null, label: this.formGroup.get('name').value, referenceTypeId: this.editorModel.id },
			maxHeight: 'min(650px, 100vh)',
		});
	}

	// ---------------------------------------------------------------- manual entries

	get labelTemplateControl() { return this.definition.get('labelTemplate'); }
	get buildsNameFromFields(): boolean { return !!this.labelTemplateControl.value; }

	setBuildsNameFromFields(build: boolean): void {
		const firstTwo = this.fieldsArray.getRawValue().slice(0, 2).map(x => `{${x.code}}`).join(' ');
		this.labelTemplateControl.setValue(build ? (this.labelTemplateControl.value || firstTwo || null) : null);
		this.labelTemplateControl.markAsDirty();
	}

	insertTemplateField(code: string): void {
		const current: string = this.labelTemplateControl.value ?? '';
		this.labelTemplateControl.setValue(`${current}${current && !current.endsWith(' ') ? ' ' : ''}{${code}}`);
		this.labelTemplateControl.markAsDirty();
	}

	/** The pattern with each field shown by its label, as users will read it. */
	get labelTemplateExample(): string {
		const labels = new Map<string, string>(this.fieldsArray.getRawValue().map(x => [x.code, x.label || x.code]));
		return (this.labelTemplateControl.value ?? '').replace(/\{([\w-]+)\}/g, (_m, code) => `[${labels.get(code) ?? code}]`);
	}

	// ---------------------------------------------------------------- XML export / import (frontend only)

	exportDefinitionToXml(): void {
		const definition = this.definition.getRawValue() as ReferenceTypeDefinitionPersist;
		FileSaver.saveAs(new Blob([toXmlString(withoutAuthRequestBodies(definition as any))], { type: 'application/xml' }), `${this.formGroup.get('code')?.value || 'reference-type'}-definition.xml`);
	}

	openImportXmlDialog(): void {
		this.dialog.open(ImportXmlDialogComponent, {
			data: {
				title: this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.TITLE'),
				confirmButton: this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.CONFIRM'),
				cancelButton: this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.CANCEL')
			} as ImportXmlDialogData,
			width: 'min(600px, 90vw)'
		}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(file => { if (file) this.importDefinitionFromXml(file); });
	}

	private importDefinitionFromXml(file: File): void {
		const reader = new FileReader();
		reader.onload = () => {
			try {
				const definition = fromXmlString(reader.result as string) as unknown as ReferenceTypeDefinitionPersist;
				this.editorModel.definition = new ReferenceTypeDefinitionEditorModel(this.editorModel.validationErrorModel).fromModel(definition as any);
				this.formGroup.setControl('definition', this.editorModel.definition.buildForm({ rootPath: 'definition.' }));
				this.definition.markAsDirty();
				this.syncedCodes = this.fieldsArray.getRawValue().map(x => x.code);
				this.selectedSourceIndex = 0;
				if (hasEnabledAuth(definition as any)) this.uiNotificationService.snackBarNotification(this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.SUCCESS-AUTH'), SnackBarNotificationLevel.Warning);
				else this.uiNotificationService.snackBarNotification(this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.SUCCESS'), SnackBarNotificationLevel.Success);
			} catch (error) {
				this.logger.error('Could not parse imported reference type definition XML: ' + error);
				this.uiNotificationService.snackBarNotification(this.language.instant('REFERENCE-TYPE-EDITOR.IMPORT.INVALID-FILE'), SnackBarNotificationLevel.Error);
			}
		};
		reader.readAsText(file);
	}
}
