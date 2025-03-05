import { Component, OnInit } from '@angular/core';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { map, Observable, takeUntil } from 'rxjs';
import { PlanWorkflowDefinitionForm, PlanWorkflowEditorModel, PlanWorkflowForm } from '../plan-workflow-editor.model';
import { FormGroup } from '@angular/forms';
import { PlanWorkflowService } from '@app/core/services/plan/plan-workflow.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { PlanWorkflow } from '@app/core/model/workflow/plan-workflow';
import { PlanWorkflowEditorResolver } from '../plan-workflow-editor.resolver';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { FormService } from '@common/forms/form-service';
import { TranslateService } from '@ngx-translate/core';
import { PlanWorkflowPersist } from '@app/core/model/workflow/plan-workflow-persist';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { nameof } from 'ts-simple-nameof';
import { Guid } from '@common/types/guid';

@Component({
    selector: 'app-plan-workflow-editor',
    templateUrl: './plan-workflow-editor.component.html',
    styleUrl: './plan-workflow-editor.component.scss',
    standalone: false
})
export class PlanWorkflowEditorComponent extends BasePendingChangesComponent implements OnInit{
    formGroup: FormGroup<PlanWorkflowForm>;
    editorModel: PlanWorkflowEditorModel;

    constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,
        protected enumUtils: EnumUtils,
		private logger: LoggingService,
		private planWorkflowService: PlanWorkflowService,
		private analyticsService: AnalyticsService,
        private planStatusService: PlanStatusService
	) {
		super();
	}

    ngOnInit(): void {
        this.analyticsService.trackPageView(AnalyticsService.PlanWorkflowEditor);
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
    }

    protected getItem(successFunction: (item: PlanWorkflow) => void) {
		this.planWorkflowService.getCurrent(PlanWorkflowEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed))
			.subscribe({
                next: (data) => successFunction(data),
                error: (error) => this.onCallbackError(error)
            });
	}

    protected prepareForm(data: PlanWorkflow) {
		try {
			this.editorModel = data ? new PlanWorkflowEditorModel().fromModel(data) : new PlanWorkflowEditorModel();
			this.formGroup = this.editorModel.buildForm({disabled: !this.authService.hasPermission(AppPermission.EditPlanWorkflow)});
            
		} catch (error) {
			this.logger.error('Could not parse PlanWorkflow item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

    protected refreshData(): void {
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
	}

    
    protected get canDelete(): boolean {
		return this.editorModel?.id && this.authService.hasPermission(this.authService.permissionEnum.DeletePlanWorkflow);
	}

	protected get canSave(): boolean {
		return this.formGroup.touched && this.authService.hasPermission(this.authService.permissionEnum.EditPlanWorkflow);
	}

    protected get definitionForm(): FormGroup<PlanWorkflowDefinitionForm> {
        return this.formGroup?.controls?.definition;
    }

	protected onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse)

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		}
	}

	protected onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.refreshData();
	}

	formSubmit(): void {
		this.editorModel.validationErrorModel.clear();        
		this.formService.removeAllBackEndErrors(this.formGroup);

		this.formService.touchAllFormFields(this.formGroup);
        this.formService.validateAllFormFields(this.formGroup);

		if (!this.formGroup?.valid) {
			return;
		}

		this.persistEntity();
	}

    persistEntity(): void {
        const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as PlanWorkflowPersist;

    

		this.planWorkflowService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe({				
                complete: () => this.onCallbackSuccess(),
				error: (error) => this.onCallbackError(error)
            });
	}

    delete() {
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
					this.planWorkflowService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe({
							complete: () => {
                                this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-RESET'), SnackBarNotificationLevel.Success);
		                        this.prepareForm(null);
                            },
							error: (error) => this.onCallbackError(error)
						});
				}
			});
		}
	}

    addStatusTransition(){
        const index = this.formGroup.controls.definition.controls.statusTransitions.length;
        this.formGroup.controls.definition.controls.statusTransitions.push(this.editorModel.buildStatusTransitionForm(index));
        this.formGroup.markAsTouched();
    }

    removeStatusTransition(index: number){
        this.formGroup.controls.definition.controls.statusTransitions.removeAt(index);
        this.editorModel.reApplyDefinitionValidators(this.formGroup.controls.definition);
        this.formGroup.markAsTouched();
    }

    canDeactivate(): boolean | Observable<boolean> {
        return this.formGroup ? !this.formGroup.dirty : true;
    }

    private planStatusLookupFields = [
        nameof<PlanStatus>(x => x.id),
        nameof<PlanStatus>(x => x.name),
    ]

    planStatusAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.planStatusService.query(
            this.planStatusService.buildLookup({
                size: 20, 
                lookupFields: this.planStatusLookupFields,
                excludedIds: excludedItems
            })).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planStatusService.query(
            this.planStatusService.buildLookup({
                size: 20, 
                like: searchQuery, 
                lookupFields: this.planStatusLookupFields
            })
        ).pipe(map(x => x.items)),
		getSelectedItem: (id: Guid) => this.planStatusService.getSingle(id, this.planStatusLookupFields),
		displayFn: (item: PlanStatus) => item.name,
        titleFn: (item: PlanStatus) => item.name,
		valueAssign: (item: PlanStatus) => {this.formGroup.markAsTouched(); return item.id},
	};
}
