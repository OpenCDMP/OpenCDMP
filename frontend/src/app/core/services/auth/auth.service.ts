
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AppAccount } from '@app/core/model/auth/principal';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseService } from '@common/base/base.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { Guid } from '@common/types/guid';
import { KeycloakEvent, KeycloakEventType, KeycloakService } from 'keycloak-angular';
import { Observable, Subject, Subscription, forkJoin, from, of, timer } from 'rxjs';
import { catchError, concatMap, exhaustMap, map, takeUntil, tap } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { PrincipalService } from '../http/principal.service';
import { TenantHandlingService } from '../tenant/tenant-handling.service';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { SpinnerComponent } from '@app/ui/common/spinner/spinner.component';
import { NotificationPrincipalService } from '@notification-service/services/http/principal.service';
import { NotificationAccount } from '@notification-service/core/model/principal';
import { AnnotationAccount } from '@annotation-service/core/model/principal';
import { AnnotationPrincipalService } from '@annotation-service/services/http/principal.service';

export interface ResolutionContext {
	permissions: AppPermission[];
}
export interface AuthenticationState {
	loginStatus: LoginStatus;
}

export enum LoginStatus {
	LoggedIn = 0,
	LoggingOut = 1,
	LoggedOut = 2
}

@Injectable()
export class AuthService extends BaseService {

	public permissionEnum = AppPermission;
	public authenticationStateSubject: Subject<AuthenticationState>;
	private accessToken: string;
	private appAccount: AppAccount;
	private notificationAccount: NotificationAccount;
	private annotationAccount: AnnotationAccount;

	private _authState: boolean;
	private _selectedTenant: string;

	private _appsUserNotAlreadyCreated: boolean;
	private appUserNotAlreadyCreatedSubscription: Subscription = null;
	private overlayRef: OverlayRef = null;

	constructor(
		private installationConfiguration: ConfigurationService,
		// private language: TranslateService,
		private router: Router,
		private zone: NgZone,
		private keycloakService: KeycloakService,
		private uiNotificationService: UiNotificationService,
		private principalService: PrincipalService,
		private overlay: Overlay,
		private tenantHandlingService: TenantHandlingService,
		private annotationPrincipalService: AnnotationPrincipalService,
		private notificationPrincipalService: NotificationPrincipalService,
        private configurationService: ConfigurationService
	) {
		super();


		this.authenticationStateSubject = new Subject<AuthenticationState>();

		window.addEventListener('storage', (event: StorageEvent) => {
			// Logout if we receive event that logout action occurred in a different tab.
			if (
				event.key &&
				event.key === 'authState' &&
				event.newValue === 'false' &&
				this._authState
			) {
				this.clear();
				this.router.navigate(['/unauthorized'], {
					queryParams: { returnUrl: this.router.url },
				});
			} else if (
				event.key &&
				event.key === 'selectedTenant' &&
				event.newValue != this.selectedTenant()
			) {
				this.selectedTenant(event.newValue);
				window.location.href = this.tenantHandlingService.getCurrentUrlEnrichedWithTenantCode(event.newValue, true);
			} else if (
				event.key &&
				event.key === 'refreshPage' &&
				event.newValue == 'true'
			) {
				window.location.reload();
			}
		});
	}

	public selectedTenant(selectedTenant?: string): string {
		if (selectedTenant !== undefined) {
			this._selectedTenant = selectedTenant;
			if (selectedTenant == null) {
				localStorage.removeItem('selectedTenant');
			} else {
				localStorage.setItem('selectedTenant', selectedTenant);
			}
		}
		if (this._selectedTenant === undefined) {
			this._selectedTenant = localStorage.getItem('selectedTenant');
		}
		return this._selectedTenant;
	}

	public getAuthenticationStateObservable(): Observable<AuthenticationState> {
		return this.authenticationStateSubject.asObservable();
	}

