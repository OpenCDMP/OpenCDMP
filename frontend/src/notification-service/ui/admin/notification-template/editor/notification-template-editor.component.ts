import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatChipEditedEvent, MatChipInputEvent } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Language } from '@app/core/model/language/language';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { EmailOverrideMode } from '@notification-service/core/enum/email-override-mode';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationDataType } from '@notification-service/core/enum/notification-data-type';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { NotificationTemplate, NotificationTemplatePersist } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateService } from '@notification-service/services/http/notification-template.service';
import { map, takeUntil } from 'rxjs/operators';
import { NotificationTemplateEditorModel } from './notification-template-editor.model';
import { NotificationTemplateEditorResolver } from './notification-template-editor.resolver';
import { NotificationTemplateEditorService } from './notification-template-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';

@Component({
    selector: 'app-notification-template-editor',
    templateUrl: './notification-template-editor.component.html',
    styleUrls: ['./notification-template-editor.component.scss'],
    providers: [NotificationTemplateEditorService],
    standalone: false
})
export class NotificationTemplateEditorComponent extends BaseEditor<NotificationTemplateEditorModel, NotificationTemplate> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	languages: Language[] = [];
	subjectMandatoryFields: string[] = [];
	bodyMandatoryFields: string[] = [];
	subjectFieldOptionsEnabled: Boolean = false;
	bodyFieldOptionsEnabled: Boolean = false;
	ccValues: string[] = [];
	bccValues: string[] = [];
	extraDataKeys: string[] = [];
	subjectFormatting: { [key: string]: string } = {};
	bodyFormatting: { [key: string]: string } = {};
	notificationType: string;
	public notificationTypeEnum = this.enumUtils.getEnumValues(NotificationType);
	public notificationTemplateKindEnum = this.enumUtils.getEnumValues(NotificationTemplateKind);
	public notificationTemplateChannelEnum = this.enumUtils.getEnumValues(NotificationTemplateChannel);
	public notificationDataTypeEnum = this.enumUtils.getEnumValues(NotificationDataType);
	public emailOverrideModeEnum = this.enumUtils.getEnumValues(EmailOverrideMode);

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteNotificationTemplate) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditNotificationTemplate) && this.editorModel.belongsToCurrentTenant != false;
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
		public enumUtils: NotificationServiceEnumUtils,
		private languageHttpService: LanguageHttpService,
		private notificationTemplateService: NotificationTemplateService,
		private notificationTemplateEditorService: NotificationTemplateEditorService,
		private logger: LoggingService,
		private titleService: Title,
		private analyticsService: AnalyticsService,
		private routerUtils: RouterUtilsService,
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.notificationType;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-EDITOR.TITLE-EDIT-NOTIFICATION-TEMPLATE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.NotificationTempplateEditor);

		super.ngOnInit();
		this.languageHttpService.query(this.languageHttpService.buildAutocompleteLookup())
			.pipe(takeUntil(this._destroyed))
			.subscribe( //TODO HANDLE-ERRORS
				data => this.languages = data.items,
			);
	}

	getItem(itemId: Guid, successFunction: (item: NotificationTemplate) => void) {
		this.notificationTemplateService.getSingle(itemId, NotificationTemplateEditorResolver.lookupFields())
			.pipe(map(data => data as NotificationTemplate), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: NotificationTemplate) {
		try {
			this.editorModel = data ? new NotificationTemplateEditorModel().fromModel(data) : new NotificationTemplateEditorModel();

			if (data) {
				if (data.value && data.value.subjectFieldOptions) {
					this.subjectFieldOptionsEnabled = true;
					this.subjectMandatoryFields = data.value.subjectFieldOptions.mandatory;
				}
				if (data.value && data.value.bodyFieldOptions) {
					this.bodyFieldOptionsEnabled = true;
					this.bodyMandatoryFields = data.value.bodyFieldOptions.mandatory;
				}
				if (data.value && data.value.subjectFieldOptions && data.value.subjectFieldOptions.formatting) {
					this.subjectFormatting = data.value.subjectFieldOptions.formatting;
				}
				if (data.value && data.value.bodyFieldOptions && data.value.bodyFieldOptions.formatting) {
					this.bodyFormatting = data.value.bodyFieldOptions.formatting;
				}
				this.ccValues = this.editorModel.value.cc;
				this.bccValues = this.editorModel.value.bcc;
				this.extraDataKeys = this.editorModel.value.extraDataKeys;
			}
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Notification Template item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditNotificationTemplate));
		if (this.isDeleted || !this.authService.hasPermission(AppPermission.EditNotificationTemplate)) this.formGroup.disable();
		this.notificationTemplateEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		NotificationTemplateEditorModel.reApplyValueValidators({
			formGroup: this.formGroup,
			validationErrorModel: this.editorModel.validationErrorModel
		}
		)
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: NotificationTemplate) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('notification-templates')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as NotificationTemplatePersist;

		this.notificationTemplateService.persist(formData)
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
					this.notificationTemplateService.delete(value.id).pipe(takeUntil(this._destroyed))
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


	subjectFieldOptionsSelectionChanged(matCheckBox: MatCheckboxChange) {
		if (matCheckBox.checked == true) {
			this.subjectFieldOptionsEnabled = true;
		} else {
			this.subjectFieldOptionsEnabled = false;
		}
	}

	bodyFieldOptionsSelectionChanged(matCheckBox: MatCheckboxChange) {
		if (matCheckBox.checked == true) {
			this.bodyFieldOptionsEnabled = true;
		} else {
			this.bodyFieldOptionsEnabled = false;
		}
	}

	// chip lists

	addChipListValues(type: string, event: MatChipInputEvent) {
		if (type == "cc") {
			this.ccValues = this.add(event, this.ccValues);
		} else if (type == "bcc") {
			this.bccValues = this.add(event, this.bccValues);
		} else if (type == "extraDataKeys") {
			this.extraDataKeys = this.add(event, this.extraDataKeys);
		}
	}

	editChipListValues(type: string, event: MatChipEditedEvent, field: string) {
		if (type == "cc") {
			this.ccValues = this.edit(field, this.ccValues, event);
		} else if (type == "bcc") {
			this.bccValues = this.edit(field, this.bccValues, event);
		} else if (type == "extraDataKeys") {
			this.extraDataKeys = this.edit(field, this.extraDataKeys, event);
		}
	}

	removeChipListValues(type: string, field: string) {
		if (type == "cc") {
			this.ccValues = this.remove(field, this.ccValues);
		} else if (type == "bcc") {
			this.bccValues = this.remove(field, this.bccValues);
		} else if (type == "extraDataKeys") {
			this.extraDataKeys = this.remove(field, this.extraDataKeys);
		}
	}

	add(event: MatChipInputEvent, values: string[]) {
		const value = (event.value || '').trim();

		if (value) values.push(value)
		event.chipInput!.clear();
		return values;

	}

	remove(field: string, values: string[]) {
		const index = values.indexOf(field);

		if (index >= 0) values.splice(index, 1);
		return values;
	}

	edit(field: string, values: string[], event: MatChipEditedEvent) {
		const value = event.value.trim();

		if (!value) {
			values = this.remove(field, values);
			return values;
		}

		const index = values.indexOf(field);
		if (index >= 0) values[index] = value

		return values;
	}

}
