import { Component, Input, NgZone, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { BaseComponent } from '@common/base/base.component';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';
import { from } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    standalone: false
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
        private language: TranslateService,
        private principalService: PrincipalService
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
			let returnUrl = this.returnUrl;
            let emptyUrl = this.routerUtils.generateUrl('/');
			if (this.authService.selectedTenant() != tenantCode){ 
                const params = new BaseHttpParams();
                params.interceptorContext = {
                    excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
                };
                this.principalService.myTenants({ params: params })
                .pipe(takeUntil(this._destroyed))
                .subscribe({
                    next: (tenants) => {
                        if(tenants.some((x) => x.code === tenantCode)){
                            this.authService.selectedTenant(tenantCode);
                            this.zone.run(() => this.router.navigateByUrl(this.routerUtils.generateUrl(returnUrl)));
                        } else {
                            this.zone.run(() => this.router.navigateByUrl(this.routerUtils.generateUrl(emptyUrl)));
                        }
                    },
                    error: (error) => this.zone.run(() => this.router.navigateByUrl(emptyUrl))
                })
            } else {
                this.zone.run(() => this.router.navigateByUrl(this.routerUtils.generateUrl(returnUrl)));
            }
		}
	}
}
