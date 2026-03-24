import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { PlanBlueprintTypeEditorService } from './plan-blueprint-type-editor.service';
import { PlanBlueprintType, PlanBlueprintTypePersist } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { PlanBlueprintTypeEditorModel } from './plan-blueprint-type-editor.model';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { PlanBlueprintTypeEditorResolver } from './plan-blueprint-type-editor.resolver';
import { PlanBlueprintTypeStatus } from '@app/core/common/enum/plan-blueprint-type-status';

@Component({
    templateUrl: './plan-blueprint-type-editor.component.html',
    styleUrls: ['./plan-blueprint-type-editor.component.scss'],
    providers: [PlanBlueprintTypeEditorService],
    standalone: false
})
export class PlanBlueprintTypeEditorComponent extends BaseEditor<PlanBlueprintTypeEditorModel, PlanBlueprintType> implements OnInit {

	isNew = true;
	isDeleted = false;
	isFinalized = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;


	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeletePlanBlueprintType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && !this.isFinalized && this.hasPermission(this.authService.permissionEnum.EditPlanBlueprintType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && !this.isFinalized && this.hasPermission(this.authService.permissionEnum.EditPlanBlueprintType) && this.editorModel.belongsToCurrentTenant != false;
	}


	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
	}

	constructor(
		// BaseFormEditor injected dependencies
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
		// Rest dependencies. Inject any other needed deps here:
		public enumUtils: EnumUtils,
		private planBlueprintTypeService: PlanBlueprintTypeService,
		private logger: LoggingService,
		private planBlueprintTypeEditorService: PlanBlueprintTypeEditorService,
		public titleService: Title,
		protected routerUtils: RouterUtilsService
	) {

		const descriptionLabel: string = route.snapshot.data['entity']?.name;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('PLAN-BLUEPRINT-TYPE-EDITOR.TITLE-EDIT-PLAN-BLUEPRINT-TYPE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: PlanBlueprintType) => void) {
		this.planBlueprintTypeService.getSingle(itemId, PlanBlueprintTypeEditorResolver.lookupFields())
			.pipe(map(data => data as PlanBlueprintType), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: PlanBlueprintType) {
		try {
			this.editorModel = data ? new PlanBlueprintTypeEditorModel().fromModel(data) : new PlanBlueprintTypeEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.isFinalized = data ? data.status === PlanBlueprintTypeStatus.Finalized : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse planBlueprintType item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditPlanBlueprintType));
		this.planBlueprintTypeEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);

		if (this.isFinalized) {
			this.formGroup.disable();
		}
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: PlanBlueprintType) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/plan-blueprint-type')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as PlanBlueprintTypePersist;

		this.planBlueprintTypeService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				// for each state navigate to listing page
				complete => {
					this.onCallbackSuccess();
				},
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistEntity();
	}

	public delete() {
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
					this.planBlueprintTypeService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackDeleteSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	finalize(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			this.dialog.open(FormValidationErrorsDialogComponent, {
				data: {
					errorMessages: [this.language.instant('PLAN-BLUEPRINT-TYPE-EDITOR.MESSAGES.MISSING-FIELDS')]
				}
			})

			this.formService.touchAllFormFields(this.formGroup);
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('PLAN-BLUEPRINT-TYPE-EDITOR.FINALIZE-DIALOG.TITLE'),
				confirmButton: this.language.instant('PLAN-BLUEPRINT-TYPE-EDITOR.FINALIZE-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('PLAN-BLUEPRINT-TYPE-EDITOR.FINALIZE-DIALOG.NEGATIVE'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.formGroup.get('status').setValue(PlanBlueprintTypeStatus.Finalized);
				this.formSubmit();
			}
		});
	}
}
