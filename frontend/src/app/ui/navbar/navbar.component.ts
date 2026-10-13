import { Location, NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, ViewChild, inject, ChangeDetectionStrategy } from '@angular/core';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { LogoTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { User } from '@app/core/model/user/user';
import { AuthService, LoginStatus } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { ProgressIndicationService } from '@app/core/services/progress-indication/progress-indication-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { UserService } from '@app/core/services/user/user.service';
import { BaseComponent } from '@common/base/base.component';
import { InAppNotificationService } from '@notification-service/services/http/inapp-notification.service';
import { MineInAppNotificationListingDialogComponent } from '@notification-service/ui/inapp-notification/listing-dialog/mine-inapp-notification-listing-dialog.component';
import { timer } from 'rxjs';
import { map, startWith, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { FaqDialogComponent } from '../faq/dialog/faq-dialog.component';
import { AccessLevel, SidebarItem } from '@app/core/model/configuration-models/sidebar.model';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { Tenant } from '@app/core/model/tenant/tenant';
import { SideNavService } from '@app/core/services/sidenav/side-nav.sevice';
import { StartNewPlanDialogComponent } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.component';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { LanguageComponent } from '../language/language-content/language.component';
import { MatTooltip } from '@angular/material/tooltip';
import { MatBadge } from '@angular/material/badge';
import { MatProgressBar } from '@angular/material/progress-bar';
import { TenantSwitchComponent } from '../tenant/tenant-switch/tenant-switch.component';
import { MatDivider } from '@angular/material/list';
import { FallbackAvatarDirective } from '../../../common/http/image/fallback-avatar.directive';
import { MatCardAvatar } from '@angular/material/card';
import { TranslatePipe } from '@ngx-translate/core';
import { UserLogoPipe } from '../../../common/http/image/user-logo.pipe';
// import { TranslateService } from '@ngx-translate/core';
// import { FontAccessibilityService } from '@app/core/services/font-accessibility.service';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css', './navbar.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [RouterLink, MatIconButton, MatMenuTrigger, MatIcon, NgTemplateOutlet, MatMenu, LanguageComponent, MatTooltip, MatBadge, MatProgressBar, MatMenuItem, TenantSwitchComponent, MatDivider, MatButton, FallbackAvatarDirective, MatCardAvatar, TranslatePipe, UserLogoPipe]
})
export class NavbarComponent extends BaseComponent implements OnInit {
	userName: string = '';
	progressIndication = false;
	location: Location;
	mobile_menu_visible: any = 0;
	languages = [];
	currentRoute: string;
	selectedLanguage: string;
	private user: User;
	inAppNotificationDialog: MatDialogRef<MineInAppNotificationListingDialogComponent> = null;
	inAppNotificationCount = 0;
	@ViewChild(MatMenuTrigger) trigger: MatMenuTrigger;
	extraImageURL: SafeUrl;

    supportItems: SidebarItem[] = [];
	/** "Take a tour" in the Support menu replays the tour registered by the current page, if any. */
	protected readonly tourState = inject(TourStateService);

	constructor(location: Location,
		protected routerUtils: RouterUtilsService,
		protected authentication: AuthService,
		protected configurationService: ConfigurationService,
		private router: Router,
		private userService: UserService,
		private dialog: MatDialog,
		private progressIndicationService: ProgressIndicationService,
		private languageService: LanguageService,
		private tenantConfigurationService: TenantConfigurationService,
		private inappNotificationService: InAppNotificationService,
		private storageFileService: StorageFileService,
		private sanitizer: DomSanitizer,
		private analyticsService: AnalyticsService,
        private principalService: PrincipalService,
		private sidenavService: SideNavService
        // private fontService: FontAccessibilityService,
        // private language: TranslateService
	) {
		super();
		this.location = location;
		this.languages = this.languageService.getAvailableLanguagesCodes();
		this.selectedLanguage = this.languageService.getDefaultLanguagesCode();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Navbar);

		this.currentRoute = this.router.url;