	public beginLogOutProcess(): void {
		this.authenticationStateSubject.next({
			loginStatus: LoginStatus.LoggingOut,
		});
	}

	public clear(): void {
		this.authState(false);
		this.accessToken = undefined;
		this.appAccount = undefined;
		this.selectedTenant(null);
	}

	private authState(authState?: boolean): boolean {
		if (authState !== undefined) {
			this._authState = authState;
			localStorage.setItem('authState', authState ? 'true' : 'false');
			if (authState) {
				this.authenticationStateSubject.next({
					loginStatus: LoginStatus.LoggedIn,
				});
				this.ensureUser();
			} else {
				this.authenticationStateSubject.next({
					loginStatus: LoginStatus.LoggedOut,
				});
			}
		}
		if (this._authState === undefined) {
			this._authState =
				localStorage.getItem('authState') === 'true' ? true : false;
		}
		return this._authState;
	}

	public currentAccountIsAuthenticated(): boolean {
		return this.appAccount && this.appAccount.isAuthenticated;
	}

	//Should this be name @isAuthenticated@ instead?
	public hasAccessToken(): boolean {
		return Boolean(this.currentAuthenticationToken());
	}

	public currentAuthenticationToken(accessToken?: string): string {
		if (accessToken) {
			this.accessToken = accessToken;
		}
		return this.accessToken;
	}

	public userId(): Guid {
		if (
			this.appAccount &&
			this.appAccount.principal &&
			this.appAccount.principal.userId
		) {
			return this.appAccount.principal.userId;
		}
		return null;
	}

	public isLoggedIn(): boolean {
		return this.authState();
	}
	public isKeycloakLoggedIn(): boolean {
		return this.keycloakService.isLoggedIn();
	}
	public prepareAuthRequest(observable: Observable<string>, tenantCode: string, httpParams?: Object): Observable<boolean> {
		return observable.pipe(
			map((x) => this.currentAuthenticationToken(x)),
			concatMap(response => {
				return response ? this.ensureTenant(tenantCode ?? this.selectedTenant() ?? 'default') : null;
			}),
			concatMap(response => {
				return response ? this.principalService.me(httpParams) : null;
			}),
			concatMap((response) => forkJoin([
				//this.installationConfiguration.idpServiceEnabled ? this.idpPrincipalService.me(httpParams) : of(null),
				//this.installationConfiguration.userServiceEnabled ? this.userPrincipalService.me(httpParams) : of(null),
				response ? this.principalService.me() : of(null),
				response && this.installationConfiguration.notificationServiceEnabled ? this.notificationPrincipalService.me() : of(null),
				response && this.installationConfiguration.annotationServiceEnabled ? this.annotationPrincipalService.me() : of(null)
			])),
			concatMap(response => {
                if (response && response[1]) {
					this.currentNotificationAccount(response[1])
				}
				if (response && response[2]) {
					this.currentAnnotationAccount(response[2])
				}
				if (response && response[0]) {
					this.currentAccount(response[0])
				}
				return of(response?.[0]);
			}),
			concatMap(response => {
				return this.tenantHandlingService.loadTenantCssColors();
			}),
			concatMap(response => {
				this.tenantHandlingService.applyTenantCssColors(response?.cssColors ?? this.configurationService.cssColorsTenantConfiguration);
				return of(true);
			})
		);
	}

