import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ContactEmailFormModel } from '@app/core/model/contact/contact-support-form-model';
import { ContactSupportService } from '@app/core/services/contact-support/contact-support.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ContactDialogComponent } from '@app/ui/contact/contact-dialog/contact-dialog.component';
import { FaqDialogComponent } from '@app/ui/faq/dialog/faq-dialog.component';
import { GlossaryDialogComponent } from '@app/ui/glossary/dialog/glossary-dialog.component';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from "@app/core/services/auth/auth.service";
import { UserGuideDialogComponent } from '@app/ui/user-guide/dialog/user-guide-dialog.component';
import { HttpErrorResponse } from '@angular/common/http';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { AccessLevel, SidebarItem } from '@app/core/model/configuration-models/sidebar.model';

@Component({
	selector: 'app-sidebar-footer',
	templateUrl: './sidebar-footer.component.html',
	styleUrls: ['./sidebar-footer.component.css']
})
export class SidebarFooterComponent extends BaseComponent implements OnInit {

	nestedFooterItems: SidebarItem[][];
	private contactEmailFormModel: ContactEmailFormModel;
	private formGroup: UntypedFormGroup;

	constructor(
		private dialog: MatDialog,
		private language: TranslateService,
		public router: Router,
		public routerUtils: RouterUtilsService,
		private contactSupportService: ContactSupportService,
		private uiNotificationService: UiNotificationService,
		private formService: FormService,
		private authentication: AuthService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private configurationService: ConfigurationService,
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.SidebarFooter);

		const flattenedFooterItems = this.configurationService.sidebar?.footerItems ?? [];

		this.nestedFooterItems = flattenedFooterItems.reduce((prev, current, index) => {
			if (current.accessLevel == AccessLevel.Authenticated && !this.isAuthenticated()) return prev;
			if (current.accessLevel == AccessLevel.Unauthenticated && this.isAuthenticated()) return prev;

			if (index%2==0) prev.push([current]);
			else prev[prev.length-1].push(current);
			return prev;
		}, []);

		this.contactEmailFormModel = new ContactEmailFormModel();
		this.formGroup = this.contactEmailFormModel.buildForm();
	}

	openContactDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(ContactDialogComponent, {
				width: '550px',
				disableClose: true,
				data: {
					isDialog: true,
					formGroup: this.formGroup
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(data => {
				if (data) {
					this.contactSupportService.send(data.value)
						.pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
					this.formGroup.reset();
				}
			});
		}
	}

	openGlossaryDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(GlossaryDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true
				}
			});
		}
	}

	openFaqDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(FaqDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true
				},
				width: '80%'
			});
		}
	}

	openUserGuideDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(UserGuideDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true
				}
			});
		}
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-EMAIL-SEND'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.setErrorModel(errorResponse.error);
		this.formService.validateAllFormFields(this.formGroup);
	
		let errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-EMAIL-SEND'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);
	}

	public setErrorModel(validationErrorModel: ValidationErrorModel) {
		Object.keys(validationErrorModel).forEach(item => {
			(<any>this.contactEmailFormModel.validationErrorModel)[item] = (<any>validationErrorModel)[item];
		});
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	public navigate(): any {
		this.router.navigateByUrl(this.routerUtils.generateUrl('/about'));
	}
}
