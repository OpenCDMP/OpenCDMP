import { OverlayModule } from '@angular/cdk/overlay';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Injector, LOCALE_ID, NgModule, inject, provideAppInitializer } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_FORMATS, MatMomentDateModule } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from '@app/app-routing.module';
import { AppComponent } from '@app/app.component';
import { CoreServiceModule } from '@app/core/core-service.module';
import { NotificationModule } from '@app/library/notification/notification.module';
import { LoginModule } from '@app/ui/auth/login/login.module';
import { ReloadHelperComponent } from '@app/ui/misc/reload-helper/reload-helper.component';
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
import { CoreAnnotationServiceModule } from 'annotation-service/services/core-service.module';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { DragulaModule } from 'ng2-dragula';
import { CookieService } from 'ngx-cookie-service';
import { NgcCookieConsentConfig, NgcCookieConsentModule } from 'ngx-cookieconsent';
import { MatomoInitializationMode, NgxMatomoModule } from 'ngx-matomo-client';
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

const cookieConfig: NgcCookieConsentConfig = {
	enabled: true,
	cookie: {
		domain: ""//environment.App // or 'your.domain.com' // it is mandatory to set a domain, for cookies to work properly (see https://goo.gl/S2Hy2A)
	},
	palette: {
		popup: {
			background: "#000000",
			text: "#ffffff",
			link: "#ffffff"
		},
		button: {
			background: "#00b29f",
			text: "#ffffff",
			border: "transparent"
		}
	},
	content: {
		message: "This website uses cookies to enhance the user experience.",
		dismiss: "Got it!",
		deny: "Refuse cookies",
		link: "Learn more",
		href: "",//environment.App + "terms-of-service",
		policy: "Cookies Policy"
	},
	position: "bottom-right",
	theme: 'edgeless',
	type: 'info'
};

export function InstallationConfigurationFactory(appConfig: ConfigurationService, keycloak: KeycloakService, authService: AuthService, languageService: LanguageService, tenantHandlingService: TenantHandlingService, router: Router) {
	return () => appConfig.loadConfiguration().then(() => {
		return languageService.loadAvailableLanguages().toPromise();
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
		return authService.prepareAuthRequest(from(token), tenantCode, { params }).toPromise().catch(error => {
			if (!authService.isKeycloakLoggedIn()) return;

			authService.onAuthenticateError(error);
			router.navigate(['/']);
		});
	}));
}


@NgModule({
	declarations: [
		AppComponent,
		ReloadHelperComponent
	],
	bootstrap: [AppComponent], imports: [BrowserModule,
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
		NgcCookieConsentModule.forRoot(cookieConfig),
		DepositOauth2DialogModule,
		GuidedTourModule.forRoot(),
		DragulaModule.forRoot(),
		NgxMatomoModule.forRoot({
			mode: MatomoInitializationMode.AUTO_DEFERRED,
		})],
	providers: [
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
		provideHttpClient(withInterceptorsFromDi())
	]
})
export class AppModule { }
