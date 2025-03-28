
import { Component, OnInit } from '@angular/core';
import { FormGroup, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { CssColorForm, TenantConfigurationEditorModel } from './css-colors-editor.model';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { CssColorsEditorService } from './css-colors-editor.service';
import { CssColorsEditorResolver } from './css-colors-editor.resolver';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { Observable } from 'rxjs';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';


@Component({
    selector: 'app-tenant-configuration-css-colors-editor',
    templateUrl: 'css-colors-editor.component.html',
    styleUrls: ['./css-colors-editor.component.scss'],
    providers: [CssColorsEditorService],
    standalone: false
})
export class CssColorsEditorComponent extends BasePendingChangesComponent implements OnInit {

	isNew = true;
	formGroup: UntypedFormGroup = null;

	get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
	set editorModel(value: TenantConfigurationEditorModel) { this._editorModel = value; }
	private _editorModel: TenantConfigurationEditorModel;

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
		private tenantConfigurationService: TenantConfigurationService,
		private cssColorsEditorService: CssColorsEditorService,
		private analyticsService: AnalyticsService,
        private tenantHandlingService: TenantHandlingService
	) {
		super();
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.TenantConfigurationsColorsEditor);
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});

	}

    get cssColorForm(): FormGroup<CssColorForm> {
        return this.formGroup.get('cssColors') as FormGroup<CssColorForm>;
    }

	private bindColorInputs() {
		this.cssColorForm?.get('primaryColorInput').valueChanges.subscribe((color) => {
			this.cssColorForm?.get('primaryColor').setValue(color, {
				emitEvent: false,
			});
		});
		this.cssColorForm?.get('primaryColor').valueChanges.subscribe((color) =>
			this.cssColorForm?.get('primaryColorInput').setValue(color, {
				emitEvent: false,
			})
		);
	}

	getItem(successFunction: (item: TenantConfiguration) => void) {
		this.tenantConfigurationService.getType(TenantConfigurationType.CssColors, CssColorsEditorResolver.lookupFields())
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

	onCallbackSuccess(cssColors?: any): void {
		this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
        this.tenantHandlingService.applyTenantCssColors(cssColors);
        this.refreshData();
	}


	prepareForm(data: TenantConfiguration) {
		try {
			this.editorModel = data ? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
			this.buildForm();
			this.bindColorInputs();

		} catch (error) {
			this.logger.error('Could not parse TenantConfiguration item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, !this.authService.hasPermission(AppPermission.EditTenantConfiguration));
		this.cssColorsEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
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
        const cssOverride = formData?.cssColors?.cssOverride
        if(cssOverride && (!cssOverride?.startsWith('{') || !cssOverride?.endsWith('}'))){
            formData.cssColors.cssOverride = `{${cssOverride.replace(/[{}]+/g, '')}}`
        }
        this.tenantConfigurationService.persist(formData)
            .pipe(takeUntil(this._destroyed)).subscribe({
                next: (complete) => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(formData?.cssColors),
                error: (error) => this.onCallbackError(error)
            });
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
