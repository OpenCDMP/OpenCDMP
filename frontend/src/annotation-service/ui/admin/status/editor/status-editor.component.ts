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
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { StatusEditorModel } from './status-editor.model';
import { StatusEditorService } from './status-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AnnotationServiceEnumUtils } from '@annotation-service/core/formatting/enum-utils.service';
import { Status, StatusPersist } from '@annotation-service/core/model/status.model';
import { StatusService } from '@annotation-service/services/http/status.service';
import { StatusEditorResolver } from './status-editor.resolver';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';

@Component({
    templateUrl: './status-editor.component.html',
    styleUrls: ['./status-editor.component.scss'],
    providers: [StatusEditorService],
    standalone: false
})
export class StatusEditorComponent extends BaseEditor<StatusEditorModel, Status> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	public internalStatusEnum = this.enumUtils.getEnumValues<InternalStatus>(InternalStatus);


	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteStatus) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditStatus) && this.editorModel.belongsToCurrentTenant != false;
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
		public enumUtils: AnnotationServiceEnumUtils,
		private statusService: StatusService,
		private logger: LoggingService,
		private statusEditorService: StatusEditorService,
		public titleService: Title,
		protected routerUtils: RouterUtilsService
	) {
		const statusLabel: string = route.snapshot.data['entity']?.name;
		if (statusLabel) {
			titleService.setTitle(statusLabel);
		} else {
			titleService.setTitle('ANNOTATION-SERVICE.STATUS-EDITOR.TITLE-EDIT-ANNOTATION-STATUS');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: Status) => void) {
		this.statusService.getSingle(itemId, StatusEditorResolver.lookupFields())
			.pipe(map(data => data as Status), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Status) {
		try {
			this.editorModel = data ? new StatusEditorModel().fromModel(data) : new StatusEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Status item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditStatus));
		this.statusEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Status) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/annotation-statuses')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as StatusPersist;

		this.statusService.persist(formData)
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
					this.statusService.delete(value.id).pipe(takeUntil(this._destroyed))
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
}
