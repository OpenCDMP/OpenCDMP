import { Component, Input, NgZone, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';
import { from } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.scss']
})
export class LoginComponent extends BaseComponent implements OnInit {

	@Input() redirect: boolean = true;
	@Input() mergeUsers: boolean;

	public auth2: any;
	private returnUrl: string;

	constructor(
		private zone: NgZone,
		private router: Router,
		private routerUtils: RouterUtilsService,
		private authService: AuthService,
		private route: ActivatedRoute,
		private tenantHandlingService: TenantHandlingService,
		private keycloakService: KeycloakService,
        private uiNotificationService: UiNotificationService,
        private language: TranslateService
	) { super(); }

	ngOnInit(): void {
		this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/';
		if (!this.keycloakService.isLoggedIn()) {
			this.authService.authenticate(this.returnUrl)
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: () => {
                    this.uiNotificationService.snackBarNotification( //maybe remove this
                        this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-LOGIN'),
                        SnackBarNotificationLevel.Success
                    );
                    this.authService.onAuthenticateSuccess(this.returnUrl);
                }
            });
		} else {
			const tenantCode = this.tenantHandlingService.extractTenantCodeFromUrlPath(this.returnUrl) ?? this.authService.selectedTenant() ?? 'default';
			let returnUrL = this.returnUrl;
			if (this.authService.selectedTenant() != tenantCode) returnUrL = this.routerUtils.generateUrl('/');
			this.zone.run(() => this.router.navigateByUrl(this.routerUtils.generateUrl(returnUrL)));
		}
	}
}
