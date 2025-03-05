import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TenantConfigurationEditorModel } from './default-plan-blueprint-editor.model';
import { MatDialog } from '@angular/material/dialog';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { DefaultPlanBlueprintEditorService } from './default-plan-blueprint-editor.service';
import { DefaultPlanBlueprintEditorResolver } from './default-plan-blueprint-editor.resolver';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';

@Component({
    selector: 'app-tenant-configuration-default-plan-blueprint-editor',
    templateUrl: './default-plan-blueprint-editor.component.html',
    styleUrls: ['./default-plan-blueprint-editor.component.scss'],
    providers: [DefaultPlanBlueprintEditorService],
    standalone: false
})
export class DefaultPlanBlueprintEditorComponent extends BasePendingChangesComponent implements OnInit {
    @ViewChild('planBlueprintSelect') planBlueprintSelect: SingleAutoCompleteComponent;
	
	get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
	set editorModel(value: TenantConfigurationEditorModel) { this._editorModel = value; }
	private _editorModel: TenantConfigurationEditorModel;

	isNew = true;
	formGroup: UntypedFormGroup = null;

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
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
		protected formService: FormService,
		private logger: LoggingService,
		private tenantConfigurationService: TenantConfigurationService,
		public defaultPlanBlueprintEditorService: DefaultPlanBlueprintEditorService,
		public planBlueprintService: PlanBlueprintService,
		private tenantHandlingService: TenantHandlingService
	) {
		super();
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.getExistingSelections();
	}

	getExistingSelections() {
		this.tenantConfigurationService.getType(TenantConfigurationType.DefaultPlanBlueprint, DefaultPlanBlueprintEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					try {
						this.editorModel = data?.defaultPlanBlueprint? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
						this.buildForm();
						this.tenantHandlingService.addPlanBlueprintGroupIdSubject(data);
					} catch {
						this.logger.error('Could not parse Tenant Configuration: ' + data);
						this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
					}
				},
				error => this.onCallbackError(error)
			);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);

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
		this.defaultPlanBlueprintEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getExistingSelections();
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
		this.prepareForm(null);
	}


	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}
}
