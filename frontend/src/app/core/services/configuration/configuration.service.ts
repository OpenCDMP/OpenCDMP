import { Injectable } from '@angular/core';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { BaseComponent } from '@common/base/base.component';
import { catchError, combineLatestWith, takeUntil } from 'rxjs/operators';
import { combineLatest, Observable, of, throwError } from 'rxjs';
import { Logging } from '@app/core/model/configuration-models/logging.model';
import { HttpClient } from '@angular/common/http';
import { KeycloakConfiguration } from '@app/core/model/configuration-models/keycloak-configuration.model';
import { AuthProviders } from '@app/core/model/configuration-models/auth-providers.model';
import { AnalyticsProviders } from '@app/core/model/configuration-models/analytics-providers.model';
import { CssColorsTenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { Sidebar } from '@app/core/model/configuration-models/sidebar.model';
import { StatusIcon } from '@app/core/model/configuration-models/status-icon.model';

@Injectable({
	providedIn: 'root',
})
export class ConfigurationService extends BaseComponent {

	constructor(private http: HttpClient) { super(); }

	private _server: string;
	get server(): string {
		return this._server;
	}

	private _app: string;
	get app(): string {
		return this._app;
	}

	private _defaultCulture: string;
	get defaultCulture(): string {
		return this._defaultCulture || 'en';
	}

	private _defaultTimezone: string;
	get defaultTimezone(): string {
		return this._defaultTimezone || 'UTC';
	}

	private _navLogoExtension: string;
	get navLogoExtension(): string {
		return this._navLogoExtension || '.svg';
	}

	private _logging: Logging;
	get logging(): Logging {
		return this._logging;
	}

	private _lockInterval: number;
	get lockInterval(): number {
		return this._lockInterval;
	}

	private _orcidPath: string;
	get orcidPath(): string {
		return this._orcidPath;
	}

	private _maxFileSizeInMB: number;
	get maxFileSizeInMB(): number {
		return this._maxFileSizeInMB;
	}


	private _keycloak: KeycloakConfiguration;
	get keycloak(): KeycloakConfiguration {
		return this._keycloak;
	}

	private _userSettingsVersion: string;
	get userSettingsVersion(): string {
		return this._userSettingsVersion;
	}

	private _notificationServiceAddress: string;
	get notificationServiceAddress(): string {
		return this._notificationServiceAddress || './';
	}

	private _notificationServiceEnabled: boolean;
	get notificationServiceEnabled(): boolean {
		return this._notificationServiceEnabled;
	}

	private _statusIcons: StatusIcon[];
	get statusIcons(): StatusIcon[] {
		return this._statusIcons;
	}

	private _defaultStatusIcon: string;
	get defaultStatusIcon(): string {
		return this._defaultStatusIcon || 'animation';
	}

	private _annotationServiceAddress: string;
	get annotationServiceAddress(): string {
		return this._annotationServiceAddress || './';
	}

	private _annotationServiceEnabled: boolean;
	get annotationServiceEnabled(): boolean {
		return this._annotationServiceEnabled;
	}

	private _inAppNotificationsCountInterval: number;
	get inAppNotificationsCountInterval(): number {
		return this._inAppNotificationsCountInterval || 3200;
	}

	private _newReleaseNotificationLink: number;
	get newReleaseNotificationLink(): number {
		return this._newReleaseNotificationLink;
	}

	private _newReleaseNotificationExpires: number;
	get newReleaseNotificationExpires(): number {
		return this._newReleaseNotificationExpires;
	}

	private _newReleaseNotificationVersionCode: number;
	get newReleaseNotificationVersionCode(): number {
		return this._newReleaseNotificationVersionCode;
	}

	private _authProviders: AuthProviders;
	get authProviders(): AuthProviders {
		return this._authProviders;
	}

	private _analyticsProviders: AnalyticsProviders;
	get analyticsProviders(): AnalyticsProviders {
		return this._analyticsProviders;
	}

	private _sidebar: Sidebar;
	get sidebar(): Sidebar {
		return this._sidebar;
	}

	private _mergeAccountDelayInSeconds: number;
	get mergeAccountDelayInSeconds(): number {
		return this._mergeAccountDelayInSeconds;
	}

	private _researcherId: any;
	get researcherId(): boolean {
		return this._researcherId;
	}

	private _grantId: any;
	get grantId(): boolean {
		return this._grantId;
	}

	private _organizationId: any;
	get organizationId(): boolean {
		return this._organizationId;
	}

	private _depositRecordUrlIdPlaceholder: string;
	get depositRecordUrlIdPlaceholder(): string {
		return this._depositRecordUrlIdPlaceholder;
	}

	private _cssColorsTenantConfiguration: CssColorsTenantConfiguration;
	get cssColorsTenantConfiguration(): CssColorsTenantConfiguration {
		return this._cssColorsTenantConfiguration;
	}



	private _kpiServiceAddress: string;
	get kpiServiceAddress(): string {
		return this._kpiServiceAddress || './';
	}

	private _kpiServiceEnabled: boolean;
	get kpiServiceEnabled(): boolean {
		return this._kpiServiceEnabled;
	}

	private _kpiDashboardId: string;
	get kpiDashboardId(): string {
		return this._kpiDashboardId;
	}

	private _keywordFilter: string;
	get keywordFilter(): string {
		return this._keywordFilter;
	}

	private _accountingServiceEnabled: boolean;
	get accountingServiceEnabled(): boolean {
		return this._accountingServiceEnabled;
	}

	public loadConfiguration(): Promise<any> {
		return new Promise((r, e) => {
			// We need to exclude all interceptors here, for the initial configuration request.
			const params = new BaseHttpParams();
			params.interceptorContext = {
				excludedInterceptors: [
					InterceptorType.AuthToken,
					InterceptorType.TenantHeaderInterceptor,
					InterceptorType.JSONContentType,
					InterceptorType.Locale,
					InterceptorType.ProgressIndication,
					InterceptorType.RequestTiming,
					InterceptorType.UnauthorizedResponse,
				],
			};

            combineLatest([
                this.http.get("./assets/config/config.json", { params: params })
                .pipe(
                    catchError((err: any) =>
                        throwError(() => err)
                    )
                ),
                this.http.get("./assets/config/config-override.json", { params: params })
                .pipe(
                    catchError((err: any) => {
                        return of(new Object());
                    })
                )
            ])
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: ([content, overrides]) => {
                    const objectType = typeof(new Object());
                    let response = this.overrideValues(content, overrides);
                    this.parseResponse(response);
                    r(this);
                },
                error: (reason) => e(reason)
            })
		});
	}

    private overrideValues(content: any, override: any){
        let nullSet = new Set([null, undefined, '']);
        let response = {}
        const objectType = typeof(new Object());
        if(nullSet.has(content)){ return override; }
        if(typeof(content) === objectType && !Array.isArray(content) && typeof(override) === objectType && !Array.isArray(override)){
            const keys = new Set([...Object.keys(content), ...Object.keys(override)])
            keys?.forEach((key) => {
                response[key] = this.overrideValues(content[key], override?.[key])
            })
        }else {
            response = nullSet.has(override) ? content : override
        }
        return response;
    }

	private parseResponse(config: any) {
		this._server = config.Server;
		this._app = config.App;
		this._defaultCulture = config.defaultCulture;
		this._defaultTimezone = config.defaultTimezone;
		this._keycloak = KeycloakConfiguration.parseValue(config.keycloak);
		this._logging = Logging.parseValue(config.logging);
		this._lockInterval = config.lockInterval;
		this._navLogoExtension = config.navLogoExtension;
		this._orcidPath = config.orcidPath;
		this._maxFileSizeInMB = config.maxFileSizeInMB;
		this._userSettingsVersion = config.userSettingsVersion;
		if (config.notification_service) {
			this._notificationServiceEnabled = config.notification_service.enabled === true || config.notification_service.enabled === "true";
			this._notificationServiceAddress = config.notification_service.address;
		}
		if (config.annotation_service) {
			this._annotationServiceEnabled = config.annotation_service.enabled === true || config.annotation_service.enabled === "true";
			this._annotationServiceAddress = config.annotation_service.address;
			this._statusIcons = [];
			config.annotation_service?.statusIcons?.forEach(statusIcon => this._statusIcons.push(StatusIcon.parseValue(statusIcon)));
			this._defaultStatusIcon = config.annotation_service.defaultStatusIcon;
		}
		this._inAppNotificationsCountInterval = config.inAppNotificationsCountInterval;
		this._newReleaseNotificationExpires = config.newReleaseNotification?.expires;
		this._newReleaseNotificationLink = config.newReleaseNotification?.link;
		this._newReleaseNotificationVersionCode = config.newReleaseNotification?.versionCode;
		this._authProviders = AuthProviders.parseValue(config.authProviders);
		this._analyticsProviders = AnalyticsProviders.parseValue(config.analytics);
		this._sidebar = Sidebar.parseValue(config.sidebar);
		this._mergeAccountDelayInSeconds = config.mergeAccountDelayInSeconds;
		this._researcherId = config.referenceTypes.researcherId;
		this._grantId = config.referenceTypes.grantId;
		this._organizationId = config.referenceTypes.organizationId;
		this._depositRecordUrlIdPlaceholder = config.deposit.recordUrlIdPlaceholder;

		if (config.defaultCssColors) {
			this._cssColorsTenantConfiguration =  {
				primaryColor: config.defaultCssColors.primaryColor,
				cssOverride: config.defaultCssColors.cssOverride
			}
		}

		if (config.kpi_service) {
			this._kpiServiceEnabled = config.kpi_service.enabled === true || config.kpi_service.enabled === "true";
			this._kpiServiceAddress = config.kpi_service.address;
			this._kpiDashboardId = config.kpi_service.dashboardId;
			this._keywordFilter = config.kpi_service.keywordFilter;
		}

		if (config.accounting_service) {
			this._accountingServiceEnabled = config.accounting_service.enabled === true || config.accounting_service.enabled === "true";
		}
	}

}