		this.progressIndicationService.getProgressIndicationObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
			setTimeout(() => { this.progressIndication = x; });
		});
		timer(2000, this.configurationService.inAppNotificationsCountInterval * 1000)
			.pipe(takeUntil(this._destroyed))
			.subscribe(x => {
				this.countUnreadInappNotifications();
			});
		this.authentication.getAuthenticationStateObservable()
        .pipe(
            map((state) => state.loginStatus),
            startWith(this.isAuthenticated() ? LoginStatus.LoggedIn : LoginStatus.LoggedOut)
        )
        .subscribe(loginStatus => {
			if (loginStatus === LoginStatus.LoggedIn) {
                this.loadUserTenants();
				this.loadLogo();
				this.loadUser();
			}
            const flattenedFooterItems = this.configurationService.sidebar?.footerItems ?? [];
            const flattenedInfoItems = this.configurationService?.sidebar?.infoItems ?? [];
            const flattenedSupportItems = [...flattenedInfoItems, ...flattenedFooterItems]
            
            this.supportItems = flattenedSupportItems.filter(x => {
                switch(x.accessLevel){
                    case AccessLevel.Authenticated: return this.isAuthenticated();
                    case AccessLevel.Unauthenticated: return !this.isAuthenticated();
                    default: return true;
                }
            })
		});
	}

    tenants: Tenant[] = [];
    loadUserTenants() {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		this.principalService.myTenantsTranslated({ params: params })
        .pipe(takeUntil(this._destroyed))
        .subscribe((res) => this.tenants = res )
	}
    get currentTenant(): string{
        if(!this.tenants?.length || !this.authentication.selectedTenant()){ return;}
        return this.tenants.find(x => x.code == this.authentication.selectedTenant())?.name;
    }


	private loadUser() {
		if (this.authentication.currentAccountIsAuthenticated() && this.authentication.userId()) {
			this.userService.getSingle(this.authentication.userId(), [
				nameof<User>(x => x.id),
				nameof<User>(x => x.name)
			]).subscribe(u => this.userName = u.name); //TODO HANDLE-ERRORS
		}
	}

	private loadLogo() {
		if (this.authentication.currentAccountIsAuthenticated() && this.authentication.selectedTenant()) {
			this.tenantConfigurationService.getActiveType(TenantConfigurationType.Logo, [
				nameof<TenantConfiguration>(x => x.type),
				[nameof<TenantConfiguration>(x => x.logo), nameof<LogoTenantConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			])
				.pipe(map(data => data as TenantConfiguration), takeUntil(this._destroyed))
				.subscribe( //TODO HANDLE-ERRORS
					data => {
						if (data?.logo?.storageFile?.id) {
							this.storageFileService.download(data?.logo?.storageFile?.id).pipe(takeUntil(this._destroyed))
								.subscribe(response => { //TODO HANDLE-ERRORS
									const blob = new Blob([response.body]);
									this.extraImageURL = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body))
								});
						}
					},
				);
		}
	}

	private countUnreadInappNotifications() {
		if (this.isAuthenticated()) {
			this.inappNotificationService.countUnread()
				.pipe(takeUntil(this._destroyed))
				.subscribe(
					data => {
						this.inAppNotificationCount = data;
					},
				);
		}
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	public onInvalidUrl(): boolean {
		return this.currentRoute === '/language-editor' || this.currentRoute === '/profile';
	}

	public getCurrentLanguage(): any {
		const lang = this.languages.find(lang => lang.value === this.languageService.getCurrentLanguage());
		return lang;
	}

	public getPrincipalName(): string {
		return this.authentication.getPrincipalName();
	}

	// public principalHasAvatar(): boolean {
	// 	return this.authentication.getUserProfileAvatarUrl() != null && this.authentication.getUserProfileAvatarUrl().length > 0;
	// }

	// public getPrincipalAvatar(): string {
	// 	return this.authentication.getUserProfileAvatarUrl();
	// }

	// openProfile() {
	// 	const dialogRef = this.dialog.open(UserDialogComponent, {
	// 		hasBackdrop: true,
	// 		closeOnNavigation: true,
	// 		disableClose: false,
	// 		position: { top: '71px', right: '1em' },
	// 		panelClass: 'custom-userbox'
	// 	});
	// }

	openFaqDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(FaqDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true
				},
				width: '80%'
			});
		}
	}

	getLanguage(selectedLanguage: string) {
		this.selectedLanguage = selectedLanguage;
	}

	toggleNavbar(event) {
		document.getElementById('hamburger').classList.toggle("change");
	}


	toggleMyNav(event) {
		this.sidenavService.toggle();
	}

	openNewPlanDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		} else if (!this.isAuthenticated()) {
			this.login();
		} else {
			const dialogRef = this.dialog.open(StartNewPlanDialogComponent, {
				disableClose: false,
				data: {
					isDialog: true
				},
                maxWidth: 'min(95vw, 33rem)'
			});
		}
	}

	login() {
		this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
	}

	logout() {
		this.router.navigate(['/logout']);
	}

	toggleInAppNotifications() {
		if (this.inAppNotificationDialog != null) {
			this.inAppNotificationDialog.close();
		} else {
			this.countUnreadInappNotifications();
			this.inAppNotificationDialog = this.dialog.open(MineInAppNotificationListingDialogComponent, {
				hasBackdrop: true,
				closeOnNavigation: true,
				disableClose: false,
                maxHeight: 'min(500px, 90vh)',
				position: { top: '70px', right: '25px' },
				width: "min(400px, 90vw)",
                data: {
                    unreadCount: this.inAppNotificationCount
                }
			});
			const onReadAllSubscription = this.inAppNotificationDialog.componentInstance.onReadAll.subscribe(() => {
				this.countUnreadInappNotifications();
			});
			this.inAppNotificationDialog.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				this.countUnreadInappNotifications();
				this.inAppNotificationDialog = null;
				onReadAllSubscription.unsubscribe();
			});
		}
	}

    // get toggleFontSizeTooltip(): string {
    //     return this.language.instant('NAV-BAR.TOGGLE-TEXT-SIZE') + this.language.instant(this.fontService.accessibleFontSignal() ? 'NAV-BAR.SMALLER' : 'NAV-BAR.LARGER') 
    // }

    // toggleFontSize() {
    //     this.fontService.toggleFontSize();
    // }

}
