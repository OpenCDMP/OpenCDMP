
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Language, LanguagePersist } from '@app/core/model/language/language';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { LanguageEditorModel } from './language-editor.model';
import { LanguageEditorResolver } from './language-editor.resolver';
import { LanguageEditorService } from './language-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';


@Component({
	selector: 'app-language-editor-component',
	templateUrl: 'language-editor.component.html',
	styleUrls: ['./language-editor.component.scss'],
	providers: [LanguageEditorService]
})
export class LanguageEditorComponent extends BaseEditor<LanguageEditorModel, Language> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	availableLanguageCodes: string[] = [];
	currentPayload: string = null;

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteLanguage);
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditLanguage) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditLanguage);
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
		private logger: LoggingService,
		private languageEditorService: LanguageEditorService,
		private fileUtils: FileUtils,
		private titleService: Title,
		private analyticsService: AnalyticsService,
		protected routerUtils: RouterUtilsService
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.code;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('LANGUAGE-EDITOR.TITLE-EDIT-LANGUAGE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.LanguagesEditor);
		super.ngOnInit();
		this.languageHttpService.queryAvailableCodes(this.languageHttpService.buildAutocompleteLookup())
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => this.availableLanguageCodes = data.items,
				error => {
					this.router.navigate([this.routerUtils.generateUrl('/languages')]);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				}
			);
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
			if (data && data.payload) {
				this.currentPayload = data.payload;
			}
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
		this.getItem(this.editorModel.id, (data: Language) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/languages')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as LanguagePersist;

		this.languageHttpService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
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
					this.languageHttpService.delete(value.id).pipe(takeUntil(this._destroyed))
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

	public overrideFromFile(matCheckBox: MatCheckboxChange, code: string) {
		if (matCheckBox.checked == true) {
			this.languageHttpService.getSingleWithCode(code, this.authService.selectedTenant(), LanguageEditorResolver.lookupFields())
				.pipe(takeUntil(this._destroyed))
				.subscribe(language => {
					this.formGroup.get('payload').patchValue(language.payload);
				},
					error => {
						matCheckBox.source.checked = false;
						this.httpErrorHandlingService.handleBackedRequestError(error);
					});
		} else {
			this.formGroup.get('payload').patchValue(this.currentPayload);
		}
	}
}
