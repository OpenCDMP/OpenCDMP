import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { WebhookEventType } from '@app/core/common/enum/webhook-event-type.enum';
import { WebhookSubscription, WebhookSubscriptionPersist } from '@app/core/model/webhook/webhook';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { WebhookService } from '@app/core/services/webhook/webhook.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { WebhookEditorModel } from './webhook-editor.model';
import { WebhookEditorResolver } from './webhook-editor.resolver';
import { WebhookEditorService } from './webhook-editor.service';
import { WebhookSecretDialogComponent } from './secret-dialog/webhook-secret-dialog.component';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { TenantReadonlyBannerComponent } from '../../../../../common/ui/tenant-shared/tenant-readonly-banner.component';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatSlideToggle } from '@angular/material/slide-toggle';

@Component({
    selector: 'app-webhook-editor-component',
    templateUrl: 'webhook-editor.component.html',
    styleUrls: ['./webhook-editor.component.scss'],
    providers: [WebhookEditorService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, TenantReadonlyBannerComponent, MatButton, MatIcon, FormsModule, MatCard, MatCardContent, MatFormField, MatLabel, ReactiveAsteriskDirective, MatInput, ReactiveFormsModule, MatError, MatSelect, MatOption, MatSlideToggle, TranslatePipe]
})
export class WebhookEditorComponent extends BaseEditor<WebhookEditorModel, WebhookSubscription> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	availableEventTypes: WebhookEventType[] = [];

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteWebhookSubscription) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditWebhookSubscription) && this.editorModel.belongsToCurrentTenant != false;
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
		private webhookService: WebhookService,
		private logger: LoggingService,
		private webhookEditorService: WebhookEditorService,
		protected routerUtils: RouterUtilsService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
		this.webhookService.eventTypes()
			.pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (eventTypes) => this.availableEventTypes = eventTypes ?? [],
				error: (error) => this.onCallbackError(error)
			});
	}

	getItem(itemId: Guid, successFunction: (item: WebhookSubscription) => void) {
		this.webhookService.getSingle(itemId, WebhookEditorResolver.lookupFields())
			.pipe(map(data => data as WebhookSubscription), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: WebhookSubscription) {
		try {
			this.editorModel = data ? new WebhookEditorModel().fromModel(data) : new WebhookEditorModel();

			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse webhook subscription item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditWebhookSubscription));
		this.webhookEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: WebhookSubscription) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/webhooks')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formGroup.getRawValue() as WebhookSubscriptionPersist;
		const wasNew = this.isNew;

		this.enqueueService.enqueueExhaustChannel(this.webhookService.persist(formData))
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => {
					// A fresh subscription's signing secret is returned exactly once — show it before navigating away.
					if (wasNew && complete?.secret) {
						this.dialog.open(WebhookSecretDialogComponent, {
							data: { secret: complete.secret },
							disableClose: true,
							maxWidth: '34rem',
							autoFocus: 'dialog'
						}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(() => {
							onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete);
						});
					} else {
						onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete);
					}
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
					this.enqueueService.enqueueExhaustChannel(this.webhookService.delete(value.id)).pipe(takeUntil(this._destroyed))
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
