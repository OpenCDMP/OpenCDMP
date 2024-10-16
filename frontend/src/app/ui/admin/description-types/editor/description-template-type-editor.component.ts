import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateType, DescriptionTemplateTypePersist } from '@app/core/model/description-template-type/description-template-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
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
import { DescriptionTemplateTypeEditorModel } from './description-template-type-editor.model';
import { DescriptionTemplateTypeEditorResolver } from './description-template-type-editor.resolver';
import { DescriptionTemplateTypeEditorService } from './description-template-type-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';

@Component({
	templateUrl: './description-template-type-editor.component.html',
	styleUrls: ['./description-template-type-editor.component.scss'],
	providers: [DescriptionTemplateTypeEditorService]
})
export class DescriptionTemplateTypeEditorComponent extends BaseEditor<DescriptionTemplateTypeEditorModel, DescriptionTemplateType> implements OnInit {

	isNew = true;
	isDeleted = false;
	isFinalized = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;


	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteDescriptionTemplateType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && !this.isFinalized && this.hasPermission(this.authService.permissionEnum.EditDescriptionTemplateType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && !this.isFinalized && this.hasPermission(this.authService.permissionEnum.EditDescriptionTemplateType) && this.editorModel.belongsToCurrentTenant != false;
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
		private descriptionTemplateTypeService: DescriptionTemplateTypeService,
		private logger: LoggingService,
		private descriptionTemplateTypeEditorService: DescriptionTemplateTypeEditorService,
		public titleService: Title,
		protected routerUtils: RouterUtilsService
	) {

		const descriptionLabel: string = route.snapshot.data['entity']?.name;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('DESCRIPTION-TEMPLATE-TYPE-EDITOR.TITLE-EDIT-DESCRIPTION-TEMPLATE-TYPE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: DescriptionTemplateType) => void) {
		this.descriptionTemplateTypeService.getSingle(itemId, DescriptionTemplateTypeEditorResolver.lookupFields())
			.pipe(map(data => data as DescriptionTemplateType), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: DescriptionTemplateType) {
		try {
			this.editorModel = data ? new DescriptionTemplateTypeEditorModel().fromModel(data) : new DescriptionTemplateTypeEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.isFinalized = data ? data.status === DescriptionTemplateTypeStatus.Finalized : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse descriptionTemplateType item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditDescriptionTemplateType));
		this.descriptionTemplateTypeEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);

		if (this.isFinalized) {
			this.formGroup.disable();
		}
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: DescriptionTemplateType) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/description-template-type')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as DescriptionTemplateTypePersist;

		this.descriptionTemplateTypeService.persist(formData)
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
					this.descriptionTemplateTypeService.delete(value.id).pipe(takeUntil(this._destroyed))
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
					errorMessages: [this.language.instant('DESCRIPTION-TEMPLATE-TYPE-EDITOR.MESSAGES.MISSING-FIELDS')]
				}
			})

			this.formService.touchAllFormFields(this.formGroup);
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('DESCRIPTION-TEMPLATE-TYPE-EDITOR.FINALIZE-DIALOG.TITLE'),
				confirmButton: this.language.instant('DESCRIPTION-TEMPLATE-TYPE-EDITOR.FINALIZE-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('DESCRIPTION-TEMPLATE-TYPE-EDITOR.FINALIZE-DIALOG.NEGATIVE'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.formGroup.get('status').setValue(DescriptionTemplateTypeStatus.Finalized);
				this.formSubmit();
			}
		});
	}
}
