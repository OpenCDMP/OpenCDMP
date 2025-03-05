import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { Observable } from 'rxjs';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EvaluatorEditorService } from './evaluator-editor.service';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { EvaluatorSourceEditorModel, EvaluatorTenantConfigurationEditorModel, TenantConfigurationEditorModel } from './evaluator-editor.model';
import { EvaluatorEditorResolver } from './evaluator-editor.resolver';
@Component({
    selector: 'app-tenant-configuration-evaluator-editor',
    templateUrl: './evaluator-editor.component.html',
    styleUrl: './evaluator-editor.component.scss',
    providers: [EvaluatorEditorService],
    standalone: false
})
export class EvaluatorEditorComponent extends BasePendingChangesComponent implements OnInit{

  isNew = true;
	formGroup: UntypedFormGroup = null;

  get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
  set editorModel( value: TenantConfigurationEditorModel) { this._editorModel = value;}
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
		private evaluatorEditorService: EvaluatorEditorService,
		private analyticsService: AnalyticsService
	) {
		super();
	}

  canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

  ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.FileTransformerEditor);
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
	}

  getItem(successFunction: (item: TenantConfiguration) => void) {
    this.tenantConfigurationService.getType(TenantConfigurationType.EvaluatorPlugins, EvaluatorEditorResolver.lookupFields())
      .pipe(map(data => data as TenantConfiguration), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
  }

  onCallbackError(errorResponse: HttpErrorResponse){
    this.httpErrorHandlingService.handleBackedRequestError(errorResponse)

    const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
    if( error.statusCode === 400){
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
		this.evaluatorEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
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

  addSource(): void {
    const formArray = this.formGroup.get('evaluatorPlugins').get('sources') as UntypedFormArray;
    const source: EvaluatorSourceEditorModel = new EvaluatorSourceEditorModel(this.editorModel.validationErrorModel);
    formArray.push(source.buildForm({ rootPath: 'evaluatorPlugins.sources[' + formArray.length + '].' }));
}

removeSource(sourceIndex: number): void {
  (this.formGroup.get('evaluatorPlugins').get('sources') as UntypedFormArray).removeAt(sourceIndex);

  // Reapply validators
   EvaluatorTenantConfigurationEditorModel.reapplySourcesFieldsValidators(
      {
          formArray: this.formGroup.get('evaluatorPlugins').get('sources') as UntypedFormArray,
          validationErrorModel: this.editorModel.validationErrorModel,
          rootPath: 'evaluatorPlugins.'
      }
  )
  this.formGroup.get('evaluatorPlugins').get('sources').markAsDirty();
}
}
