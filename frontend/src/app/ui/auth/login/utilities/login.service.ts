import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { CultureService } from '@app/core/services/culture/culture-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseService } from '@common/base/base.service';
import { TranslateService } from '@ngx-translate/core';
import { LanguageService } from '@app/core/services/language/language.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';

@Injectable()
export class LoginService extends BaseService {

	constructor(
		private router: Router,
		private routerUtils: RouterUtilsService,
		private authService: AuthService,
		private translate: TranslateService,
		private zone: NgZone,
		private cultureService: CultureService,
		private uiNotificationService: UiNotificationService,
		private language: LanguageService
	) {
		super();
	}

	public onLogInSuccess(loginResponse: any, returnUrl: string) {
		this.zone.run(() => {
			this.uiNotificationService.snackBarNotification(this.translate.instant('GENERAL.SNACK-BAR.SUCCESSFUL-LOGIN'), SnackBarNotificationLevel.Success);
			if (this.authService.currentAccountIsAuthenticated() && this.authService.getUserProfileCulture()) { this.cultureService.cultureSelected(this.authService.getUserProfileCulture()); }
			if (this.authService.currentAccountIsAuthenticated() && this.authService.getUserProfileLanguage()) { this.language.changeLanguage(this.authService.getUserProfileLanguage()); }
			const redirectUrl = returnUrl || '/';
			this.zone.run(() => this.router.navigateByUrl(this.routerUtils.generateUrl(redirectUrl)));
		});
	}

	public onLogInError(errorMessage: string) {
		this.uiNotificationService.snackBarNotification(this.translate.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-LOGIN'), SnackBarNotificationLevel.Error);
	}
}
