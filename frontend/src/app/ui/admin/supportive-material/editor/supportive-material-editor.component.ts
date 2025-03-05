
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { SupportiveMaterial, SupportiveMaterialPersist } from '@app/core/model/supportive-material/supportive-material';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
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
import { SupportiveMaterialEditorModel } from './supportive-material-editor.model';
import { SupportiveMaterialEditorResolver } from './supportive-material-editor.resolver';
import { SupportiveMaterialEditorService } from './supportive-material-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';


@Component({
    selector: 'app-supportive-material-editor-component',
    templateUrl: 'supportive-material-editor.component.html',
    styleUrls: ['./supportive-material-editor.component.scss'],
    providers: [SupportiveMaterialEditorService],
    standalone: false
})
export class SupportiveMaterialEditorComponent extends BaseEditor<SupportiveMaterialEditorModel, SupportiveMaterial> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
    availableLanguageCodes: string[] = [];
	currentPayload: string = null;

	public supportiveMaterialTypeEnum = this.enumUtils.getEnumValues(SupportiveMaterialFieldType);

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteSupportiveMaterial);
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditSupportiveMaterial) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditSupportiveMaterial);
	}


	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
	}

	constructor(
		// BaseFormEditor injected dependencies
		protected dialog: MatDialog,
		protected SupportiveMaterial: TranslateService,
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
		private supportiveMaterialService: SupportiveMaterialService,
		private logger: LoggingService,
		private supportiveMaterialEditorService: SupportiveMaterialEditorService,
		private fileUtils: FileUtils,
		private titleService: Title,
		private analyticsService: AnalyticsService,
		private languageHttpService: LanguageHttpService,
		protected routerUtils: RouterUtilsService
	) {
		const supportiveMaterialLabel: string = route.snapshot.data['entity']?.code;
		if (supportiveMaterialLabel) {
			titleService.setTitle(supportiveMaterialLabel);
		} else {
			titleService.setTitle('SUPPORTIVE-MATERIAL-EDITOR.TITLE-EDIT-SUPPORTIVE-MATERIAL');
		}
		super(dialog, SupportiveMaterial, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.SupportiveMaterialEditor);
		super.ngOnInit();
		this.languageHttpService.queryAvailableCodes(this.languageHttpService.buildAutocompleteLookup())
		.pipe(takeUntil(this._destroyed))
		.subscribe(
			data => this.availableLanguageCodes = data.items,
		);
	}

	getItem(itemId: Guid, successFunction: (item: SupportiveMaterial) => void) {
		this.supportiveMaterialService.getSingle(itemId, SupportiveMaterialEditorResolver.lookupFields())
			.pipe(map(data => data as SupportiveMaterial), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: SupportiveMaterial) {
		try {
			if (data && data.payload) {
				this.currentPayload = data.payload;
			}
			this.editorModel = data ? new SupportiveMaterialEditorModel().fromModel(data) : new SupportiveMaterialEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse SupportiveMaterial item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.SupportiveMaterial.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditSupportiveMaterial));
		this.supportiveMaterialEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: SupportiveMaterial) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/SupportiveMaterials')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as SupportiveMaterialPersist;

		this.supportiveMaterialService.persist(formData)
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
					message: this.SupportiveMaterial.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.SupportiveMaterial.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.SupportiveMaterial.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.supportiveMaterialService.delete(value.id).pipe(takeUntil(this._destroyed))
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

	public overridePayloadFromFile(matCheckBox: MatCheckboxChange) {
		if (matCheckBox.checked == true) {
			this.supportiveMaterialService.getPayloadFromFile(this.formGroup.get('type').value, this.formGroup.get('languageCode').value)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				if(response) this.formGroup.get('payload').patchValue(response.body);
			},
			error => {
				matCheckBox.source.checked = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			});
		} else {
			this.formGroup.get('payload').patchValue(this.currentPayload);
		}
	}
}
