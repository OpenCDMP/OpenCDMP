import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { DescriptionWorkflow } from '@app/core/model/workflow/description-workflow';
import { DescriptionWorkflowPersist } from '@app/core/model/workflow/description-workflow-persist';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { DescriptionWorkflowService } from '@app/core/services/description-workflow/description-workflow.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { UiNotificationService, SnackBarNotificationLevel } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService, HttpError } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil, Observable, map } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { DescriptionWorkflowForm, DescriptionWorkflowEditorModel, DescriptionWorkflowDefinitionForm } from '../description-workflow-editor.model';
import { DescriptionWorkflowEditorResolver } from '../description-workflow-editor.resolver';

@Component({
  selector: 'app-description-workflow-editor',
  templateUrl: './description-workflow-editor.component.html',
  styleUrl: './description-workflow-editor.component.scss'
})
export class DescriptionWorkflowEditorComponent extends BasePendingChangesComponent implements OnInit{
    formGroup: FormGroup<DescriptionWorkflowForm>;
    editorModel: DescriptionWorkflowEditorModel;

    constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,
        protected enumUtils: EnumUtils,
		private logger: LoggingService,
		private descriptionWorkflowService: DescriptionWorkflowService,
		private analyticsService: AnalyticsService,
        private descriptionStatusService: DescriptionStatusService
	) {
		super();
	}

    ngOnInit(): void {
        this.analyticsService.trackPageView(AnalyticsService.DescriptionWorkflowEditor);
		this.getItem((entity) => {
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		});
    }

    protected getItem(successFunction: (item: DescriptionWorkflow) => void) {
		this.descriptionWorkflowService.getCurrent(DescriptionWorkflowEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed))
			.subscribe({
                next: (data) => successFunction(data),
                error: (error) => this.onCallbackError(error)
            });
	}

    protected prepareForm(data: DescriptionWorkflow) {
		try {
			this.editorModel = data ? new DescriptionWorkflowEditorModel().fromModel(data) : new DescriptionWorkflowEditorModel();
			this.formGroup = this.editorModel.buildForm({disabled: !this.authService.hasPermission(AppPermission.EditDescriptionWorkflow)});
            
		} catch (error) {
			this.logger.error('Could not parse DescriptionWorkflow item: ' + data + error);
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
		return this.editorModel?.id && this.authService.hasPermission(this.authService.permissionEnum.DeleteDescriptionWorkflow);
	}

	protected get canSave(): boolean {
		return this.formGroup.touched && this.authService.hasPermission(this.authService.permissionEnum.EditDescriptionWorkflow);
	}

    protected get definitionForm(): FormGroup<DescriptionWorkflowDefinitionForm> {
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
        const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as DescriptionWorkflowPersist;

    

		this.descriptionWorkflowService.persist(formData)
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
					this.descriptionWorkflowService.delete(value.id).pipe(takeUntil(this._destroyed))
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

    private descriptionStatusLookupFields = [
        nameof<DescriptionStatus>(x => x.id),
        nameof<DescriptionStatus>(x => x.name),
    ]

    descriptionStatusAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.descriptionStatusService.query(
            this.descriptionStatusService.buildLookup({
                size: 20, 
                lookupFields: this.descriptionStatusLookupFields,
                excludedIds: excludedItems
            })).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.descriptionStatusService.query(
            this.descriptionStatusService.buildLookup({
                size: 20, 
                like: searchQuery, 
                lookupFields: this.descriptionStatusLookupFields
            })
        ).pipe(map(x => x.items)),
		getSelectedItem: (id: Guid) => this.descriptionStatusService.getSingle(id, this.descriptionStatusLookupFields),
		displayFn: (item: DescriptionStatus) => item.name,
        titleFn: (item: DescriptionStatus) => item.name,
		valueAssign: (item: DescriptionStatus) => {this.formGroup.markAsTouched(); return item.id},
	};
}

