import { Location, NgClass } from '@angular/common';
import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ContactEmailFormModel, ContactSupportPersist } from '@app/core/model/contact/contact-support-form-model';
import { ContactSupportService } from '@app/core/services/contact-support/contact-support.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { FormService } from '@common/forms/form-service';
import { HttpClient } from '@angular/common/http';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { MatFormField, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-contact-content',
    templateUrl: './contact-content.component.html',
    styleUrls: ['./contact-content.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormsModule, ReactiveFormsModule, NgClass, MatFormField, MatInput, MatError, MatButton, TranslatePipe]
})
export class ContactContentComponent extends BaseComponent implements OnInit {

	@Input() isDialog: boolean;
	@Input() form: UntypedFormGroup;
	private contactEmailFormModel: ContactEmailFormModel;
	public formGroup: UntypedFormGroup;

	constructor(
		private contactSupportService: ContactSupportService,
		private _location: Location,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private formService: FormService,
		private httpClient: HttpClient,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.ContactContent);
		if (this.isDialog) {
			this.formGroup = this.form;
		} else {
			this.contactEmailFormModel = new ContactEmailFormModel();
			this.formGroup = this.contactEmailFormModel.buildForm();
		}
	}

	cancel() {
		this._location.back();
	}

	send() {
		const formData = this.formService.getValue(this.formGroup.getRawValue()) as ContactSupportPersist;
		this.contactSupportService.send(formData)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => this.onCallbackSuccess(),
				error => this.onCallbackError(error)
			);
		this.formGroup.reset();
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-EMAIL-SEND'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: any) {
		this.setErrorModel(errorResponse.error);
		this.formService.validateAllFormFields(this.formGroup);
	
		const errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-EMAIL-SEND'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error)
	}

	public setErrorModel(validationErrorModel: ValidationErrorModel) {
		Object.keys(validationErrorModel).forEach(item => {
			(<any>this.contactEmailFormModel.validationErrorModel)[item] = (<any>validationErrorModel)[item];
		});
	}
}
