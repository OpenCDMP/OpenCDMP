import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PrefillingSourceSystemTargetType } from '@app/core/common/enum/prefilling-source-system-target-type';
import { PrefillingSource, PrefillingSourceDefinitionPersist, PrefillingSourcePersist } from '@app/core/model/prefilling-source/prefilling-source';
import { ReferenceSourceTestResult } from '@app/core/model/reference/reference';
import { ReferenceTestLookup } from '@app/core/query/reference-test.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { HeaderConfigEditorModel, QueryCaseConfigEditorModel, QueryConfigEditorModel, ResultFieldsMappingConfigurationEditorModel } from '@app/ui/external-fetcher/external-fetcher-source-editor.model';
import { fromXmlString, toXmlString } from '@app/ui/external-fetcher/external-fetcher-xml.util';
import { ImportXmlDialogComponent, ImportXmlDialogData } from '@app/ui/external-fetcher/import-xml-dialog/import-xml.dialog.component';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { PrefillingSourceDefinitionEditorModel, PrefillingSourceEditorModel } from './prefilling-source-editor.model';
import { PrefillingSourceEditorResolver } from './prefilling-source-editor.resolver';
import { PrefillingSourceEditorService } from './prefilling-source-editor.service';
import { Pf2FillRuleDialogComponent, Pf2FillRuleDialogData, Pf2FillRuleValue } from './fill-rule/fill-rule.dialog';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { TenantReadonlyBannerComponent } from '../../../../../common/ui/tenant-shared/tenant-readonly-banner.component';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { Rt2SourceEditorComponent } from '../../../external-fetcher/source-editor/source-editor.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatTooltip } from '@angular/material/tooltip';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';

export type Pf2Tab = 'connection' | 'search' | 'record' | 'fill' | 'try';

// The search must name these for the pick list; Prefilling._id/_label in the backend read them.
const PICK_CODES = ['reference_id', 'label', 'description'];

export interface Pf2Rule {
	kind: 'field' | 'fixed';
	index: number;
	form: UntypedFormGroup;
}

// The connection (headers and authentication) is edited once on the search request and copied into the full-record
// request on save, since the definition stores it in both.
@Component({
    selector: 'app-prefilling-source-editor',
    templateUrl: './prefilling-source-editor.component.html',
    styleUrls: ['./prefilling-source-editor.component.scss'],
    providers: [PrefillingSourceEditorService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, MatButton, MatIconButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, TenantReadonlyBannerComponent, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, Rt2SourceEditorComponent, MatSlideToggle, MatTooltip, ReactiveAsteriskDirective, TranslatePipe]
})
export class PrefillingSourceEditorComponent extends BaseEditor<PrefillingSourceEditorModel, PrefillingSource> implements OnInit {

	formGroup: UntypedFormGroup = null;
	tab: Pf2Tab = 'connection';
	attemptedSave = false;
	readonly systemTargets = this.enumUtils.getEnumValues<PrefillingSourceSystemTargetType>(PrefillingSourceSystemTargetType);
	readonly pickCodes = PICK_CODES;

	readonly searchTest = signal<ReferenceSourceTestResult>(null);
	readonly recordTest = signal<ReferenceSourceTestResult>(null);
	readonly tryLike = signal('');
	readonly tryRunning = signal(false);
	readonly tryResults = signal<Record<string, string>[]>(null);
	readonly tryPicked = signal<Record<string, string>>(null);
	readonly tryRecord = signal<Record<string, string>>(null);
	readonly tryError = signal<string>(null);

