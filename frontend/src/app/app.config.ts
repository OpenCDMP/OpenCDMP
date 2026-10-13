import { OverlayModule } from '@angular/cdk/overlay';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { ApplicationConfig, LOCALE_ID, importProvidersFrom, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_FORMATS, MatMomentDateModule } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from '@app/app-routing.module';
import { CoreServiceModule } from '@app/core/core-service.module';
import { NotificationModule } from '@app/library/notification/notification.module';
import { LoginModule } from '@app/ui/auth/login/login.module';
import { NavbarModule } from '@app/ui/navbar/navbar.module';
import { SidebarModule } from '@app/ui/sidebar/sidebar.module';
import { MomentUtcDateAdapter } from '@common/date/moment-utc-date-adapter';
import { MomentUtcDateTimeAdapter } from '@common/date/moment-utc-date-time.adapter';
import { BaseHttpParams } from '@common/http/base-http-params';
import { CommonHttpModule } from '@common/http/common-http.module';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DatetimeAdapter } from '@mat-datetimepicker/core';
import { TranslateCompiler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { CoreNotificationServiceModule } from '@notification-service/services/core-service.module';
import { CoreAnnotationServiceModule } from '@annotation-service/services/core-service.module';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { CookieService } from 'ngx-cookie-service';
import { MatomoModule } from 'ngx-matomo-client';
import { from } from 'rxjs';
import { AuthService } from './core/services/auth/auth.service';
import { ConfigurationService } from './core/services/configuration/configuration.service';
import { CultureService } from './core/services/culture/culture-service';
import { LanguageHttpService } from './core/services/language/language.http.service';
import { LanguageService } from './core/services/language/language.service';
import { TranslateServerLoader } from './core/services/language/server.loader';
import { AnalyticsService } from './core/services/matomo/analytics-service';
import { MatomoService } from './core/services/matomo/matomo-service';
import { TenantHandlingService } from './core/services/tenant/tenant-handling.service';
import { GuidedTourModule } from './library/guided-tour/guided-tour.module';
import { DepositOauth2DialogModule } from './ui/misc/deposit-oauth2-dialog/deposit-oauth2-dialog.module';
import { OpenCDMPCustomTranslationCompiler } from './utilities/translate/opencdmp-custom-translation-compiler';
import { Router } from '@angular/router';
import { CoreKpiServiceModule } from 'kpi-service/services/core-service.module';
import { SpinnerModule } from './ui/common/spinner/spinner.module';

// AoT requires an exported function for factories
export function HttpLoaderFactory(languageHttpService: LanguageHttpService, authService: AuthService) {
	return new TranslateServerLoader(languageHttpService, authService);
}

export function InstallationConfigurationFactory(appConfig: ConfigurationService, keycloak: KeycloakService, authService: AuthService, languageService: LanguageService, tenantHandlingService: TenantHandlingService, router: Router) {
	return () => appConfig.loadConfiguration().then(() => {
		const tenantCode = tenantHandlingService.extractTenantCodeFromUrlPath(window.location.pathname) ?? authService.selectedTenant();
		return languageService.loadAvailableLanguages(tenantCode == 'default' ? null : tenantCode).toPromise();
	}).then(x => keycloak.init({
		config: {
			url: appConfig.keycloak.address,
			realm: appConfig.keycloak.realm,
			clientId: appConfig.keycloak.clientId,
		},
		initOptions: {
			onLoad: 'check-sso',
			flow: appConfig.keycloak.flow,
			checkLoginIframe: false,
			scope: appConfig.keycloak.scope,
			pkceMethod: 'S256'
		},
		shouldAddToken: () => false
	}).then(() => {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [
				InterceptorType.Locale,
				InterceptorType.ProgressIndication,
				InterceptorType.RequestTiming,
				InterceptorType.UnauthorizedResponse,
			]
		};

		const tenantCode = tenantHandlingService.extractTenantCodeFromUrlPath(window.location.pathname) ?? authService.selectedTenant() ?? 'default';
		const token = keycloak.getToken();
		// update selected tenant before make any request
		authService.selectedTenant(tenantCode);
		return authService.prepareAuthRequest(from(token), tenantCode, { params }).toPromise().catch(error => {
			if (!authService.isKeycloakLoggedIn()) return;

			authService.onAuthenticateError(error);
			router.navigate(['/']);
		});
	}));
}


export const appConfig: ApplicationConfig = {
    providers: [
        provideZoneChangeDetection(),
        importProvidersFrom(
            BrowserAnimationsModule,
            KeycloakAngularModule,
            CoreServiceModule.forRoot(),
            CoreAnnotationServiceModule.forRoot(),
            CoreNotificationServiceModule.forRoot(),
            CoreKpiServiceModule.forRoot(),
            AppRoutingModule,
            CommonUiModule,
            SpinnerModule,
            TranslateModule.forRoot({
                compiler: { provide: TranslateCompiler, useClass: OpenCDMPCustomTranslationCompiler },
                loader: {
                    provide: TranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [LanguageHttpService, AuthService]
                }
            }),
            OverlayModule,
            CommonHttpModule,
            MatMomentDateModule,
            LoginModule,
            //Ui
            NotificationModule,
            ReactiveFormsModule,
            FormsModule,
            NavbarModule,
            SidebarModule,
            DepositOauth2DialogModule,
            GuidedTourModule.forRoot(),
            MatomoModule.forRoot({
                mode: 'deferred',
            })
        ),
        ConfigurationService,
        provideAppInitializer(() => {
            const initializerFn = (InstallationConfigurationFactory)(inject(ConfigurationService), inject(KeycloakService), inject(AuthService), inject(LanguageService), inject(TenantHandlingService), inject(Router));
            return initializerFn();
        }),
        {
            provide: MAT_DATE_LOCALE,
            deps: [CultureService],
            useFactory: (cultureService) => cultureService.getCurrentCulture().name
        },
        { provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS },
        { provide: DateAdapter, useClass: MomentUtcDateAdapter },
        { provide: DatetimeAdapter, useClass: MomentUtcDateTimeAdapter },
        {
            provide: LOCALE_ID,
            deps: [CultureService, ConfigurationService],
            useFactory: (cultureService, installationConfigurationService) => cultureService.getCurrentCulture(installationConfigurationService).name
        },
        {
            provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
            useValue: {
                appearance: 'outline'
            }
        },
        Title,
        CookieService,
        MatomoService,
        AnalyticsService,
        provideHttpClient(withXhr(), withInterceptorsFromDi())
    ]
};