	private ensureUser() {
		this._appsUserNotAlreadyCreated = this.currentAccountIsAuthenticated() && !this.AppsUserExists();
		if (this._appsUserNotAlreadyCreated) {
			if (!this.overlayRef) {
				this.overlayRef = this.overlay.create({
					positionStrategy: this.overlay.position().global().centerHorizontally().centerVertically(),
					hasBackdrop: true
				});
				this.overlayRef.attach(new ComponentPortal(SpinnerComponent));
			}
			if (!this.appUserNotAlreadyCreatedSubscription) {
				this.appUserNotAlreadyCreatedSubscription = timer(1000, 1000)
					.pipe(takeUntil(this._destroyed))
					.subscribe(x => {
						this.refresh().pipe(takeUntil(this._destroyed), catchError(x => of(false))).subscribe(authenticationState => {
							this._appsUserNotAlreadyCreated = this.currentAccountIsAuthenticated() && !this.AppsUserExists();
							if (!this._appsUserNotAlreadyCreated) {
								this.appUserNotAlreadyCreatedSubscription?.unsubscribe();
								this.appUserNotAlreadyCreatedSubscription = null;
								if (this.overlayRef) {
									this.overlayRef.detach();
									this.overlayRef.dispose();
									this.overlayRef = null;
								}

							}
						});
					},
					error => {
						this.appUserNotAlreadyCreatedSubscription?.unsubscribe();
						this.appUserNotAlreadyCreatedSubscription = null;
						if (this.overlayRef) {
							this.overlayRef.detach();
							this.overlayRef.dispose();
							this.overlayRef = null;
						}
					});
			}
			return;
		}

		this.appUserNotAlreadyCreatedSubscription?.unsubscribe();
		this.appUserNotAlreadyCreatedSubscription = null;
		if (this.overlayRef) {
			this.overlayRef.detach();
			this.overlayRef.dispose();
			this.overlayRef = null;
		}
	}

	public appsNotAlreadyUserCreated() {
		return this._appsUserNotAlreadyCreated;
	}

	public AppsUserExists(): boolean {
		return this.NotificationUserExists() && this.AnnotationUserExists() && this.MainAppUserExists();
	}

	public MainAppUserExists(): boolean {
		return this.appAccount && this.appAccount.userExists;
	}

	public NotificationUserExists(): boolean {
		return !this.installationConfiguration.notificationServiceEnabled || (this.notificationAccount && this.notificationAccount.userExists);
	}

	public AnnotationUserExists(): boolean {
		return !this.installationConfiguration.annotationServiceEnabled || (this.annotationAccount && this.annotationAccount.userExists);
	}

	public refresh(): Observable<boolean> {
		return of({}).pipe(
			exhaustMap(() => forkJoin([
				this.principalService.me(),
				this.installationConfiguration.notificationServiceEnabled ? this.notificationPrincipalService.me() : of(null),
				this.installationConfiguration.annotationServiceEnabled ? this.annotationPrincipalService.me() : of(null)
			])),
			map((response) => {
				if (response && response[0]) {
					this.currentAccount(response[0])
				}
				if (response && response[1]) {
					this.currentNotificationAccount(response[1])
				}
				if (response && response[2]) {
					this.currentAnnotationAccount(response[2])
				}
				return true;
			}),
		);
	}

	private ensureTenant(tenantCode: string): Observable<string> {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		return this.principalService.myTenants({ params: params }).pipe(takeUntil(this._destroyed),
			map(
				(myTenants) => {
					if (myTenants) {

						if (myTenants.some(x => x.code.toLocaleLowerCase() == tenantCode.toLocaleLowerCase())) {
							this.selectedTenant(tenantCode);
						}
						else {
							this.selectedTenant(null);
						}
					}
					return this.selectedTenant();
				}

			)
		);
	}

	private currentAccount(
		appAccount: AppAccount
	): void {
		this.appAccount = appAccount;
		this.authState(true);
	}

	private currentNotificationAccount(
		account: NotificationAccount
	): void {
		this.notificationAccount = account;
	}

	private currentAnnotationAccount(
		account: AnnotationAccount
	): void {
		this.annotationAccount = account;
	}


	public getPrincipalName(): string {
		if (this.appAccount && this.appAccount.principal) {
			return this.appAccount.principal.name;
		}
		return null;
	}

	public getSelectedTenantName(): string {
		if (this.appAccount && this.appAccount.selectedTenant) {
			return this.appAccount.selectedTenant.name;
		}
		return null;
	}

