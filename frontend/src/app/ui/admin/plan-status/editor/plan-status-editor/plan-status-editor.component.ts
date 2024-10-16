import { Component, OnInit } from '@angular/core';
import { PlanStatusDefinitionAuthorizationForm, PlanStatusDefinitionAuthorizationItemForm, PlanStatusEditorModel, PlanStatusForm } from './plan-status-editor.model';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { BaseEditor } from '@common/base/base-editor';
import { Guid } from '@common/types/guid';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { MatDialog } from '@angular/material/dialog';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TranslateService } from '@ngx-translate/core';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { PlanStatusEditorResolver } from '../plan-status-editor.resolver';
import { map, takeUntil } from 'rxjs';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { PlanStatusPersist } from '@app/core/model/plan-status/plan-status-persist';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { FormGroup } from '@angular/forms';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { AppRole } from '@app/core/common/enum/app-role';

@Component({
  selector: 'app-plan-status-editor',
  templateUrl: './plan-status-editor.component.html',
  styleUrl: './plan-status-editor.component.scss'
})
export class PlanStatusEditorComponent extends BaseEditor<PlanStatusEditorModel, PlanStatus> implements OnInit{

    protected internalStatusEnum = this.enumUtils.getEnumValues<PlanStatusEnum>(PlanStatusEnum);
    protected userRolesEnum = this.enumUtils.getEnumValues<AppRole>(AppRole);
    protected planRolesEnum = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
    protected belongsToCurrentTenant: boolean;

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
        private planStatusService: PlanStatusService,
        private logger: LoggingService,
        private routerUtils: RouterUtilsService,
    ){
        super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
    }

    formGroup: FormGroup<PlanStatusForm>;
    
    ngOnInit(){
        this.analyticsService.trackPageView(AnalyticsService.PlanStatusEditor);
        super.ngOnInit();
    }

    get editAuthenticationForm(): FormGroup<PlanStatusDefinitionAuthorizationItemForm> {
        return this.formGroup?.controls?.definition?.controls?.authorization?.controls?.edit;
    }

    getItem(itemId: Guid, successFunction: (item: PlanStatus) => void): void {
        this.planStatusService.getSingle(itemId, PlanStatusEditorResolver.lookupFields())
			.pipe(map(data => data as PlanStatus), takeUntil(this._destroyed))
			.subscribe({
                next: (data) => successFunction(data),
                error: (error) => this.onCallbackError(error)
            });
    }

    prepareForm(data: PlanStatus): void {
        try {
			this.editorModel = data ? new PlanStatusEditorModel().fromModel(data) : new PlanStatusEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
            this.belongsToCurrentTenant = data?.belongsToCurrentTenant;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse planStatus item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
    }

    buildForm(): void {
        this.formGroup = this.editorModel.buildForm({disabled: !this.isNew && (!this.belongsToCurrentTenant || this.isDeleted || !this.authService.hasPermission(AppPermission.EditPlanStatus))});
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
					this.planStatusService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe({
							complete: () => this.onCallbackDeleteSuccess(),
							error: (error) => this.onCallbackError(error)
                        });
				}
			});
		}
    }

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: PlanStatus) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
        if (this.isNew) {
			let route = [];
			route.push(this.routerUtils.generateUrl('/plan-statuses/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}
    
	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formGroup.value as PlanStatusPersist;

		this.planStatusService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe({
				next: (complete) => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
                error: (error) => this.onCallbackError(error)
            });
	}

    protected get canSave(): boolean {
        const editPlanStatus = this.authService.permissionEnum.EditPlanStatus;
		return (this.isNew || (this.belongsToCurrentTenant && !this.isDeleted)) && this.authService.hasPermission(editPlanStatus);
	}

    protected get canDelete(): boolean {
        const deletedPlanStatus = this.authService.permissionEnum.DeletePlanStatus;
        return this.belongsToCurrentTenant && !this.isNew && !this.isDeleted && this.authService.hasPermission(deletedPlanStatus);
    }
}
