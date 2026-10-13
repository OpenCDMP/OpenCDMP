import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent, MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Language, LanguagePersist } from '@app/core/model/language/language';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BUNDLED_LANGUAGE_CODES, LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
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
import { flattenTranslations, LanguageEditorModel, TranslationText, unflattenTranslations } from './language-editor.model';
import { LanguageEditorResolver } from './language-editor.resolver';
import { LanguageEditorService } from './language-editor.service';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { TenantReadonlyBannerComponent } from '../../../../../common/ui/tenant-shared/tenant-readonly-banner.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatRadioGroup, MatRadioButton } from '@angular/material/radio';
import { MatFormField, MatLabel, MatHint, MatError, MatPrefix } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatTooltip } from '@angular/material/tooltip';

interface TextRow {
	id: string;
	path: string[];
	key: string;
	section: string;
	reference: string;
	isJson: boolean;
	placeholders: string[];
	added: boolean;
}

const PLACEHOLDER_PATTERN = /{{\s*([^{}\s]+)\s*}}/g;
const LANGUAGE_CODE_PATTERN = /^[a-z]{2,3}(-[a-z0-9]{2,8})*$/i;
const TEXT_KEY_PATTERN = /^[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*$/;

@Component({
    selector: 'app-language-editor-component',
    templateUrl: 'language-editor.component.html',
    styleUrls: ['./language-editor.component.scss'],
    providers: [LanguageEditorService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, TenantReadonlyBannerComponent, MatButton, MatIcon, FormsModule, MatCard, MatCardContent, MatRadioGroup, MatRadioButton, MatFormField, MatLabel, ReactiveAsteriskDirective, MatInput, MatHint, MatSelect, ReactiveFormsModule, MatOption, MatError, RouterLink, MatProgressBar, MatPrefix, MatButtonToggleGroup, MatButtonToggle, MatIconButton, MatTooltip, MatPaginator, TranslatePipe]
})
export class LanguageEditorComponent extends BaseEditor<LanguageEditorModel, Language> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;

	readonly bundledLanguageCodes = BUNDLED_LANGUAGE_CODES;
	takenLanguageCodes: string[] = [];
	newLanguageKind: 'builtIn' | 'custom' = 'builtIn';

	referenceCode: string = null;
	texts: TextRow[] = [];
	overrides = new Map<string, string>();
	invalidTexts = new Set<string>();
	sections: { key: string, label: string }[] = [];

	searchTerm = '';
	sectionFilter: string = null;
	changedOnly = false;
	visibleTexts: TextRow[] = [];
	pageIndex = 0;
	pageSize = 50;

	addingText = false;
	newTextKey = '';
	newTextValue = '';
	newTextError: { key: string, params?: object } = null;

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteLanguage);
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditLanguage) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get isBuiltIn(): boolean {
		return BUNDLED_LANGUAGE_CODES.includes(this.formGroup?.get('code').value);
	}

	protected get isDefaultLanguage(): boolean {
		return !this.isNew && this.formGroup?.get('code').value === this.languageService.getDefaultLanguagesCode();
	}

	protected get pageTexts(): TextRow[] {
		return this.visibleTexts.slice(this.pageIndex * this.pageSize, (this.pageIndex + 1) * this.pageSize);
	}

	protected get availableBuiltInCodes(): string[] {
		return BUNDLED_LANGUAGE_CODES.filter(code => !this.takenLanguageCodes.includes(code));
	}

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
	}

	constructor(
		// BaseFormEditor injected dependencies
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
		// Rest dependencies. Inject any other needed deps here:
		public enumUtils: EnumUtils,
		private languageHttpService: LanguageHttpService,
		protected languageService: LanguageService,
		private logger: LoggingService,
		private languageEditorService: LanguageEditorService,
		private analyticsService: AnalyticsService,
		protected routerUtils: RouterUtilsService
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.LanguagesEditor);
		super.ngOnInit();
		if (this.isNew) {
			const lookup = this.languageHttpService.buildAutocompleteLookup();
			lookup.project.fields.push(nameof<Language>(x => x.belongsToCurrentTenant));
			this.languageHttpService.query(lookup)
				.pipe(takeUntil(this._destroyed))
				.subscribe(
					data => this.takenLanguageCodes = data.items.filter(x => x.belongsToCurrentTenant !== false).map(x => x.code),
					error => this.httpErrorHandlingService.handleBackedRequestError(error)
				);
		} else {
			this.loadReference(this.isBuiltIn ? this.formGroup.get('code').value : 'en');
		}
	}

	getItem(itemId: Guid, successFunction: (item: Language) => void) {
		this.languageHttpService.getSingle(itemId, LanguageEditorResolver.lookupFields())
			.pipe(map(data => data as Language), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Language) {
		try {
			this.editorModel = data ? new LanguageEditorModel().fromModel(data) : new LanguageEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Language item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditLanguage));
		this.languageEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Language) => {
			this.prepareForm(data);
			this.loadReference(this.referenceCode);
		});
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		if (this.isNew) {
			this.router.navigate([this.routerUtils.generateUrl('/languages/' + id)], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as LanguagePersist;

		this.enqueueService.enqueueExhaustChannel(this.languageHttpService.persist(formData))
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		const code = this.formGroup.get('code');
		if (this.isNew && code.value) {
			if (!LANGUAGE_CODE_PATTERN.test(code.value)) code.setErrors({ pattern: true });
			else if (this.takenLanguageCodes.includes(code.value)) code.setErrors({ taken: true });
		}
		if (!this.isFormValid()) {
			return;
		}
		if (this.invalidTexts.size > 0) {
			this.uiNotificationService.snackBarNotification(this.language.instant('LANGUAGE-EDITOR.VALIDATION.INVALID-TEXTS'), SnackBarNotificationLevel.Error);
			return;
		}

		this.persistEntity();
	}

	public delete() {
		const value = this.formGroup.value;
		if (value.id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.enqueueService.enqueueExhaustChannel(this.languageHttpService.delete(value.id)).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackDeleteSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	//
	// New language
	//

	public onNewLanguageKindChanged(kind: 'builtIn' | 'custom') {
		this.newLanguageKind = kind;
		this.formGroup.get('code').setValue(null);
	}

	//
	// Texts
	//

	public loadReference(code: string) {
		this.languageHttpService.getBundledTranslations(code)
			.pipe(takeUntil(this._destroyed))
			.subscribe(reference => {
				this.referenceCode = code;
				this.texts = flattenTranslations(reference).map(text => this.toTextRow(text, false));
				this.overrides = this.readOverrides(this.formGroup.get('payload').value);
				this.updateSections();
				// Old rows stored a full copy of the file: rewrite the payload as overrides only, without marking the form dirty.
				this.formGroup.get('payload').setValue(this.buildPayload(), { emitEvent: false });
				this.applyTextFilters();
			});
	}

	public applyTextFilters() {
		const term = this.searchTerm.trim().toLowerCase();
		this.visibleTexts = this.texts.filter(row =>
			(!this.sectionFilter || row.path[0] === this.sectionFilter)
			&& (!this.changedOnly || (this.isBuiltIn ? this.overrides.has(row.id) : !this.overrides.has(row.id)))
			&& (!term || row.key.toLowerCase().includes(term) || row.reference.toLowerCase().includes(term) || (this.overrides.get(row.id) ?? '').toLowerCase().includes(term))
		);
		this.pageIndex = 0;
	}

	public onPage(event: PageEvent) {
		this.pageIndex = event.pageIndex;
		this.pageSize = event.pageSize;
	}

	public onTextChanged(row: TextRow, value: string) {
		if (value === '' || (this.isBuiltIn && value === row.reference)) this.overrides.delete(row.id);
		else this.overrides.set(row.id, value);
		this.syncPayload();
	}

	public resetText(row: TextRow) {
		this.overrides.delete(row.id);
		if (row.added) {
			this.texts = this.texts.filter(x => x !== row);
			this.updateSections();
			this.applyTextFilters();
		}
		this.syncPayload();
	}

	public resetAllTexts() {
		this.overrides.clear();
		this.texts = this.texts.filter(x => !x.added);
		this.updateSections();
		this.syncPayload();
		this.applyTextFilters();
	}

	public addText() {
		const key = this.newTextKey.trim();
		const path = key.split('.');
		if (!TEXT_KEY_PATTERN.test(key)) this.newTextError = { key: 'LANGUAGE-EDITOR.VALIDATION.KEY-PATTERN' };
		else if (this.texts.some(row => row.id === this.textId(path))) this.newTextError = { key: 'LANGUAGE-EDITOR.VALIDATION.KEY-EXISTS' };
		else if (this.conflictingKey(path)) this.newTextError = { key: 'LANGUAGE-EDITOR.VALIDATION.KEY-CONFLICT', params: { key: this.conflictingKey(path) } };
		else if (!this.newTextValue.trim()) this.newTextError = { key: 'GENERAL.VALIDATION.REQUIRED' };
		else this.newTextError = null;
		if (this.newTextError) return;

		const row = this.toTextRow({ path: path, value: this.newTextValue }, true);
		this.texts.push(row);
		this.overrides.set(row.id, this.newTextValue);
		this.updateSections();
		this.syncPayload();
		this.searchTerm = key;
		this.applyTextFilters();
		this.cancelAddText();
	}

	public cancelAddText() {
		this.addingText = false;
		this.newTextKey = '';
		this.newTextValue = '';
		this.newTextError = null;
	}

	public missingPlaceholders(row: TextRow): string {
		const value = this.overrides.get(row.id);
		if (value == null) return null;
		const present = this.placeholderNames(value);
		const missing = row.placeholders.filter(name => !present.includes(name));
		return missing.length > 0 ? missing.map(name => `{{${name}}}`).join(', ') : null;
	}

	public exportTexts() {
		const blob = new Blob([this.formGroup.get('payload').value ?? '{}'], { type: 'application/json' });
		FileSaver.saveAs(blob, `${this.formGroup.get('code').value}.json`);
	}

	public importTexts(input: HTMLInputElement) {
		const file = input.files?.[0];
		input.value = '';
		if (!file) return;
		file.text().then(content => {
			let imported: TranslationText[];
			try {
				imported = flattenTranslations(JSON.parse(content));
			} catch {
				this.uiNotificationService.snackBarNotification(this.language.instant('LANGUAGE-EDITOR.SNACK-BAR.IMPORT-FAILED'), SnackBarNotificationLevel.Error);
				return;
			}
			const rows = new Map(this.texts.map(row => [row.id, row]));
			let count = 0;
			for (const text of imported) {
				let row = rows.get(this.textId(text.path));
				if (!row) {
					if (this.conflictingKey(text.path)) continue;
					row = this.toTextRow(text, true);
					this.texts.push(row);
				}
				this.onTextChanged(row, typeof text.value === 'string' ? text.value : JSON.stringify(text.value));
				count++;
			}
			this.updateSections();
			this.applyTextFilters();
			this.uiNotificationService.snackBarNotification(this.language.instant('LANGUAGE-EDITOR.SNACK-BAR.IMPORTED', { count: count }), SnackBarNotificationLevel.Success);
		});
	}

	private syncPayload() {
		this.formGroup.get('payload').setValue(this.buildPayload());
		this.formGroup.markAsDirty();
	}

	private buildPayload(): string {
		const texts: TranslationText[] = [];
		this.invalidTexts.clear();
		for (const row of this.texts) {
			const value = this.overrides.get(row.id);
			if (value == null) continue;
			if (!row.isJson) {
				texts.push({ path: row.path, value: value });
				continue;
			}
			try {
				texts.push({ path: row.path, value: JSON.parse(value) });
			} catch {
				this.invalidTexts.add(row.id);
			}
		}
		return texts.length > 0 ? JSON.stringify(unflattenTranslations(texts)) : null;
	}

	private readOverrides(payload: string): Map<string, string> {
		const result = new Map<string, string>();
		if (!payload) return result;
		const rows = new Map(this.texts.map(row => [row.id, row]));
		for (const text of flattenTranslations(JSON.parse(payload))) {
			let row = rows.get(this.textId(text.path));
			// A key the bundled file doesn't have, e.g. a plugin's label: keep it as an added text.
			if (!row) {
				if (this.conflictingKey(text.path)) continue;
				row = this.toTextRow(text, true);
				this.texts.push(row);
			}
			const value = typeof text.value === 'string' ? text.value : JSON.stringify(text.value);
			if (value !== '' && !(this.isBuiltIn && value === row.reference)) result.set(row.id, value);
		}
		return result;
	}

	private toTextRow(text: TranslationText, added: boolean): TextRow {
		const reference = added ? '' : typeof text.value === 'string' ? text.value : JSON.stringify(text.value);
		return {
			id: this.textId(text.path),
			path: text.path,
			key: text.path.join('.'),
			section: text.path.slice(0, -1).map(x => this.humanize(x)).join(' › '),
			reference: reference,
			isJson: typeof text.value !== 'string',
			placeholders: this.placeholderNames(reference),
			added: added
		};
	}

	// An added key must not be a section of built-in texts, nor sit inside a built-in text.
	private conflictingKey(path: string[]): string {
		const key = path.join('.');
		const conflict = this.texts.find(row => !row.added && (row.key.startsWith(key + '.') || key.startsWith(row.key + '.')));
		return conflict ? (conflict.key.startsWith(key + '.') ? key : conflict.key) : null;
	}

	private updateSections() {
		this.sections = [...new Set(this.texts.filter(x => x.path.length > 1).map(x => x.path[0]))]
			.map(key => ({ key: key, label: this.humanize(key) }));
	}

	private textId(path: string[]): string {
		return path.join('\u001f');
	}

	private placeholderNames(value: string): string[] {
		return [...value.matchAll(PLACEHOLDER_PATTERN)].map(match => match[1]);
	}

	private humanize(key: string): string {
		const words = key.replace(/[-_]+/g, ' ').trim().toLowerCase();
		return words.charAt(0).toUpperCase() + words.slice(1);
	}
}
