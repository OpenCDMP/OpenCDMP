import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router, ActivatedRoute } from '@angular/router';
import { AppRole } from '@app/core/common/enum/app-role';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { UiNotificationService, SnackBarNotificationLevel } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { map, takeUntil } from 'rxjs';
import { DescriptionStatusForm, DescriptionStatusDefinitionAuthorizationItemForm, DescriptionStatusEditorModel } from './description-status-editor.model';
import { DescriptionStatusEditorResolver } from './description-status-editor.resolver';
import { BaseEditor } from '@common/base/base-editor';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { DescriptionStatusAvailableActionType } from '@app/core/common/enum/description-status-available-action-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionStatusPersist } from '@app/core/model/description-status/description-status-persist';
import { CssColorsEditorService } from '@app/ui/admin/tenant-configuration/editor/css-colors/css-colors-editor.service';

@Component({
    selector: 'app-description-status-editor',
    templateUrl: './description-status-editor.component.html',
    styleUrl: './description-status-editor.component.scss',
    providers: [CssColorsEditorService],
    standalone: false
})
export class DescriptionStatusEditorComponent extends BaseEditor<DescriptionStatusEditorModel, DescriptionStatus> implements OnInit{
    protected internalStatusEnum = this.enumUtils.getEnumValues<DescriptionStatusEnum>(DescriptionStatusEnum);
    protected userRolesEnum = this.enumUtils.getEnumValues<AppRole>(AppRole);
    protected planRolesEnum = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	protected descriptionStatusAvailableActionTypeEnumValues = this.enumUtils.getEnumValues<DescriptionStatusAvailableActionType>(DescriptionStatusAvailableActionType);
    protected belongsToCurrentTenant: boolean;
    initialFile: StorageFile = null;
	isUsingDropzone: boolean = true; 

    constructor(
        protected enumUtils: EnumUtils,
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
        private analyticsService: AnalyticsService,
        private descriptionStatusService: DescriptionStatusService,
        private logger: LoggingService,
        private routerUtils: RouterUtilsService,
		private cssColorsEditorService: CssColorsEditorService
    ){
        super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
    }

    formGroup: FormGroup<DescriptionStatusForm>;
    
    ngOnInit(){
        this.analyticsService.trackPageView(AnalyticsService.DescriptionStatusEditor);
        super.ngOnInit();
    }

    get editAuthenticationForm(): FormGroup<DescriptionStatusDefinitionAuthorizationItemForm> {
        return this.formGroup?.controls?.definition?.controls?.authorization?.controls?.edit;
    }

    getItem(itemId: Guid, successFunction: (item: DescriptionStatus) => void): void {
        this.descriptionStatusService.getSingle(itemId, DescriptionStatusEditorResolver.lookupFields())
			.pipe(map(data => data as DescriptionStatus), takeUntil(this._destroyed))
			.subscribe({
                next: (data) => successFunction(data),
                error: (error) => this.onCallbackError(error)
            });
    }

    prepareForm(data: DescriptionStatus): void {
        try {
			this.editorModel = data ? new DescriptionStatusEditorModel().fromModel(data) : new DescriptionStatusEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
            this.belongsToCurrentTenant = data?.belongsToCurrentTenant;
			this.initialFile = data?.definition?.storageFile;
			this.buildForm();
			this.bindColorInputs();
		} catch (error) {
			this.logger.error('Could not parse descriptionStatus item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
    }

    buildForm(): void {
        this.formGroup = this.editorModel.buildForm({disabled: !this.isNew && (!this.belongsToCurrentTenant || this.isDeleted || !this.authService.hasPermission(AppPermission.EditDescriptionStatus))});
		this.cssColorsEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		
		// if ((this.formGroup?.controls.definition.controls.storageFileId.value != undefined)) {
			

		// 	const fields = [
		// 		nameof<StorageFile>(x => x.id),
		// 		nameof<StorageFile>(x => x.name),
		// 		nameof<StorageFile>(x => x.extension),
		// 	]
		// 	this.storageFileService.getSingle(this.formGroup?.controls.definition.controls.storageFileId.value, fields).pipe(takeUntil(this._destroyed)).subscribe(storageFile => {
		// 		this.createFileNameDisplay(storageFile.name, storageFile.extension);
		// 	});
		
		// }
	
	}

    formSubmit(): void {
        this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistEntity();
    }

    delete(): void {
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
					this.descriptionStatusService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe({
							complete: () => this.onCallbackDeleteSuccess(),
							error: (error) => this.onCallbackError(error)
                        });
				}
			});
		}
    }

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: DescriptionStatus) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
        if (this.isNew) {
			let route = [];
			route.push(this.routerUtils.generateUrl('/description-statuses/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}
    
	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formGroup.value as DescriptionStatusPersist;

		if (this.isUsingDropzone) formData.definition.matIconName = null;
		else formData.definition.storageFileId = null;

		this.descriptionStatusService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: (complete) => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
                error: (error) => this.onCallbackError(error)
            });
	}

    toggleInputMethod(): void {
        this.isUsingDropzone = !this.isUsingDropzone; 
    }


    protected get canSave(): boolean {
        const editDescriptionStatus = this.authService.permissionEnum.EditDescriptionStatus;
		return (this.isNew || (this.belongsToCurrentTenant && !this.isDeleted)) && this.authService.hasPermission(editDescriptionStatus);
	}

    protected get canDelete(): boolean {
        const deletedDescriptionStatus = this.authService.permissionEnum.DeleteDescriptionStatus;
        return this.belongsToCurrentTenant && !this.isNew && !this.isDeleted && this.authService.hasPermission(deletedDescriptionStatus);
    }

	private bindColorInputs() {
		this.formGroup?.controls?.definition?.controls?.statusColor?.valueChanges.subscribe((color) => {
			this.formGroup?.controls?.definition?.controls?.statusColor.setValue(color, {
				emitEvent: false,
			});
		});
		this.formGroup?.controls?.definition?.controls?.statusColor?.valueChanges.subscribe((color) =>
			this.formGroup?.controls?.definition?.controls?.statusColor?.setValue(color, {
				emitEvent: false,
			})
		);
	}
}
