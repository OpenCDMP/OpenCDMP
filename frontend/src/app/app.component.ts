
import { of as observableOf, Subscription } from 'rxjs';

import { AfterViewInit, Component, effect, OnInit, Renderer2, ViewChild, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { LangChangeEvent, TranslateService, TranslatePipe } from '@ngx-translate/core';
import { filter, map, switchMap, takeUntil } from 'rxjs/operators';
import { AuthService, LoginStatus } from './core/services/auth/auth.service';
import { CultureService } from './core/services/culture/culture-service';
import { DomSanitizer, Title } from '@angular/platform-browser';
import { ConfigurationService } from './core/services/configuration/configuration.service';
import { LanguageService } from './core/services/language/language.service';

import { MatSidenav } from '@angular/material/sidenav';
import { MatomoService } from './core/services/matomo/matomo-service';
import { SideNavService } from './core/services/sidenav/side-nav.sevice';
import { TimezoneService } from './core/services/timezone/timezone-service';
import { BreadcrumbService } from './ui/misc/breadcrumb/breadcrumb.service';
import { TenantHandlingService } from './core/services/tenant/tenant-handling.service';
import { MatIconRegistry } from '@angular/material/icon';
import { CookieConsentService } from './core/services/cookie-consent/cookieconsent.service';
import { PlanWebSocketService } from './core/services/websocket/plan-websocket.service';
import { PrincipalService } from './core/services/http/principal.service';
import { BaseComponent } from '@common/base/base.component';
import { UserTermsAcceptanceService } from './core/services/http/user-terms-acceptance.service';
import { TermsAcceptanceDialogComponent } from './ui/misc/terms-acceptance-dialog/terms-acceptance-dialog.component';
import { A11yAnnouncerService } from './core/services/accessibility/a11y-announcer.service';
import { GuidedTourComponent } from './library/guided-tour/guided-tour.component';
import { NavbarComponent } from './ui/navbar/navbar.component';
import { SidebarComponent } from './ui/sidebar/sidebar.component';
import { NotificationComponent } from './library/notification/notification.component';


declare const gapi: any;
declare let $: any;

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [GuidedTourComponent, NavbarComponent, SidebarComponent, RouterOutlet, NotificationComponent, TranslatePipe]
})
export class AppComponent extends BaseComponent implements OnInit, AfterViewInit, OnDestroy {

	hasBreadCrumb = observableOf(false);
	private sideNavSubscription: Subscription;
	private statusChangeSubscription: Subscription;
	showOnlyRouterOutlet = false;
	cssConfigLoaded = false;

	@ViewChild('sidenav') sidenav: MatSidenav;

