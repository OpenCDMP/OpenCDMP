import { Location } from '@angular/common';
import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatMenuTrigger } from '@angular/material/menu';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
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
import { SideNavService } from '@app/core/services/sidenav/side-nav.sevice';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { UserService } from '@app/core/services/user/user.service';
import { BaseComponent } from '@common/base/base.component';
import { InAppNotificationService } from '@notification-service/services/http/inapp-notification.service';
import { MineInAppNotificationListingDialogComponent } from '@notification-service/ui/inapp-notification/listing-dialog/mine-inapp-notification-listing-dialog.component';
import { timer } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { StartNewPlanDialogComponent } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.component';
import { FaqDialogComponent } from '../faq/dialog/faq-dialog.component';
import { UserDialogComponent } from './user-dialog/user-dialog.component';
// import { TranslateService } from '@ngx-translate/core';
// import { FontAccessibilityService } from '@app/core/services/font-accessibility.service';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css', './navbar.component.scss'],
    standalone: false
})
export class NavbarComponent extends BaseComponent implements OnInit {
	userName: string = '';
	progressIndication = false;
	location: Location;
	mobile_menu_visible: any = 0;
	private toggleButton: any;
	private sidebarVisible: boolean;
	languages = [];
	currentRoute: string;
	selectedLanguage: string;
	private user: User;
	inAppNotificationDialog: MatDialogRef<MineInAppNotificationListingDialogComponent> = null;
	inAppNotificationCount = 0;
	@Output() sidebarToggled: EventEmitter<any> = new EventEmitter();
	@ViewChild(MatMenuTrigger) trigger: MatMenuTrigger;
	extraImageURL: SafeUrl;

	constructor(location: Location,
		public routerUtils: RouterUtilsService,
		public authentication: AuthService,
		private router: Router,
		private userService: UserService,
		private dialog: MatDialog,
		private progressIndicationService: ProgressIndicationService,
		private languageService: LanguageService,
		private sidenavService: SideNavService,
		private tenantConfigurationService: TenantConfigurationService,
		private inappNotificationService: InAppNotificationService,
		public configurationService: ConfigurationService,
		private storageFileService: StorageFileService,
		private sanitizer: DomSanitizer,
		private analyticsService: AnalyticsService,
        // private fontService: FontAccessibilityService,
        // private language: TranslateService
	) {
		super();
		this.location = location;
		this.sidebarVisible = false;
		this.languages = this.languageService.getAvailableLanguagesCodes();
		this.selectedLanguage = this.languageService.getDefaultLanguagesCode();
        router.events.subscribe(() => {
            if(window.innerWidth <= 500){
                this.sidenavService.setStatus(false);
            }
        })
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
		this.authentication.getAuthenticationStateObservable().subscribe(authenticationState => {
			if (authenticationState.loginStatus === LoginStatus.LoggedIn) {
				this.loadLogo();
				this.loadUser();
			}
		});
		this.loadLogo();
		this.loadUser();
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

	sidebarOpen() {
		const toggleButton = this.toggleButton;
		const body = document.getElementsByTagName('body')[0];
		setTimeout(function () {
			toggleButton.classList.add('toggled');
		}, 500);

		body.classList.add('nav-open');

		this.sidebarVisible = true;
	};
	sidebarClose() {
		const body = document.getElementsByTagName('body')[0];
		this.toggleButton.classList.remove('toggled');
		this.sidebarVisible = false;
		body.classList.remove('nav-open');
	};
	sidebarToggle() {
		var $toggle = document.getElementsByClassName('navbar-toggler')[0];

		if (this.sidebarVisible === false) {
			this.sidebarOpen();
		} else {
			this.sidebarClose();
		}
		const body = document.getElementsByTagName('body')[0];

		if (this.mobile_menu_visible == 1) {
			// $('html').removeClass('nav-open');
			body.classList.remove('nav-open');
			if ($layer) {
				$layer.remove();
			}
			setTimeout(function () {
				$toggle.classList.remove('toggled');
			}, 400);

			this.mobile_menu_visible = 0;
		} else {
			setTimeout(function () {
				$toggle.classList.add('toggled');
			}, 430);

			var $layer = document.createElement('div');
			$layer.setAttribute('class', 'close-layer');


			if (body.querySelectorAll('.main-panel')) {
				document.getElementsByClassName('main-panel')[0].appendChild($layer);
			} else if (body.classList.contains('off-canvas-sidebar')) {
				document.getElementsByClassName('wrapper-full-page')[0].appendChild($layer);
			}

			setTimeout(function () {
				$layer.classList.add('visible');
			}, 100);

			$layer.onclick = function () { //asign a function
				body.classList.remove('nav-open');
				this.mobile_menu_visible = 0;
				$layer.classList.remove('visible');
				setTimeout(function () {
					$layer.remove();
					$toggle.classList.remove('toggled');
				}, 400);
			}.bind(this);

			body.classList.add('nav-open');
			this.mobile_menu_visible = 1;

		}
	};

	public getCurrentLanguage(): any {
		const lang = this.languages.find(lang => lang.value === this.languageService.getCurrentLanguage());
		return lang;
	}

	public getPrincipalName(): string {
		return this.authentication.getPrincipalName();
	}

	public principalHasAvatar(): boolean {
		return this.authentication.getUserProfileAvatarUrl() != null && this.authentication.getUserProfileAvatarUrl().length > 0;
	}

	public getPrincipalAvatar(): string {
		return this.authentication.getUserProfileAvatarUrl();
	}

	public getDefaultAvatar(): string {
		return 'assets/images/profile-placeholder.png';
	}

	public applyFallbackAvatar(ev: Event) {
		(ev.target as HTMLImageElement).src = this.getDefaultAvatar();
	}

	openProfile() {
		const dialogRef = this.dialog.open(UserDialogComponent, {
			hasBackdrop: true,
			closeOnNavigation: true,
			disableClose: false,
			position: { top: '71px', right: '1em' },
			panelClass: 'custom-userbox'
		});
	}

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

	sidebarToggleOutput(event) {
		this.sidebarToggled.emit(event);
	}

	toggleMyNav(event) {
		this.sidenavService.toggle();
	}

	openNewPlanDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		} else if (!this.isAuthenticated()) {
			this.router.navigate(['/login']);
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
				position: { top: '71px', right: '4.8em' },
				minWidth: "min(600px, 90vw)",
                maxWidth: '90vw'
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
