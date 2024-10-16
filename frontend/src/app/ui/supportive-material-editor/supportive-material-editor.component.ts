import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { SupportiveMaterial, SupportiveMaterialPersist } from '@app/core/model/supportive-material/supportive-material';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
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
import { SupportiveMaterialEditorModel } from './supportive-material-editor.model';
import { SupportiveMaterialEditorResolver } from './supportive-material-editor.resolver';
import { SupportiveMaterialEditorService } from './supportive-material-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';


@Component({
	selector: 'app-supportive-material-editor',
	templateUrl: './supportive-material-editor.component.html',
	styleUrls: ['./supportive-material-editor.component.scss'],
	providers: [SupportiveMaterialEditorService]
})
export class SupportiveMaterialEditorComponent extends BaseEditor<SupportiveMaterialEditorModel, SupportiveMaterial> implements OnInit {
	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	availableLanguageCodes: string[] = []

	public supportiveMaterialTypeEnum = this.enumUtils.getEnumValues(SupportiveMaterialFieldType);


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
		private supportiveMaterialService: SupportiveMaterialService,
		private languageV2Service: LanguageHttpService,
		private logger: LoggingService,
		private supportiveMaterialEditorService: SupportiveMaterialEditorService,
		private routerUtils: RouterUtilsService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
		this.languageV2Service.queryAvailableCodes(this.languageV2Service.buildAutocompleteLookup())
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => this.availableLanguageCodes = data.items,
			);
	}

	getItem(itemId: Guid, successFunction: (item: SupportiveMaterial) => void) {
		this.supportiveMaterialService.getSingle(itemId, SupportiveMaterialEditorResolver.lookupFields())
			.pipe(map(data => data as SupportiveMaterial), takeUntil(this._destroyed))
			.subscribe( //TODO HANDLE-ERRORS
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: SupportiveMaterial) {
		try {
			this.editorModel = data ? new SupportiveMaterialEditorModel().fromModel(data) : new SupportiveMaterialEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Supportive Material item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditLanguage));
		this.supportiveMaterialEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: SupportiveMaterial) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('supportive-material')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
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
					this.supportiveMaterialService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackDeleteSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	public selectedMaterialChanged() {
		this.getSupportiveMaterialData();
	}

	public selectedLangChanged() {
		this.getSupportiveMaterialData();
	}

	private getSupportiveMaterialData() {

		if (this.formGroup.get('type').value >= 0 && this.formGroup.get('languageCode').value) {
			const lookup = SupportiveMaterialService.DefaultSupportiveMaterialLookup();
			lookup.types = [this.formGroup.get('type').value];
			lookup.languageCodes = [this.formGroup.get('languageCode').value];
			this.supportiveMaterialService.query(lookup).pipe(takeUntil(this._destroyed)).subscribe(data => { //TODO HANDLE-ERRORS
				if (data.count == 1) {
					this.formGroup.get('id').patchValue(data.items[0].id);
					this.formGroup.get('payload').patchValue(data.items[0].payload);
				} else {
					this.formGroup.get('id').patchValue(null);
					this.formGroup.get('payload').patchValue('');
				}
			});
		}
	}

}
