import { Injectable } from '@angular/core';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { BaseComponent } from '@common/base/base.component';
import { catchError, takeUntil } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { HelpService } from '@app/core/model/configuration-models/help-service.model';
import { Logging } from '@app/core/model/configuration-models/logging.model';
import { HttpClient } from '@angular/common/http';
import { KeycloakConfiguration } from '@app/core/model/configuration-models/keycloak-configuration.model';
import { Guid } from '@common/types/guid';
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

	private _helpService: HelpService;
	get helpService(): HelpService {
		return this._helpService;
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

	private _defaultBlueprintId: Guid;
	get defaultBlueprintId(): Guid {
		return this._defaultBlueprintId;
	}

	private _logging: Logging;
	get logging(): Logging {
		return this._logging;
	}

	private _lockInterval: number;
	get lockInterval(): number {
		return this._lockInterval;
	}

	private _guideAssets: string;
	get guideAssets(): string {
		return this._guideAssets;
	}

	private _allowOrganizationCreator: boolean;
	get allowOrganizationCreator(): boolean {
		return this._allowOrganizationCreator;
	}

	private _orcidPath: string;
	get orcidPath(): string {
		return this._orcidPath;
	}

	private _matomoEnabled: boolean;
	get matomoEnabled(): boolean {
		return this._matomoEnabled;
	}

	private _matomoSiteUrl: string;
	get matomoSiteUrl(): string {
		return this._matomoSiteUrl;
	}

	private _matomoSiteId: number;
	get matomoSiteId(): number {
		return this._matomoSiteId;
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

			this.http
				.get("./assets/config/config.json", { params: params })
				.pipe(
					catchError((err: any, caught: Observable<any>) =>
						throwError(err)
					)
				)
				.pipe(takeUntil(this._destroyed))
				.subscribe(
					(content: ConfigurationService) => {
						this.parseResponse(content);
						r(this);
					},
					(reason) => e(reason)
				);
		});
	}



	private parseResponse(config: any) {
		this._server = config.Server;
		this._app = config.App;
		this._helpService = HelpService.parseValue(config.HelpService);
		this._defaultCulture = config.defaultCulture;
		this._defaultBlueprintId = config.defaultBlueprintId;
		this._defaultTimezone = config.defaultTimezone;
		this._keycloak = KeycloakConfiguration.parseValue(config.keycloak);
		this._logging = Logging.parseValue(config.logging);
		this._lockInterval = config.lockInterval;
		this._guideAssets = config.guideAssets;
		this._navLogoExtension = config.navLogoExtension;
		this._allowOrganizationCreator = config.allowOrganizationCreator;
		this._orcidPath = config.orcidPath;
		if (config.matomo) {
			this._matomoEnabled = config.matomo.enabled;
			this._matomoSiteUrl = config.matomo.url;
			this._matomoSiteId = config.matomo.siteId;
		}
		this._maxFileSizeInMB = config.maxFileSizeInMB;
		this._userSettingsVersion = config.userSettingsVersion;
		if (config.notification_service) {
			this._notificationServiceEnabled = config.notification_service.enabled;
			this._notificationServiceAddress = config.notification_service.address;
		}
		if (config.annotation_service) {
			this._annotationServiceEnabled = config.annotation_service.enabled;
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
				primaryColor2: config.defaultCssColors.primaryColor2,
				primaryColor3: config.defaultCssColors.primaryColor3,
				secondaryColor: config.defaultCssColors.secondaryColor,
			}
		}
	}

}
