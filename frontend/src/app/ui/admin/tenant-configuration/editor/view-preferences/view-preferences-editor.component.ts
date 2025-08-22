import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, OnInit, ViewChild } from '@angular/core';
import { FormArray, UntypedFormGroup } from '@angular/forms';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { TenantConfigurationEditorModel } from './view-preferences-editor.model';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ViewPreferenceEditorService } from './view-preferences-editor.service';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { ViewPreferencesEditorResolver } from './view-preferences-editor.resolver';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { ViewPreference } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';

@Component({
    selector: 'app-tenant-configuration-view-preferences-editor',
    templateUrl: './view-preferences-editor.component.html',
    styleUrls: ['./view-preferences-editor.component.scss'],
    providers: [ViewPreferenceEditorService],
    standalone: false
})
export class ViewPreferencesEditorComponent extends BasePendingChangesComponent implements OnInit {
    @ViewChild('descriptionTemplateSelect') descriptionTemplateSelect: SingleAutoCompleteComponent;
    @ViewChild('planBlueprintSelect') planBlueprintSelect: SingleAutoCompleteComponent;
	orderedPlanPreferencesList:ReferenceType[] = [];
	orderedDescriptionTemplateList:ReferenceType[] = [];
	
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
    reorderAssistiveText = computed(() => this.dragAndDropService.assistiveTextSignal());
    get reorderMode(){
        return this.dragAndDropService.reorderMode;
    }
	constructor(
		protected dialog: MatDialog,
		private uiNotificationService: UiNotificationService,
		public language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
		protected formService: FormService,
		private logger: LoggingService,
		private tenantConfigurationService: TenantConfigurationService,
		public viewPreferenceEditorService: ViewPreferenceEditorService,
		private referenceTypeService: ReferenceTypeService,
        private dragAndDropService: DragAndDropAccessibilityService
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
		this.tenantConfigurationService.getType(TenantConfigurationType.ViewPreferences, ViewPreferencesEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					try {
						this.orderedPlanPreferencesList = data?.viewPreferences?.planPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
						this.orderedDescriptionTemplateList = data?.viewPreferences?.descriptionPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
						this.editorModel = data?.viewPreferences? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
						this.buildForm();
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
		this.viewPreferenceEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
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

	afterPlanReferenceTypeUpdated(result: ReferenceType[]) {
		this.orderedPlanPreferencesList = result;
	}

	afterDescriptionReferenceTypeUpdated(result: ReferenceType[]) {
		this.orderedDescriptionTemplateList = result;
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
		this.orderedPlanPreferencesList = [];
		this.orderedDescriptionTemplateList = [];
		this.prepareForm(null);
	}


	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

}