    private languageSignal = this.language.getCurrentLanguageSignal();

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		protected authentication: AuthService,
		private translate: TranslateService,
		private titleService: Title,
		private cultureService: CultureService,
		private timezoneService: TimezoneService,
		private language: LanguageService,
		private configurationService: ConfigurationService,
		private matomoService: MatomoService,
		private tenantHandlingService: TenantHandlingService,
		private breadcrumbService: BreadcrumbService,
		private sanitizer: DomSanitizer,
		private planWebSocketService: PlanWebSocketService,
		public iconRegistry: MatIconRegistry,
        private renderer: Renderer2,
        private cookieConsentService: CookieConsentService,
		private principalService: PrincipalService,
		private dialog: MatDialog,
		private userTermsAcceptanceService: UserTermsAcceptanceService,
		protected sidenavService: SideNavService,
		private announcer: A11yAnnouncerService,
	) {
		super();
		this.initializeServices();

		effect(() => {
			const termsStatus = this.authentication.termsAcceptanceStatus();
			if (termsStatus?.termsOfServiceAcceptanceRequired && termsStatus?.content) {
				this.openTermsAcceptanceDialog(termsStatus.content);
			}
		});
		this.matomoService.init();

		const paperPlaneIconSrc = this.sanitizer.bypassSecurityTrustResourceUrl('/assets/images/annotations/paper-plane.svg');
		iconRegistry.addSvgIcon('paperPlane', paperPlaneIconSrc);

        effect(() => {
            const language = this.languageSignal();
            const html = document.querySelector('html');
            html?.setAttribute('lang', language);
            // WCAG 1.3.2 / 3.1.x: keep document direction in sync with the language so
            // RTL locales render correctly. (Full Material mirroring would also need CDK
            // BidiModule/Dir — tracked as a follow-up.)
            const rtlCodes = ['ar', 'he', 'fa', 'ur', 'ps', 'sd', 'ug', 'yi', 'dv'];
            const base = (language || '').split('-')[0].toLowerCase();
            html?.setAttribute('dir', rtlCodes.includes(base) ? 'rtl' : 'ltr');
        })
	}
	ngAfterViewInit(): void {
        this.cookieConsentService.init();
	}

	onActivate(event: any) {
	}

	onDeactivate(event: any) {
	}

	ngOnInit() {
        this.translate.onLangChange.subscribe(() => this.cookieConsentService.setLanguage(this.translate.currentLang));

		this.hasBreadCrumb = this.router.events.pipe(
			filter(event => event instanceof NavigationEnd),
			map(() => this.route),
			map(route => route.firstChild),
			switchMap(route => route.data),
			map(data => data['breadcrumb']));

		// The browser-tab title is composed by AppTitleStrategy from the route breadcrumb
		// taxonomy. Here we only react to navigation for layout + accessibility: toggle the
		// router-outlet-only shell, then move focus to the main content and announce the new
		// page so screen-reader/keyboard users are taken to it (WCAG 2.4.3 / 4.1.3).
		this.router
			.events.pipe(
				filter(event => event instanceof NavigationEnd)
			).subscribe(() => {
				let child = this.route.firstChild;
				if (child != null) {
					while (child.firstChild) {
						child = child.firstChild;
					}
					this.showOnlyRouterOutlet = !!child.snapshot.data['showOnlyRouterOutlet'];
				}
				this.onPageChanged(this.currentPageLabel());
			});


		this.router
			.events.pipe(
				filter(event => event instanceof NavigationEnd)
			)
			.subscribe(() => {

				this.breadcrumbService.addExcludedParam('t', true);
				this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
					if (this.authentication.getSelectedTenantName() && this.authentication.getSelectedTenantName() !== '')
						this.principalService.myTenantsTranslated().pipe(takeUntil(this._destroyed)).subscribe( tenants => {
							const tenant = tenants?.find(x => x.id === this.authentication.getSelectedTenantId());
							if (tenant) {
								this.breadcrumbService.addIdResolvedValue(this.authentication.selectedTenant(), this.principalService.getTranslatedTenantName(tenant));
							}
						});
						
				});

				if (this.authentication.getSelectedTenantName() && this.authentication.getSelectedTenantName() !== '')
					this.breadcrumbService.addIdResolvedValue(this.authentication.selectedTenant(), this.authentication.getSelectedTenantName());
			});
        if(this.configurationService.userWayId){
            const script = this.renderer.createElement('script');
            script.src = 'https://cdn.userway.org/widget.js';
            script.setAttribute('data-account', this.configurationService.userWayId);
            script.defer = true;
            this.renderer.appendChild(document.body, script);
        }
	}

	/**
	 * The human page label to announce on navigation — the current document title with the
	 * "OpenCDMP - " brand prefix stripped (the prefix is noise for screen-reader users).
	 * The title itself is set by AppTitleStrategy before NavigationEnd fires.
	 */
	private currentPageLabel(): string {
		const full = this.titleService.getTitle();
		const prefix = this.translate.instant('GENERAL.TITLES.PREFIX');
		return prefix && full?.startsWith(prefix) ? full.slice(prefix.length) : full;
	}

	/**
	 * On client-side navigation, move focus to the main content region and announce
	 * the new page to assistive tech (WCAG 2.4.3 / 4.1.3). #main-page is `tabindex="-1"`
	 * so it is programmatically focusable without being a tab stop.
	 */
	private onPageChanged(pageTitle: string) {
		const main = document.getElementById('main-page');
		main?.focus({ preventScroll: true });
		this.announcer.announce(pageTitle, 'polite');
	}

	ngOnDestroy() {
		super.ngOnDestroy();
		this.statusChangeSubscription?.unsubscribe();
		if (this.sideNavSubscription) {
			this.sideNavSubscription.unsubscribe();
		}
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	private openTermsAcceptanceDialog(content: string): void {
		const dialogRef = this.dialog.open(TermsAcceptanceDialogComponent, {
			disableClose: true,
			data: { content },
			maxWidth: '1000px',
			width: '100%',
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(accepted => {
			if (accepted) {
				this.userTermsAcceptanceService.accept()
					.pipe(takeUntil(this._destroyed))
					.subscribe({ error: () => {} });
			}
		});
	}

	initializeServices() {

		if (!this.authentication.currentAccountIsAuthenticated() && this.configurationService.cssColorsTenantConfiguration) {
			this.tenantHandlingService.applyTenantCssColors(this.configurationService.cssColorsTenantConfiguration);
		}

		this.translate.setDefaultLang(this.language.getDefaultLanguagesCode());
		this.authentication.currentAccountIsAuthenticated() && this.authentication.getUserProfileCulture() ? this.cultureService.cultureSelected(this.authentication.getUserProfileCulture()) : this.cultureService.cultureSelected(this.configurationService.defaultCulture);
		this.authentication.currentAccountIsAuthenticated() && this.authentication.getUserProfileTimezone() ? this.timezoneService.timezoneSelected(this.authentication.getUserProfileTimezone()) : this.timezoneService.timezoneSelected(this.configurationService.defaultTimezone);
		const profileLanguage = this.authentication.currentAccountIsAuthenticated() ? this.authentication.getUserProfileLanguage() : null;
		this.language.changeLanguage(profileLanguage ?? this.language.getDefaultLanguagesCode());

		if (this.authentication.currentAccountIsAuthenticated()) {
			this.planWebSocketService.connect();
		}

		this.authentication.getAuthenticationStateObservable().subscribe(authenticationState => {
			if (authenticationState.loginStatus === LoginStatus.LoggedIn) {
				this.tenantHandlingService.loadAndApplyTenantCssColors();
			}
		});
	}

	toggleNavbar(event) {
		document.getElementById('hamburger').classList.toggle("change");
	}

    skipToMain() {
        document.getElementById('main-page')?.focus();
	}

}

