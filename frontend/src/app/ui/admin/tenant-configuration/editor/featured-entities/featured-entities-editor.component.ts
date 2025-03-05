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
import { TenantConfigurationEditorModel } from './featured-entities-editor.model';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { FeaturedEntitiesEditorService } from './featured-entities-editor.service';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { FeaturedEntitiesEditorResolver } from './featured-entities-editor.resolver';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';

@Component({
    selector: 'app-tenant-configuration-featured-entities-editor',
    templateUrl: './featured-entities-editor.component.html',
    styleUrls: ['./featured-entities-editor.component.scss'],
    providers: [FeaturedEntitiesEditorService],
    standalone: false
})
export class FeaturedEntitiesEditorComponent extends BasePendingChangesComponent implements OnInit {
    @ViewChild('descriptionTemplateSelect') descriptionTemplateSelect: SingleAutoCompleteComponent;
    @ViewChild('planBlueprintSelect') planBlueprintSelect: SingleAutoCompleteComponent;
	orderedPlanBlueprintList:PlanBlueprint[] = [];
	orderedDescriptionTemplateList:DescriptionTemplate[] = [];
	
	get editorModel(): TenantConfigurationEditorModel { return this._editorModel; }
	set editorModel(value: TenantConfigurationEditorModel) { this._editorModel = value; }
	private _editorModel: TenantConfigurationEditorModel;

    get planBlueprintConfig(): SingleAutoCompleteConfiguration {
        return {
            initialItems: (data?: any) => this.planBlueprintService.query(this.planBlueprintService.buildPlanBlueprintGroupAutocompleteLookup({
                isActive: [IsActive.Active],
                excludedGroupIds: this.formGroup.get('featuredEntities').get('planBlueprints')?.value?.map((x) => x.groupId)
            })).pipe(map(x => x.items)),
            filterFn: (searchQuery: string, data?: any) => this.planBlueprintService.query(this.planBlueprintService.buildPlanBlueprintGroupAutocompleteLookup({
                isActive: [IsActive.Active], 
                like: searchQuery,
                excludedGroupIds: this.formGroup.get('featuredEntities').get('planBlueprints')?.value?.map((x) => x.groupId)
            })).pipe(map(x => x.items)),
            getSelectedItem: null,
            displayFn: (item: PlanBlueprint) => null,
            titleFn: (item: PlanBlueprint) => item.label,
            subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' '+ item.version,
            valueAssign: (item: PlanBlueprint) => null,
        };
    }
	planBlueprintAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
    
	descriptionTemplateAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
        initialItems: (data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
            isActive: [IsActive.Active],
            excludedGroupIds: this.formGroup.get('featuredEntities').get('descriptionTemplates')?.value?.map((x) => x.groupId)
        })).pipe(map(x => x.items)),
        filterFn: (searchQuery: string, data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
            isActive: [IsActive.Active],
            like: searchQuery,
            excludedGroupIds: this.formGroup.get('featuredEntities').get('descriptionTemplates')?.value?.map((x) => x.groupId)
        })).pipe(map(x => x.items)),
        getSelectedItem: (selectedItem: any) => null,
        displayFn: (item: DescriptionTemplate) => null,
        titleFn: (item: DescriptionTemplate) => item.label,
        subtitleFn: (item: DescriptionTemplate) => item.description,
        valueAssign: (item: DescriptionTemplate) => null,
        popupItemActionIcon: 'visibility'
    };

    onPreviewDescriptionTemplate(event: DescriptionTemplate) {
        const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
            width: '590px',
            minHeight: '200px',
            restoreFocus: false,
            data: {
                descriptionTemplateId: event.id
            },
            panelClass: 'custom-modalbox'
        }).afterClosed().pipe(takeUntil(this._destroyed))
        .subscribe((descTemplate) => {
            if (descTemplate) {
                this.addDescriptionTemplate(descTemplate);
            }
        })
    }

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
		private language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
		protected formService: FormService,
		private logger: LoggingService,
		private tenantConfigurationService: TenantConfigurationService,
		public featuredEntitiesEditorService: FeaturedEntitiesEditorService,
		private planBlueprintService: PlanBlueprintService,
		private descriptionTemplateService: DescriptionTemplateService,
        private dragAndDropService: DragAndDropAccessibilityService
	) {
		super();
        this.planBlueprintAutoCompleteConfiguration = this.planBlueprintConfig;
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	ngOnInit(): void {
		this.getExistingSelections();
	}

	getExistingSelections() {
		this.tenantConfigurationService.getType(TenantConfigurationType.FeaturedEntities, FeaturedEntitiesEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					try {
						this.orderedPlanBlueprintList = data?.featuredEntities?.planBlueprints || [];
						this.orderedDescriptionTemplateList = data?.featuredEntities?.descriptionTemplates || [];
						this.editorModel = data?.featuredEntities? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
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
		this.featuredEntitiesEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
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

	addPlanBlueprint(event: PlanBlueprint) {
		if (event?.groupId) {
			const formArray = this.formGroup.get('featuredEntities').get('planBlueprints') as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (event.groupId == formArray.at(j).get('groupId').getRawValue()) {
					return;
				}
			}
			
			formArray.push(this.editorModel.createPlanBlueprint(formArray.length, event.groupId));
			this.orderedPlanBlueprintList.push(event);
            this.planBlueprintSelect.clearValue(false);
		}
	}

	removePlanBlueprint(item: PlanBlueprint) {
		if (item?.groupId) {
			const orderedListIndex = this.orderedPlanBlueprintList.indexOf(item, 0);
			if (orderedListIndex > -1) {
				this.orderedPlanBlueprintList.splice(orderedListIndex, 1);
			}
	
			const formArray = this.formGroup.get('featuredEntities').get('planBlueprints') as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (item.groupId == formArray.at(j).get('groupId').getRawValue()) {
					formArray.removeAt(j);
				}
			}
            this.planBlueprintSelect.clearValue(false);
		}
	}

	droppedPlanBlueprints(event: {previousIndex: number, currentIndex: number}) {
		const formArray = (this.formGroup.get('featuredEntities').get('planBlueprints') as FormArray);

		moveItemInArray(formArray.controls, event.previousIndex, event.currentIndex);
		formArray.updateValueAndValidity();
		formArray.controls.forEach((planBlueprint, index) => {
			planBlueprint.get('ordinal').setValue(index);
		});

		moveItemInArray(this.orderedPlanBlueprintList, event.previousIndex, event.currentIndex);
	}

	addDescriptionTemplate(event: DescriptionTemplate) {
		if (event?.groupId) {
			const formArray = this.formGroup.get('featuredEntities').get('descriptionTemplates') as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (event.groupId == formArray.at(j).get('groupId').getRawValue()) {
					return;
				}
			}
			
			formArray.push(this.editorModel.createDescriptionTemplate(formArray.length, event.groupId));
			this.orderedDescriptionTemplateList.push(event);
            this.descriptionTemplateSelect.clearValue(false);
		}
	}

	removeDescriptionTemplate(item: DescriptionTemplate) {
		if (item?.groupId) {
			const orderedListIndex = this.orderedDescriptionTemplateList.indexOf(item, 0);
			if (orderedListIndex > -1) {
				this.orderedDescriptionTemplateList.splice(orderedListIndex, 1);
			}
	
			const formArray = this.formGroup.get('featuredEntities').get('descriptionTemplates') as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (item.groupId == formArray.at(j).get('groupId').getRawValue()) {
					formArray.removeAt(j);
				}
			}
            this.descriptionTemplateSelect.clearValue(false);
		}
	}

	droppedDescriptionTemplates(event: {previousIndex: number, currentIndex: number}) {
		const formArray = (this.formGroup.get('featuredEntities').get('descriptionTemplates') as FormArray);

		moveItemInArray(formArray.controls, event.previousIndex, event.currentIndex);
		formArray.updateValueAndValidity();
		formArray.controls.forEach((planBlueprint, index) => {
			planBlueprint.get('ordinal').setValue(index);
		});

		moveItemInArray(this.orderedDescriptionTemplateList, event.previousIndex, event.currentIndex);
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
		this.orderedPlanBlueprintList = [];
		this.orderedDescriptionTemplateList = [];
		this.prepareForm(null);
	}


	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

    onBlueprintKeyDown($event: KeyboardEvent, index: number, blueprint: PlanBlueprint) {
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            listLength: this.orderedDescriptionTemplateList.length,
            itemName: blueprint.label,
            moveDownFn: () => {
                this.droppedPlanBlueprints({previousIndex: index, currentIndex: index + 1});
                setTimeout(() => document.getElementById(blueprint.id.toString())?.focus());
            },
            moveUpFn: () => {
                this.droppedPlanBlueprints({previousIndex: index, currentIndex: index - 1});
                setTimeout(() => document.getElementById(blueprint.id.toString())?.focus());
            }
        })
    }

    onDescTemplateKeyDown($event: KeyboardEvent, index: number, descriptionTemplate: DescriptionTemplate) {
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            listLength: this.orderedDescriptionTemplateList.length,
            itemName: descriptionTemplate.label,
            moveDownFn: () => {
                this.droppedDescriptionTemplates({previousIndex: index, currentIndex: index + 1});
                setTimeout(() => document.getElementById(descriptionTemplate.id.toString())?.focus());
            },
            moveUpFn: () => {
                this.droppedDescriptionTemplates({previousIndex: index, currentIndex: index - 1});
                setTimeout(() => document.getElementById(descriptionTemplate.id.toString())?.focus());
            }
        })
    }
}