	get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(AppPermission.DeletePrefillingSource) && this.editorModel.belongsToCurrentTenant != false;
	}

	get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(AppPermission.EditPrefillingSource) && this.editorModel.belongsToCurrentTenant != false;
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
		private prefillingSourceService: PrefillingSourceService,
		private logger: LoggingService,
		private prefillingSourceEditorService: PrefillingSourceEditorService,
		protected routerUtils: RouterUtilsService,
		private analyticsService: AnalyticsService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PrefillingSourcesEditor);
		super.ngOnInit();
	}

	// ---------------------------------------------------------------- data

	getItem(itemId: Guid, successFunction: (item: PrefillingSource) => void) {
		this.prefillingSourceService.getSingle(itemId, PrefillingSourceEditorResolver.lookupFields())
			.pipe(map(data => data as PrefillingSource), takeUntil(this._destroyed))
			.subscribe({ next: data => successFunction(data), error: error => this.onCallbackError(error) });
	}

	prepareForm(data: PrefillingSource) {
		try {
			this.editorModel = data ? new PrefillingSourceEditorModel().fromModel(data) : new PrefillingSourceEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse PrefillingSource item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		const readOnly = this.isDeleted || !this.authService.hasPermission(AppPermission.EditPrefillingSource);
		this.formGroup = this.editorModel.buildForm(null, readOnly);
		if (readOnly) this.formGroup.disable();
		this.prefillingSourceEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		PICK_CODES.forEach(code => this.ensureMapping(this.searchConfig, 'searchConfiguration', code));
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: PrefillingSource) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
		if (this.isNew && id) {
			this.router.navigate([this.routerUtils.generateUrl(`/prefilling-sources/${id}`)], { replaceUrl: true });
			return;
		}
		this.refreshData();
	}

	cancel(): void {
		this.router.navigate([this.routerUtils.generateUrl('/prefilling-sources')]);
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as PrefillingSourcePersist;
		this.enqueueService.enqueueExhaustChannel(this.prefillingSourceService.persist(formData, [nameof<PrefillingSource>(x => x.id)]))
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error: error => this.onCallbackError(error)
			});
	}

	formSubmit(): void {
		this.attemptedSave = true;
		this.fillRequestDefaults();
		this.copyConnectionToRecord();
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			if (this.formGroup.get('label').invalid || this.formGroup.get('code').invalid) this.tab = 'connection';
			else if (this.searchConfig.invalid) this.tab = 'search';
			else if (this.recordEnabled && this.recordSourceConfig.invalid) this.tab = 'record';
			else this.tab = 'fill';
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
			this.enqueueService.enqueueExhaustChannel(this.prefillingSourceService.delete(value.id)).pipe(takeUntil(this._destroyed))
				.subscribe({ next: () => { this.formGroup.markAsPristine(); this.onCallbackDeleteSuccess(); }, error: error => this.onCallbackError(error) });
		});
	}

	// ---------------------------------------------------------------- shape

	get definition(): UntypedFormGroup { return this.formGroup.get('definition') as UntypedFormGroup; }
	get searchConfig(): UntypedFormGroup { return this.definition.get('searchConfiguration') as UntypedFormGroup; }
	get recordEnabled(): boolean { return this.definition.get('getEnabled').value === true; }
	get recordSourceConfig(): UntypedFormGroup { return this.definition.get('getConfiguration') as UntypedFormGroup; }
	/** Where the fill rules read their values: the full record when it is fetched, else the search result. */
	get valuesConfig(): UntypedFormGroup { return this.recordEnabled ? this.recordSourceConfig : this.searchConfig; }
	get fieldsArray(): UntypedFormArray { return this.definition.get('fields') as UntypedFormArray; }
	get fixedArray(): UntypedFormArray { return this.definition.get('fixedValueFields') as UntypedFormArray; }

	setTab(tab: Pf2Tab): void { this.tab = tab; }

	private mappings(config: UntypedFormGroup): UntypedFormArray { return config.get('results').get('fieldsMapping') as UntypedFormArray; }

	/** The values the fill rules can take, by the name the record step gives them. */
	get recordCodes(): string[] { return this.mappings(this.valuesConfig).getRawValue().map(x => x.code).filter(x => !!x); }

	private ensureMapping(config: UntypedFormGroup, controlName: string, code: string, responsePath: string = null): void {
		const rows = this.mappings(config);
		if (rows.getRawValue().some(x => x.code === code)) return;
		const row = new ResultFieldsMappingConfigurationEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath: `definition.${controlName}.results.fieldsMapping[${rows.length}].` });
		row.patchValue({ code, responsePath });
		rows.push(row);
	}

	// the backend fills {like} in a URL only through a search rule of that name
	private ensureLikeQuery(): void {
		const queries = this.recordSourceConfig.get('queries') as UntypedFormArray;
		if (queries.getRawValue().some(x => x.name === 'like')) return;
		const rootPath = `definition.getConfiguration.queries[${queries.length}].`;
		const query = new QueryConfigEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath });
		query.get('name').setValue('like');
		const queryCase = new QueryCaseConfigEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath: `${rootPath}cases[0].` });
		queryCase.get('value').setValue('{like}');
		(query.get('cases') as UntypedFormArray).push(queryCase);
		queries.push(query);
	}

	// ---------------------------------------------------------------- connection

	/** Search and full record each store headers and authentication; the editor keeps the record's equal to the search's. */
	private copyConnectionToRecord(): void {
		if (!this.recordEnabled) return;
		const search = this.searchConfig.getRawValue();
		const record = this.recordSourceConfig;
		record.get('contentType').setValue(search.contentType);
		record.get('auth').patchValue(search.auth);
		const headers = record.get('headers') as UntypedFormArray;
		headers.clear();
		(search.headers ?? []).forEach((h, i) => {
			const row = new HeaderConfigEditorModel(this.editorModel.validationErrorModel).buildForm({ rootPath: `definition.getConfiguration.headers[${i}].` });
			row.patchValue(h);
			headers.push(row);
		});
	}

	/** Key, label and ordinal are required on each request, but nobody picks them here. */
	private fillRequestDefaults(): void {
		const label = this.formGroup.get('label').value;
		[[this.searchConfig, 'search'], [this.recordEnabled ? this.recordSourceConfig : null, 'record']].forEach(([config, key]: [UntypedFormGroup, string]) => {
			if (!config) return;
			if (!config.get('key').value) config.get('key').setValue(key);
			if (!config.get('label').value) config.get('label').setValue(label);
			if (config.get('ordinal').value == null) config.get('ordinal').setValue(1);
		});
	}

	// ---------------------------------------------------------------- full record

	setRecordEnabled(enabled: boolean): void {
		const definition = this.definition;
		definition.get('getEnabled').setValue(enabled);
		const ruleCodes = this.fieldsArray.getRawValue().map(x => x.code).filter(x => !!x);
		if (enabled) {
			if (!definition.get('getConfiguration')) new PrefillingSourceDefinitionEditorModel(this.editorModel.validationErrorModel).buildGetConfiguration(definition, 'definition.');
			// the rules' values move with them: the record now provides them
			const searchRows = this.mappings(this.searchConfig).getRawValue();
			ruleCodes.forEach(code => this.ensureMapping(this.recordSourceConfig, 'getConfiguration', code, searchRows.find(x => x.code === code)?.responsePath ?? null));
			this.ensureLikeQuery();
			this.fillRequestDefaults();
			this.copyConnectionToRecord();
		} else {
			const recordRows = definition.get('getConfiguration') ? this.mappings(this.recordSourceConfig).getRawValue() : [];
			ruleCodes.forEach(code => this.ensureMapping(this.searchConfig, 'searchConfiguration', code, recordRows.find(x => x.code === code)?.responsePath ?? null));
			if (definition.get('getConfiguration')) definition.removeControl('getConfiguration');
		}
		PrefillingSourceEditorModel.reApplyDefinitionValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
		definition.markAsDirty();
		this.recordTest.set(null);
	}

	/** The search test's results, to fetch one of them in the record test. */
	get pickedRecordOptions(): { value: string, label: string }[] {
		return (this.searchTest()?.results ?? []).filter(x => !!x['reference_id']).map(x => ({ value: x['reference_id'], label: `${x['label'] ?? ''} · ${x['reference_id']}` }));
	}

	readonly testSearchFn = (lookup: ReferenceTestLookup): Observable<ReferenceSourceTestResult> => this.prefillingSourceService.testSource({ like: lookup.like, key: lookup.key, sources: lookup.sources });

	// ---------------------------------------------------------------- fill rules

	get rules(): Pf2Rule[] {
		return [
			...this.fieldsArray.controls.map((form, index) => ({ kind: 'field' as const, index, form: form as UntypedFormGroup })),
			...this.fixedArray.controls.map((form, index) => ({ kind: 'fixed' as const, index, form: form as UntypedFormGroup }))
		];
	}

	/** The last value the record step returned for a code: the record test, or the search test when no record is fetched. */
	sampleOf(code: string): string {
		const test = this.recordEnabled ? this.recordTest() : this.searchTest();
		return test?.results?.[0]?.[code] ?? null;
	}

	ruleProblem(rule: Pf2Rule): string {
		const value = rule.form.getRawValue();
		if (!value.systemFieldTarget && !value.semanticTarget) return 'PREFILLING-SOURCE-EDITOR.FILL.NO-TARGET';
		if (rule.kind === 'field' && value.code && !this.recordCodes.includes(value.code)) return 'PREFILLING-SOURCE-EDITOR.FILL.NOT-RETURNED';
		return null;
	}

	get problemCount(): number { return this.rules.filter(r => !!this.ruleProblem(r)).length; }

	/** Long semantics differ only at the end; the chip shows that end and the tooltip the whole name. */
	shortSemantic(semantic: string): string {
		const parts = (semantic ?? '').split('.');
		return parts.length > 3 ? `…${parts.slice(-2).join('.')}` : semantic;
	}

	systemTargetName(target: string): string {
		return target ? this.enumUtils.toPrefillingSourceSystemTargetTypeString(target as PrefillingSourceSystemTargetType) : null;
	}

	openRule(rule?: Pf2Rule): void {
		const value = rule?.form.getRawValue();
		const data: Pf2FillRuleDialogData = {
			value: rule ? { kind: rule.kind, code: value.code ?? null, fixedValue: value.fixedValue ?? null, systemFieldTarget: value.systemFieldTarget ?? null, semanticTarget: value.semanticTarget ?? null } : null,
			codes: this.recordCodes.map(code => ({ code, sample: this.sampleOf(code) })),
			canDelete: !!rule
		};
		this.dialog.open(Pf2FillRuleDialogComponent, { width: 'min(780px, 94vw)', data }).afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Pf2FillRuleValue | 'delete') => {
			if (!result) return;
			if (rule) this.removeRule(rule);
			if (result === 'delete') return;
			this.addRule(result);
		});
	}

	private addRule(value: Pf2FillRuleValue): void {
		const target = value.kind === 'field' ? this.fieldsArray : this.fixedArray;
		const form = value.kind === 'field' ? this.editorModel.createChildField(target.length) : this.editorModel.createChildFixedValueField(target.length);
		form.patchValue(value.kind === 'field'
			? { code: value.code, systemFieldTarget: value.systemFieldTarget, semanticTarget: value.semanticTarget }
			: { fixedValue: value.fixedValue, systemFieldTarget: value.systemFieldTarget, semanticTarget: value.semanticTarget });
		target.push(form);
		target.markAsDirty();
	}

	removeRule(rule: Pf2Rule): void {
		(rule.kind === 'field' ? this.fieldsArray : this.fixedArray).removeAt(rule.index);
		PrefillingSourceEditorModel.reApplyDefinitionValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
		this.definition.markAsDirty();
	}

	// ---------------------------------------------------------------- try it

	/** The tab whose request still lacks a method or URL; the backend answers those with an internal error. */
	get tryBlocker(): Pf2Tab {
		const ready = (config: UntypedFormGroup) => config.get('httpMethod').value != null && !!config.get('url').value?.trim();
		if (!ready(this.searchConfig)) return 'search';
		if (this.recordEnabled && !ready(this.recordSourceConfig)) return 'record';
		return null;
	}

	runTrySearch(): void {
		if (this.tryBlocker) return;
		this.tryRunning.set(true);
		this.tryError.set(null);
		this.tryPicked.set(null);
		this.tryRecord.set(null);
		this.prefillingSourceService.testSource({ like: this.tryLike() || null, key: this.searchConfig.get('key').value, sources: [JSON.parse(JSON.stringify(this.searchConfig.getRawValue()))] })
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: result => { this.tryRunning.set(false); this.tryError.set(result.error ?? null); this.tryResults.set(result.results ?? []); },
				error: error => { this.tryRunning.set(false); this.httpErrorHandlingService.handleBackedRequestError(error); }
			});
	}

	pickTryResult(result: Record<string, string>): void {
		this.tryPicked.set(result);
		this.tryRecord.set(null);
		if (!this.recordEnabled) { this.tryRecord.set(result); return; }
		this.copyConnectionToRecord();
		this.tryRunning.set(true);
		this.prefillingSourceService.testSource({ like: result['reference_id'], key: this.recordSourceConfig.get('key').value, sources: [JSON.parse(JSON.stringify(this.recordSourceConfig.getRawValue()))] })
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: record => { this.tryRunning.set(false); this.tryError.set(record.error ?? null); this.tryRecord.set(record.results?.[0] ?? {}); },
				error: error => { this.tryRunning.set(false); this.httpErrorHandlingService.handleBackedRequestError(error); }
			});
	}

	/** What each rule puts where, for the picked record; mirrors PrefillingSourceServiceImpl.mapPrefilledEntityToDescription. */
	get tryOutcome(): { title: string, description: string, tags: string[], semantics: { semantic: string, value: string, from: string }[], filled: number } {
		const record = this.tryRecord();
		if (!record) return null;
		const out = { title: null, description: null, tags: [] as string[], semantics: [] as { semantic: string, value: string, from: string }[], filled: 0 };
		this.rules.forEach(rule => {
			const v = rule.form.getRawValue();
			const value: string = rule.kind === 'field' ? record[v.code] : v.fixedValue;
			if (value == null || value === '') return;
			out.filled++;
			const from = rule.kind === 'field' ? v.code : this.language.instant('PREFILLING-SOURCE-EDITOR.FILL.FIXED');
			if (v.systemFieldTarget === PrefillingSourceSystemTargetType.Label) out.title = value;
			if (v.systemFieldTarget === PrefillingSourceSystemTargetType.Description) out.description = value;
			if (v.systemFieldTarget === PrefillingSourceSystemTargetType.Tags) {
				let tags: string[];
				try { const parsed = JSON.parse(value); tags = Array.isArray(parsed) ? parsed.map(String) : [value]; } catch { tags = [value]; }
				tags.forEach(t => { if (!out.tags.includes(t.trim())) out.tags.push(t.trim()); });
			}
			if (v.semanticTarget) out.semantics.push({ semantic: v.semanticTarget, value, from });
		});
		return out;
	}

	// ---------------------------------------------------------------- XML export / import (frontend only)

	exportDefinitionToXml(): void {
		const definition = this.definition.getRawValue() as PrefillingSourceDefinitionPersist;
		FileSaver.saveAs(new Blob([toXmlString(definition as any)], { type: 'application/xml' }), `${this.formGroup.get('code')?.value || 'prefilling-source'}-definition.xml`);
	}

	openImportXmlDialog(): void {
		this.dialog.open(ImportXmlDialogComponent, {
			data: {
				title: this.language.instant('PREFILLING-SOURCE-EDITOR.IMPORT.TITLE'),
				confirmButton: this.language.instant('PREFILLING-SOURCE-EDITOR.IMPORT.CONFIRM'),
				cancelButton: this.language.instant('PREFILLING-SOURCE-EDITOR.IMPORT.CANCEL')
			} as ImportXmlDialogData,
			width: 'min(600px, 90vw)'
		}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(file => { if (file) this.importDefinitionFromXml(file); });
	}

	private importDefinitionFromXml(file: File): void {
		const reader = new FileReader();
		reader.onload = () => {
			try {
				const definition = fromXmlString(reader.result as string) as unknown as PrefillingSourceDefinitionPersist;
				this.editorModel.definition = new PrefillingSourceDefinitionEditorModel(this.editorModel.validationErrorModel).fromModel(definition as any);
				this.formGroup.setControl('definition', this.editorModel.definition.buildForm({ rootPath: 'definition.' }));
				PICK_CODES.forEach(code => this.ensureMapping(this.searchConfig, 'searchConfiguration', code));
				this.definition.markAsDirty();
				this.uiNotificationService.snackBarNotification(this.language.instant('PREFILLING-SOURCE-EDITOR.IMPORT.SUCCESS'), SnackBarNotificationLevel.Success);
			} catch (error) {
				this.logger.error('Could not parse imported prefilling source definition XML: ' + error);
				this.uiNotificationService.snackBarNotification(this.language.instant('PREFILLING-SOURCE-EDITOR.IMPORT.INVALID-FILE'), SnackBarNotificationLevel.Error);
			}
		};
		reader.readAsText(file);
	}
}
