
import { Component, OnInit } from '@angular/core';
import { FormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { ReferenceFieldDataType } from '@app/core/common/enum/reference-field-data-type';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { Reference, ReferencePersist } from '@app/core/model/reference/reference';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { ReferenceEditorModel } from './reference-editor.model';
import { ReferenceEditorResolver } from './reference-editor.resolver';
import { ReferenceEditorService } from './reference-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';


@Component({
    selector: 'app-reference-editor-component',
    templateUrl: 'reference-editor.component.html',
    styleUrls: ['./reference-editor.component.scss'],
    providers: [ReferenceEditorService],
    standalone: false
})
export class ReferenceEditorComponent extends BaseEditor<ReferenceEditorModel, Reference> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	public referenceSourceTypeEnum = this.enumUtils.getEnumValues(ReferenceSourceType);
	public referenceFieldDataTypeEnum = this.enumUtils.getEnumValues(ReferenceFieldDataType);

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteReference) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditReference) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditReference);
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
		private referenceService: ReferenceService,
		private logger: LoggingService,
		private referenceEditorService: ReferenceEditorService,
		private fileUtils: FileUtils,
		public referenceTypeService: ReferenceTypeService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		private routerUtils: RouterUtilsService
	) {

		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('REFERENCE-EDITOR.TITLE-EDIT-REFERENCE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.ReferencesEditor);
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: Reference) => void) {
		this.referenceService.getSingle(itemId, ReferenceEditorResolver.lookupFields())
			.pipe(map(data => data as Reference), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Reference) {
		try {
			this.editorModel = data ? new ReferenceEditorModel().fromModel(data) : new ReferenceEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Reference item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditReference));
		this.referenceEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Reference) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/references')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as ReferencePersist;

		this.referenceService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
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
					this.referenceService.delete(value.id).pipe(takeUntil(this._destroyed))
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

	//
	// fields
	//
	addField(): void {
		(this.formGroup.get('definition').get('fields') as FormArray).push(this.editorModel.createChildField((this.formGroup.get('definition').get('fields') as FormArray).length));

	}

	removeField(fieldIndex: number): void {
		(this.formGroup.get('definition').get('fields') as FormArray).removeAt(fieldIndex);
		ReferenceEditorModel.reApplyDefinitionFieldsValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		)
		this.formGroup.get('definition').get('fields').markAsDirty();
	}


}
