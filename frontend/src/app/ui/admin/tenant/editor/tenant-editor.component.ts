
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Tenant, TenantPersist } from '@app/core/model/tenant/tenant';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TenantService } from '@app/core/services/tenant/tenant.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { TenantEditorModel } from './tenant-editor.model';
import { TenantEditorResolver } from './tenant-editor.resolver';
import { TenantEditorService } from './tenant-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';


@Component({
	selector: 'app-tenant-editor-component',
	templateUrl: 'tenant-editor.component.html',
	styleUrls: ['./tenant-editor.component.scss'],
	providers: [TenantEditorService]
})
export class TenantEditorComponent extends BaseEditor<TenantEditorModel, Tenant> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	depositCodes: Map<number, string[]> = new Map<number, string[]>;
	fileTransformersCodes: Map<number, string[]> = new Map<number, string[]>;

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteTenant);
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditTenant);
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditTenant);
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
		private tenantService: TenantService,
		private logger: LoggingService,
		private tenantEditorService: TenantEditorService,
		private analyticsService: AnalyticsService,
		protected routerUtils: RouterUtilsService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.TenantsEditor);
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: Tenant) => void) {
		this.tenantService.getSingle(itemId, TenantEditorResolver.lookupFields())
			.pipe(map(data => data as Tenant), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Tenant) {
		try {
			this.editorModel = data ? new TenantEditorModel().fromModel(data) : new TenantEditorModel();

			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Tenant item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditTenant));
		if (!this.isNew) this.formGroup.get('code')?.disable();
		this.tenantEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Tenant) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/tenants')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formGroup.getRawValue() as TenantPersist;

		this.tenantService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
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
					this.tenantService.delete(value.id).pipe(takeUntil(this._destroyed))
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