	public getSelectedTenantId(): Guid {
		if (this.appAccount && this.appAccount.selectedTenant) {
			return this.appAccount.selectedTenant.id;
		}
		return null;
	}

	public getUserProfileEmail(): string {
		if (this.appAccount && this.appAccount.profile) {
			return this.appAccount.profile.email;
		}
		return null;
	}

	public getUserProfileLanguage(): string {
		if (this.appAccount && this.appAccount.profile) {
			return this.appAccount.profile.language;
		}
		return null;
	}

	public getUserProfileCulture(): string {
		if (this.appAccount && this.appAccount.profile) {
			return this.appAccount.profile.culture;
		}
		return null;
	}

	public getUserProfileAvatarUrl(): string {
		if (this.appAccount && this.appAccount.profile && !this.appAccount.profile.avatarUrl) {
			return this.appAccount.profile.avatarUrl;
		}
		return null;
	}

	public getUserProfileTimezone(): string {
		if (this.appAccount && this.appAccount.profile) {
			return this.appAccount.profile.timezone;
		}
		return null;
	}


	public authenticate(returnUrl: string): Observable<KeycloakEvent | null> {
		if (!this.keycloakService.isLoggedIn()) {
			this.keycloakService.login({
				scope: this.installationConfiguration.keycloak.scope,
			})
				.then(() => {
					return this.keycloakService.keycloakEvents$.pipe(
						tap((e) => {
							if (
								e.type === KeycloakEventType.OnTokenExpired
							) {
								this.refreshToken({}).then((isRefreshed) => {
									if (!isRefreshed) {
										this.clear();
									}

									return isRefreshed;
								}).catch(x => {
									this.clear();
									throw x;
								});
							}
						}),
					);
				})
				.catch((error) => this.onAuthenticateError(error));
		} else {
			this.zone.run(() => this.router.navigateByUrl(returnUrl));
            return of();
		}
	}

	public refreshToken(httpParams?: Object): Promise<boolean> {
		return this.keycloakService.updateToken().then((isRefreshed) => {
			if (!isRefreshed) {
				return false;
			}

			return this.prepareAuthRequest(
				from(this.keycloakService.getToken()),
				this.selectedTenant(),
				httpParams
			)
				.pipe(takeUntil(this._destroyed))
				.pipe(
					map(
						() => {
							return true;
						},
						(error) => {
							this.onAuthenticateError(error);
							return false;
						}
					)
				)
				.toPromise();
		});
	}

	onAuthenticateError(errorResponse: HttpErrorResponse) {
		this.zone.run(() => {
			this.uiNotificationService.snackBarNotification(
				errorResponse.message,
				SnackBarNotificationLevel.Warning
			);
		});
	}

	onAuthenticateSuccess(returnUrl: string): void {
		this.authState(true);
		this.zone.run(() => this.router.navigateByUrl(returnUrl));
	}

	public hasPermission(permission: AppPermission): boolean {
		// if (!this.installationConfiguration.appServiceEnabled) { return true; } //TODO: maybe reconsider
		return this.evaluatePermission(this.appAccount?.permissions || [], permission);

	}
	private evaluatePermission(availablePermissions: string[], permissionToCheck: string): boolean {
		if (!permissionToCheck) { return false; }
		return availablePermissions.map(x => x.toLowerCase()).includes(permissionToCheck.toLowerCase());
	}
	public hasAnyPermission(permissions: AppPermission[]): boolean {
		if (!permissions) { return false; }
		return permissions.filter((p) => this.hasPermission(p)).length > 0;
	}

	public authorize(context: ResolutionContext): boolean {

		if (!context) { return true; }

		let permissionAuthorized = false;
		if (context.permissions && context.permissions.length > 0) {
			permissionAuthorized = this.hasAnyPermission(context.permissions);
		}

		if (permissionAuthorized) { return true; }

		return false;
	}
}
