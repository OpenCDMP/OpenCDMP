import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, OnInit, signal } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationTrackingProcess } from '@notification-service/core/enum/notification-tracking-process.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TenantConfigurationEditorModel } from './notifier-list-editor.model';
import { TenantConfiguration, TenantConfigurationPersist } from '@notification-service/core/model/tenant-configuration';
import { TenantConfigurationService } from '@notification-service/services/http/tenant-configuration.service';
import { TenantConfigurationType } from '@notification-service/core/enum/tenant-configuration-type';
import { NotifierListEditorResolver } from './notifier-list-editor.resolver';
import { NotifierListEditorService } from './notifier-list-editor.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';

@Component({
    selector: 'app-tenant-configuration-notifier-list-editor',
    templateUrl: './notifier-list-editor.component.html',
    styleUrls: ['./notifier-list-editor.component.scss'],
    providers: [NotifierListEditorService],
    standalone: false
})
export class NotifierListEditorComponent extends BasePendingChangesComponent implements OnInit {

	availableNotifiers: { [key: string]: NotificationContactType[] } = {};
	availableNotifiersKeys: NotificationType[];

	get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
	set editorModel(value: TenantConfigurationEditorModel) { this._editorModel = value; }
	private _editorModel: TenantConfigurationEditorModel;

	notificationTrackingProcess: NotificationTrackingProcess = NotificationTrackingProcess.PENDING;

	isNew = true;
	formGroup: UntypedFormGroup = null;

    reorderAssistiveText = computed(() => this.dragAndDropService.assistiveTextSignal());
    get reorderMode(){
        return this.dragAndDropService.reorderMode;
    }
    
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
		public notificationServiceEnumUtils: NotificationServiceEnumUtils,
		public notifierListEditorService: NotifierListEditorService,
        private dragAndDropService: DragAndDropAccessibilityService
	) {
		super();
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.getConfiguration();
	}

	getConfiguration() {
		this.formGroup = null;
		this.tenantConfigurationService.getNotifierList(new NotifierListLookup())
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => {
					try {
						this.availableNotifiers = data.notifiers;
						this.availableNotifiersKeys = Object.keys(this.availableNotifiers) as NotificationType[];
						this.getExistingSelections();
					} catch {
						this.notificationTrackingProcess = NotificationTrackingProcess.ERROR;
						this.logger.error('Could not parse Description: ' + data);
						this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
					}
				},
				error => this.onCallbackError(error)
			);
	}

	getExistingSelections() {
		this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.NotifierList, NotifierListEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					try {
						if (data?.notifierList?.notifiers) {
							this.orderAvailableItemsbasedOnExistingSelections(data);
						}
						this.editorModel = data?.notifierList?.notifiers ? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
						this.buildForm();
					} catch {
						this.notificationTrackingProcess = NotificationTrackingProcess.ERROR;
						this.logger.error('Could not parse Description: ' + data);
						this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
					}
				},
				error => this.onCallbackError(error)
			);
	}

	orderAvailableItemsbasedOnExistingSelections(existingSelections: TenantConfiguration) {
		if (!existingSelections?.notifierList?.notifiers) { return; }
		this.availableNotifiersKeys.forEach(key => {
			const orderedList = [];
			orderedList.push(...(existingSelections.notifierList.notifiers[key] || []).filter(x => this.availableNotifiers[key].includes(x))); // First push the selected ordered values.
			orderedList.push(...this.availableNotifiers[key].filter(x => !orderedList.includes(x))); //Then push the rest items.
			this.availableNotifiers[key] = orderedList;
		});
	}

	dropped(event: CdkDragDrop<string[]>, type: NotificationType) {
		moveItemInArray(this.availableNotifiers[type], event.previousIndex, event.currentIndex);
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
		this.formGroup = this.editorModel.buildForm(this.availableNotifiersKeys, this.availableNotifiers,  null, !this.authService.hasPermission(AppPermission.EditTenantConfiguration));
		this.notifierListEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getConfiguration();
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as TenantConfigurationPersist;

		// Clear empty or null selections.
		if (formData?.notifierList?.notifiers) {
			Object.keys(formData.notifierList.notifiers).forEach(key => {
				if (formData.notifierList.notifiers[key] == null || formData.notifierList.notifiers[key].length === 0) { delete formData.notifierList.notifiers[key]; }
			});
		}
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

    onKeyDown($event: KeyboardEvent, index: number, type: NotificationType, item: string) {
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            listLength: this.availableNotifiers[type].length,
            itemName: item,
            moveDownFn: () => {
                moveItemInArray(this.availableNotifiers[type], index, index + 1);
                document.getElementById(`${type}-${index + 1}`)?.focus();
            },
            moveUpFn: () => {
                moveItemInArray(this.availableNotifiers[type], index, index - 1);
                document.getElementById(`${type}-${index - 1}`)?.focus();
            }
        })
    }
}
