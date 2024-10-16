
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { AuthService } from '@app/core/services/auth/auth.service';
import { CultureInfo, CultureService } from '@app/core/services/culture/culture-service';
import { DefaultUserLocaleService } from '@app/core/services/default-user-locale/default-user-locale.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import moment from 'moment';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { TenantConfigurationEditorModel } from './default-user-locale-editor.model';
import { DefaultUserLocaleEditorResolver } from './default-user-locale-editor.resolver';
import { DefaultUserLocaleEditorService } from './default-user-locale-editor.service';


@Component({
	selector: 'app-tenant-configuration-default-user-locale-editor',
	templateUrl: 'default-user-locale-editor.component.html',
	styleUrls: ['./default-user-locale-editor.component.scss'],
	providers: [DefaultUserLocaleEditorService]
})
export class DefaultUserLocaleEditorComponent extends BasePendingChangesComponent implements OnInit {

	isNew = true;
	formGroup: UntypedFormGroup = null;

	get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
	set editorModel(value: TenantConfigurationEditorModel) { this._editorModel = value; }
	private _editorModel: TenantConfigurationEditorModel;

	timezones: any[];
	cultures: CultureInfo[];
	languages = [];

	singleTimezoneAutocompleteConfiguration: SingleAutoCompleteConfiguration;
	singleCultureAutocompleteConfiguration: SingleAutoCompleteConfiguration;

	protected get canDelete(): boolean {
		return !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteTenantConfiguration);
	}

	protected get canSave(): boolean {
		return this.hasPermission(this.authService.permissionEnum.EditTenantConfiguration);
	}

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission);
	}

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,
		private logger: LoggingService,
		private cultureService: CultureService,
		private tenantConfigurationService: TenantConfigurationService,
		private languageService: LanguageService,
		private defaultUserLocaleEditorService: DefaultUserLocaleEditorService,
		private defaultUserLocaleService: DefaultUserLocaleService,
		private analyticsService: AnalyticsService
	) {
		super();
		this.languages = this.languageService.getAvailableLanguagesCodes().sort((x, y) => x.localeCompare(y));
		this.cultures = this.cultureService.getCultureValues().sort((x, y) => x.displayName.localeCompare(y.displayName));
		this.timezones = moment.tz.names().sort((x, y) => x.localeCompare(y));
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.singleTimezoneAutocompleteConfiguration = this.defaultUserLocaleService.singleTimezoneAutocompleteConfiguration;
		this.singleCultureAutocompleteConfiguration = this.defaultUserLocaleService.singleCultureAutocompleteConfiguration;

		this.analyticsService.trackPageView(AnalyticsService.TenantConfigurationsUserLocaleEditor);

		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
	}

	getItem(successFunction: (item: TenantConfiguration) => void) {
		this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.DefaultUserLocale, DefaultUserLocaleEditorResolver.lookupFields())
			.pipe(map(data => data as TenantConfiguration), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse)

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		}
	}

	onCallbackSuccess(data?: any): void {
		this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.refreshData();
	}


	prepareForm(data: TenantConfiguration) {
		try {
			this.editorModel = data ? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();

			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse TenantConfiguration item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, !this.authService.hasPermission(AppPermission.EditTenantConfiguration));
		this.defaultUserLocaleEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as TenantConfigurationPersist;

		this.tenantConfigurationService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.clearErrorModel();
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistEntity();
	}

	public isFormValid() {
		return this.formGroup.valid;
	}

	public delete() {
		const value = this.formGroup.value;
		if (value.id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('TENANT-CONFIGURATION-EDITOR.RESET-TO-DEFAULT-DIALOG.RESET-TO-DEFAULT'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.tenantConfigurationService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackDeleteSuccessConfig(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	onCallbackDeleteSuccessConfig(data?: any): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-RESET'), SnackBarNotificationLevel.Success);
		this.prepareForm(null)
	}


	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

}
