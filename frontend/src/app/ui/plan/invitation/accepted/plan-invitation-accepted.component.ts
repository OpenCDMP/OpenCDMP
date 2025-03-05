import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-plan-invitation-accepted-component',
    templateUrl: 'plan-invitation-accepted.component.html',
    standalone: false
})
export class InvitationAcceptedComponent extends BaseComponent implements OnInit {
	constructor(
		private planService: PlanService,
		private route: ActivatedRoute,
		private router: Router,
		private authentication: AuthService,
		protected routerUtils: RouterUtilsService,
		protected language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private uiNotificationService: UiNotificationService
	) { super(); }

	ngOnInit(): void {

		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe(params => {
				const token = params['token'];

				if(this.isAuthenticated()){
					this.planService.acceptInvitation(token)
					.pipe(takeUntil(this._destroyed))
					.subscribe(result => {
						if (result?.isAlreadyAccepted == false) {
							this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-USER-INVITATION-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
						}
						this.router.navigate([this.routerUtils.generateUrl('plans/overview/' + result?.planId)]);
					},
					error => this.onCallbackError(error));
				}else{
					let returnUrl = `plans/invitation/${token}`;
					this.router.navigate([this.routerUtils.generateUrl('login')], {queryParams:{returnUrl: this.routerUtils.generateUrl(returnUrl)}});
				}
			});
	}


	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	onCallbackError(errorResponse: HttpErrorResponse) {

		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, null, SnackBarNotificationLevel.Error)

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 302) {
			this.router.navigate([this.routerUtils.generateUrl('home')]);
		}
		this.router.navigate([this.routerUtils.generateUrl('home')]);
	}
}
