
import { Component, effect, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { TenantConfigurationEditorModel } from './plugin-configuration-editor.model';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { combineLatest, Observable } from 'rxjs';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { Guid } from '@common/types/guid';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { PluginConfigurationEditorService } from './plugin-configuration-editor.service';
import { PluginConfigurationEditorResolver } from './plugin-configuration-editor.resolver';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { toObservable } from '@angular/core/rxjs-interop';
import { PluginConfiguration, PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';


@Component({
	selector: 'app-tenant-configuration-plugin-configuration-editor',
	templateUrl: 'plugin-configuration-editor.component.html',
	styleUrls: ['./plugin-configuration-editor.component.scss'],
	providers: [PluginConfigurationEditorService],
    standalone: false
})
export class PluginConfigurationEditorComponent extends BasePendingChangesComponent implements OnInit {

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

    fileMap = new Map<Guid, StorageFile>([]);
    availablePluginConfiguration: Observable<PluginRepositoryConfiguration[]>;
	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,
		private logger: LoggingService,
		private tenantConfigurationService: TenantConfigurationService,
		private pluginConfigurationEditorService: PluginConfigurationEditorService,
		private analyticsService: AnalyticsService,
		public pluginConfigurationService: PluginConfigurationService

	) {
		super();
        this.availablePluginConfiguration = toObservable(this.pluginConfigurationService.pluginRepositoryConfiguration);
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PluginConfigurationEditor);
        combineLatest([
            this.getItem(),
            this.availablePluginConfiguration
            
        ]).pipe(takeUntil(this._destroyed))
        .subscribe(([entity, availablePluginConfiguration]) => {
            if(availablePluginConfiguration){
                this.prepareForm(entity);
                if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
                    this.formGroup.disable();
                }
            }
        })
	}

	getItem() {
		return this.tenantConfigurationService.getType(TenantConfigurationType.PluginConfiguration, PluginConfigurationEditorResolver.lookupFields())
			.pipe(map(data => data as TenantConfiguration), takeUntil(this._destroyed))
			// .subscribe(
			// 	data => successFunction(data),
			// 	error => this.onCallbackError(error)
			// );
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
            this.fileMap.clear();
			if (data?.pluginConfiguration?.pluginConfigurations) {
                data.pluginConfiguration.pluginConfigurations?.forEach((config) => {
                    config.fields?.forEach((field) => {
                        if(field.fileValue){
                            this.fileMap.set(field.fileValue.id, field.fileValue)
                        }
                    })
                })

			} else {
				const pluginConfigurations = this.pluginConfigurationService.availablePlugins();
				data = {
                    ...data,
					pluginConfiguration : {
						pluginConfigurations
					},
					type: TenantConfigurationType.PluginConfiguration
				}
            }
			this.editorModel = new TenantConfigurationEditorModel().fromModel(data, this.pluginConfigurationService.pluginRepositoryConfiguration(),);
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse TenantConfiguration item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, !this.authService.hasPermission(AppPermission.EditTenantConfiguration));
		this.pluginConfigurationEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem().subscribe((entity) => {
            this.prepareForm(entity);
            if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
                this.formGroup.disable();
            }
        })
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
