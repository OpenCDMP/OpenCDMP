import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { UserService } from '@app/core/services/user/user.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from "rxjs/operators";

@Component({
	selector: 'app-unlink-email-confirmation-component',
	templateUrl: './unlink-email-confirmation.component.html'
})
export class UnlinkEmailConfirmation extends BaseComponent implements OnInit {
	private token: Guid;


	get showForm(): boolean {
		return this.token != null;
	}

	constructor(
		private userService: UserService,
		private route: ActivatedRoute,
		private router: Router,
		private language: TranslateService,
		private routerUtils: RouterUtilsService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authentication: AuthService,
	) { super(); }

	ngOnInit() {
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe(params => {
				const token = params['token']
				if (token != null) {
					this.token = token;
				}
				if(!this.authentication.currentAccountIsAuthenticated()){
					let returnUrl = `login/unlink/confirmation/${this.token}`;
					this.router.navigate([this.routerUtils.generateUrl('login')], {queryParams:{returnUrl: this.routerUtils.generateUrl(returnUrl)}});	
				}
			});
	}

	onConfirm(): void {
		if (this.showForm === false) return;

		this.userService.confirmRemoveCredentialAccount(this.token)
			.subscribe(result => {
				if (result) {
					this.onCallbackConfirmationSuccess();
				}
			},
				error => this.onCallbackError(error));
	}

	onCallbackConfirmationSuccess() {
		this.router.navigate([this.routerUtils.generateUrl('home')])
			.then(() => {
				localStorage.setItem('refreshPage', null);
				localStorage.setItem('refreshPage', 'true');
				window.location.reload();
			});
	}

	onCallbackError(errorResponse: HttpErrorResponse) {

		let errorOverrides = new Map<number, string>();
		errorOverrides.set(302, this.language.instant('EMAIL-CONFIRMATION.EMAIL-FOUND'));
		errorOverrides.set(-1, this.language.instant('EMAIL-CONFIRMATION.EXPIRED-EMAIL'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides)

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 302 || error.statusCode === 400) {
			this.router.navigate([this.routerUtils.generateUrl('home')]);
		}
	}
}
