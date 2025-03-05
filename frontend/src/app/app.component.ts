
import { of as observableOf, Subscription, timer } from 'rxjs';

import { AfterViewInit, Component, effect, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { filter, map, switchMap } from 'rxjs/operators';
import { AuthService, LoginStatus } from './core/services/auth/auth.service';
import { CultureService } from './core/services/culture/culture-service';
import { DomSanitizer, Title } from '@angular/platform-browser';
import { CookieService } from "ngx-cookie-service";
import { NgcCookieConsentService, NgcStatusChangeEvent } from "ngx-cookieconsent";
import { ConfigurationService } from './core/services/configuration/configuration.service';
import { LanguageService } from './core/services/language/language.service';

import { MatSidenav } from '@angular/material/sidenav';
import { MatomoService } from './core/services/matomo/matomo-service';
import { SideNavService } from './core/services/sidenav/side-nav.sevice';
import { TimezoneService } from './core/services/timezone/timezone-service';
import { BreadcrumbService } from './ui/misc/breadcrumb/breadcrumb.service';
import { TenantHandlingService } from './core/services/tenant/tenant-handling.service';
import { MatIconRegistry } from '@angular/material/icon';


declare const gapi: any;
declare var $: any;

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent implements OnInit, AfterViewInit {

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
		private cookieService: CookieService,
		private ccService: NgcCookieConsentService,
		private language: LanguageService,
		private configurationService: ConfigurationService,
		private matomoService: MatomoService,
		private tenantHandlingService: TenantHandlingService,
		private sidenavService: SideNavService,
		private breadcrumbService: BreadcrumbService,
		private sanitizer: DomSanitizer,
		public iconRegistry: MatIconRegistry,
	) {
		this.initializeServices();
		this.matomoService.init();

		const paperPlaneIconSrc = this.sanitizer.bypassSecurityTrustResourceUrl('/assets/images/annotations/paper-plane.svg');
		iconRegistry.addSvgIcon('paperPlane', paperPlaneIconSrc);

        effect(() => {
            const language = this.languageSignal();
            document.querySelector('html')?.setAttribute('lang', language);
        })
	}
	ngAfterViewInit(): void {
		setTimeout(() => {
			this.sideNavSubscription = this.sidenavService.status().subscribe(isopen => {
				const hamburger = document.getElementById('hamburger');
				if (isopen) {
					//update value of hamburfer
					if (!hamburger) {//try later
						setTimeout(() => {
							const hamburger = document.getElementById('hamburger');
							if (hamburger) {
								hamburger.classList.add('change');
							}
						}, 300);
					} else {
						hamburger.classList.add('change');
					}
					this.sidenav.open()
				} else {//closed
					if (!hamburger) {//try later
						setTimeout(() => {
							const hamburger = document.getElementById('hamburger');
							if (hamburger) {
								hamburger.classList.remove('change');
							}
						}, 300);
					} else {
						hamburger.classList.remove('change');
					}
					this.sidenav.close();

				}
			});
		});
	}

	onActivate(event: any) {
	}

	onDeactivate(event: any) {
	}

	ngOnInit() {
		if (!this.cookieService.check("cookiesConsent")) {
			this.cookieService.set("cookiesConsent", "false", 356, null, null, false, 'Lax');

		}

		this.hasBreadCrumb = this.router.events.pipe(
			filter(event => event instanceof NavigationEnd),
			map(() => this.route),
			map(route => route.firstChild),
			switchMap(route => route.data),
			map(data => data['breadcrumb']));

		const appTitle = this.titleService.getTitle();
		this.router
			.events.pipe(
				filter(event => event instanceof NavigationEnd),
				map(() => {
					let child = this.route.firstChild;
					if (child != null) {
						while (child.firstChild) {
							child = child.firstChild;
						}
						if (child.snapshot.data && child.snapshot.data.showOnlyRouterOutlet) {
							this.showOnlyRouterOutlet = true;
							this.ccService.getConfig().enabled = false;
                            this.ccService.getConfig().container = document.getElementById('cookies-consent');
							this.ccService.destroy();
							this.ccService.init(this.ccService.getConfig());
						} else {
							this.showOnlyRouterOutlet = false;
							if (this.cookieService.get("cookiesConsent") == "true") {
								this.ccService.getConfig().enabled = false;
							} else {
								this.ccService.getConfig().enabled = true;
							}
                            this.ccService.getConfig().container = document.getElementById('cookies-consent');
							this.ccService.destroy();
							this.ccService.init(this.ccService.getConfig());
						}

						const usePrefix = child.snapshot.data['usePrefix'] ?? true;
						if (child.snapshot.data['getFromTitleService']) {
							return { title: this.titleService.getTitle(), usePrefix: usePrefix };
						}
						else if (child.snapshot.data['title']) {
							return { title: child.snapshot.data['title'], usePrefix: usePrefix };
						}
					}
					return { title: appTitle, usePrefix: true };
				})
			).subscribe((titleOptions: { title: string, usePrefix: boolean }) => {
				this.translateTitle(titleOptions.title, titleOptions.usePrefix);
				this.translate.onLangChange.subscribe(() => this.translateTitle(titleOptions.title, titleOptions.usePrefix));
			});


		this.router
			.events.pipe(
				filter(event => event instanceof NavigationEnd)
			)
			.subscribe(() => {

				this.breadcrumbService.addExcludedParam('t', true);

				if (this.authentication.getSelectedTenantName() && this.authentication.getSelectedTenantName() !== '')
					this.breadcrumbService.addIdResolvedValue(this.authentication.selectedTenant(), this.authentication.getSelectedTenantName());
			});

		this.statusChangeSubscription = this.ccService.statusChange$.subscribe((event: NgcStatusChangeEvent) => {
			if (event.status == "dismiss") {
				this.cookieService.set("cookiesConsent", "true", 356, null, null, false, 'Lax');
			}
		});

		this.ccService.getConfig().content.href = this.configurationService.app + "cookies-policy";
		this.ccService.getConfig().cookie.domain = this.configurationService.app;
		this.translate
			.get(['COOKIE.MESSAGE', 'COOKIE.DISMISS', 'COOKIE.DENY', 'COOKIE.LINK', 'COOKIE.POLICY'])
			.subscribe(data => {
				this.ccService.getConfig().content = this.ccService.getConfig().content || {};
				// Override default messages with the translated ones
				this.ccService.getConfig().content.message = data['COOKIE.MESSAGE'];
				this.ccService.getConfig().content.dismiss = data['COOKIE.DISMISS'];
				this.ccService.getConfig().content.deny = data['COOKIE.DENY'];
				this.ccService.getConfig().content.link = data['COOKIE.LINK'];
				this.ccService.getConfig().content.policy = data['COOKIE.POLICY'];

				if (this.cookieService.get("cookiesConsent") == "true") {
					this.ccService.getConfig().enabled = false;
				}
				this.ccService.destroy();
				this.ccService.init(this.ccService.getConfig());
			});
	}

	translateTitle(ttl: string, usePrefix: boolean) {
		if (ttl.length > 0) {
			this.translate.get(ttl).subscribe((translated: string) => {
				if (usePrefix) {
					this.translate.get('GENERAL.TITLES.PREFIX').subscribe((titlePrefix: string) => {
						this.titleService.setTitle(titlePrefix + translated);
					});
				} else {
					this.titleService.setTitle(translated);
				}
			});
		} else {
			this.translate.get('GENERAL.TITLES.GENERAL').subscribe((translated: string) => {
				this.titleService.setTitle(translated);
			});
		}
	}

	ngOnDestroy() {
		this.statusChangeSubscription.unsubscribe();
		if (this.sideNavSubscription) {
			this.sideNavSubscription.unsubscribe();
		}
	}

	login() {
		//redirect to login page
		this.router.navigate(['/login'], { queryParams: { /*refresh : Math.random() ,returnUrl: this.state.url*/ } });
	}

	logout() {

	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	initializeServices() {

		if (!this.authentication.currentAccountIsAuthenticated() && this.configurationService.cssColorsTenantConfiguration) {
			this.tenantHandlingService.applyTenantCssColors(this.configurationService.cssColorsTenantConfiguration);
		}

		this.translate.setDefaultLang(this.language.getDefaultLanguagesCode());
		this.authentication.currentAccountIsAuthenticated() && this.authentication.getUserProfileCulture() ? this.cultureService.cultureSelected(this.authentication.getUserProfileCulture()) : this.cultureService.cultureSelected(this.configurationService.defaultCulture);
		this.authentication.currentAccountIsAuthenticated() && this.authentication.getUserProfileTimezone() ? this.timezoneService.timezoneSelected(this.authentication.getUserProfileTimezone()) : this.timezoneService.timezoneSelected(this.configurationService.defaultTimezone);
		this.authentication.currentAccountIsAuthenticated() && this.authentication.getUserProfileLanguage() ? this.language.changeLanguage(this.authentication.getUserProfileLanguage()) : (this.language.getDefaultLanguagesCode());

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

