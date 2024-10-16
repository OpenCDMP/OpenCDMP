import { Location } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth/auth.service';
import { LanguageDialogComponent } from '../language/dialog/language-dialog.component';
import { UserDialogComponent } from '../navbar/user-dialog/user-dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { AccessLevel } from '@app/core/model/configuration-models/sidebar.model';

enum RouteType {
	System = 0,
	Configurable = 1,
}
declare interface RouteInfo {
	path: string;
	title: string;
	icon: string;
	externalUrl?: string;
	routeType: RouteType;
}
class GroupMenuItem {
	title: string;
	routes: RouteInfo[];
}

@Component({
	selector: 'app-sidebar',
	templateUrl: './sidebar.component.html',
	styleUrls: ['./sidebar.component.css', './sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
	generalItems: GroupMenuItem;
	planItems: GroupMenuItem;
	adminItems: GroupMenuItem;
	descriptionItems: GroupMenuItem;
	grantItems: GroupMenuItem;
	publicItems: GroupMenuItem;
	infoItems: GroupMenuItem;
	groupMenuItems: GroupMenuItem[] = [];
	currentRoute: string;
	routeType = RouteType;

	constructor(
		public translate: TranslateService,
		private authentication: AuthService,
		private dialog: MatDialog,
		public router: Router,
		private location: Location,
		private analyticsService: AnalyticsService,
		public routerUtils: RouterUtilsService,
		private configurationService: ConfigurationService,
	) { }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Sidebar);

		this.currentRoute = this.router.url;

		this.authentication.getAuthenticationStateObservable().pipe().subscribe(authenticationState => {
			this.reCalculateMenu()
		});

		this.reCalculateMenu();

		this.router.events.subscribe((event) => this.currentRoute = this.router.url);
	}

	private reCalculateMenu() {
		this.groupMenuItems = []
		this.generalItems = {
			title: 'SIDE-BAR.GENERAL',
			routes: [],
		}
		this.generalItems.routes.push({ path: '/home', title: 'SIDE-BAR.DASHBOARD', icon: 'home', routeType: RouteType.System });

		this.groupMenuItems.push(this.generalItems);

		this.planItems = {
			title: 'SIDE-BAR.PLAN',
			routes: [],
		}

		if (this.authentication.hasPermission(AppPermission.ViewMyPlanPage)) this.planItems.routes.push({ path: '/plans', title: 'SIDE-BAR.MY-PLANS', icon: 'library_books', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewMyDescriptionPage)) this.planItems.routes.push({ path: '/descriptions', title: 'SIDE-BAR.MY-DESCRIPTIONS', icon: 'dns', routeType: RouteType.System });
		this.groupMenuItems.push(this.planItems);

		this.descriptionItems = {
			title: 'SIDE-BAR.DESCRIPTIONS',
			routes: [],
		}

		if (this.authentication.hasPermission(AppPermission.ViewPublicPlanPage)) this.descriptionItems.routes.push({ path: '/explore-plans', title: 'SIDE-BAR.PUBLIC-PLANS', icon: 'library_books', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewPublicDescriptionPage)) this.descriptionItems.routes.push({ path: '/explore-descriptions', title: 'SIDE-BAR.PUBLIC-DESC', icon: 'dns', routeType: RouteType.System });
		this.groupMenuItems.push(this.descriptionItems);

		this.publicItems = {
			title: 'SIDE-BAR.PUBLIC',
			routes: [],
		}
		if (!this.isAuthenticated()) {
			this.publicItems.routes.push({ path: '/explore-plans', title: 'SIDE-BAR.PUBLIC-PLANS', icon: 'library_books', routeType: RouteType.System });
			this.publicItems.routes.push({ path: '/explore-descriptions', title: 'SIDE-BAR.PUBLIC-DESC', icon: 'dns', routeType: RouteType.System });
		}
		this.groupMenuItems.push(this.publicItems);

		this.adminItems = {
			title: 'SIDE-BAR.ADMIN',
			routes: [],
		}
		if (this.authentication.hasPermission(AppPermission.ViewPlanBlueprintPage)) this.adminItems.routes.push({ path: '/plan-blueprints', title: 'SIDE-BAR.PLAN-BLUEPRINTS', icon: 'library_books', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewDescriptionTemplatePage)) this.adminItems.routes.push({ path: '/description-templates', title: 'SIDE-BAR.DESCRIPTION-TEMPLATES', icon: 'description', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewDescriptionTemplateTypePage)) this.adminItems.routes.push({ path: '/description-template-type', title: 'SIDE-BAR.DESCRIPTION-TEMPLATE-TYPES', icon: 'stack', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewEntityLockPage)) this.adminItems.routes.push({ path: '/entity-locks', title: 'SIDE-BAR.ENTITY-LOCKS', icon: 'lock_person', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewReferencePage)) this.adminItems.routes.push({ path: '/references', title: 'SIDE-BAR.REFERENCES', icon: 'dataset_linked', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewReferenceTypePage)) this.adminItems.routes.push({ path: '/reference-type', title: 'SIDE-BAR.REFERENCE-TYPES', icon: 'add_link', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewPrefillingSourcePage)) this.adminItems.routes.push({ path: '/prefilling-sources', title: 'SIDE-BAR.PREFILLING-SOURCES', icon: 'quick_reference_all', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewUsageLimitPage)) this.adminItems.routes.push({ path: '/usage-limits', title: 'SIDE-BAR.USAGE-LIMITS', icon: 'quick_reference_all', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewTenantPage)) this.adminItems.routes.push({ path: '/tenants', title: 'SIDE-BAR.TENANTS', icon: 'tenancy', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewTenantConfigurationPage)) this.adminItems.routes.push({ path: '/tenant-configuration', title: 'SIDE-BAR.TENANT-CONFIGURATION', icon: 'settings', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewUserPage)) this.adminItems.routes.push({ path: '/users', title: 'SIDE-BAR.USERS', icon: 'people', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewTenantUserPage)) this.adminItems.routes.push({ path: '/tenant-users', title: 'SIDE-BAR.TENANT-USERS', icon: 'people', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewLanguagePage)) this.adminItems.routes.push({ path: '/languages', title: 'SIDE-BAR.LANGUAGES', icon: 'language', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewSupportiveMaterialPage)) this.adminItems.routes.push({ path: '/supportive-material', title: 'SIDE-BAR.SUPPORTIVE-MATERIAL', icon: 'help_center', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewNotificationTemplatePage)) this.adminItems.routes.push({ path: '/notification-templates', title: 'SIDE-BAR.NOTIFICATION-TEMPLATES', icon: 'grid_guides', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewNotificationPage)) this.adminItems.routes.push({ path: '/notifications', title: 'SIDE-BAR.NOTIFICATIONS', icon: 'notifications', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewStatusPage)) this.adminItems.routes.push({ path: '/annotation-statuses', title: 'SIDE-BAR.ANNOTATION-STATUSES', icon: 'notifications', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewMaintenancePage)) this.adminItems.routes.push({ path: '/maintenance-tasks', title: 'SIDE-BAR.MAINTENANCE', icon: 'build', routeType: RouteType.System });
        if (this.authentication.hasPermission(AppPermission.ViewPlanStatusPage)) this.adminItems.routes.push({ path: '/plan-statuses', title: 'SIDE-BAR.PLAN-STATUSES', icon: 'library_books', routeType: RouteType.System });
		if (this.authentication.hasPermission(AppPermission.ViewDescriptionStatusPage)) this.adminItems.routes.push({ path: '/description-statuses', title: 'SIDE-BAR.DESCRIPTION-STATUSES', icon: 'description', routeType: RouteType.System });
		this.groupMenuItems.push(this.adminItems);

		let infoItems: RouteInfo[] = this.configurationService?.sidebar?.infoItems.map(infoItem => {
			if (infoItem.accessLevel == AccessLevel.Authenticated && !this.isAuthenticated()) return null;
			if (infoItem.accessLevel == AccessLevel.Unauthenticated && this.isAuthenticated()) return null;

			if (infoItem.isExternalLink) {
				return { title: infoItem.title, icon: infoItem.icon, externalUrl: infoItem.externalUrl, routeType: RouteType.Configurable } as RouteInfo;
			} else {
				return { title: infoItem.title, icon: infoItem.icon, path: infoItem.routerPath, routeType: RouteType.Configurable } as RouteInfo;
			}
		})?.filter(x => x!=null);
		this.infoItems = {
			title: "",
			routes: infoItems ?? []
		}

		this.groupMenuItems.push(this.infoItems);
	}

	public principalHasAvatar(): boolean {
		return this.authentication.getUserProfileAvatarUrl() != null && this.authentication.getUserProfileAvatarUrl().length > 0;
	}

	public getDefaultAvatar(): string {
		return 'assets/images/profile-placeholder.png';
	}

	public applyFallbackAvatar(ev: Event) {
		(ev.target as HTMLImageElement).src = this.getDefaultAvatar();
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	isLoginRouteActivated(): boolean {
		return this.location.path().indexOf('/login') > -1;
	}

	public logout(): void {
		this.router.navigate(['/logout']);
	}

	showItem(value: GroupMenuItem) {
		return value.routes && value.routes.length > 0;
	}

	openProfile() {
		const dialogRef = this.dialog.open(UserDialogComponent, {
			hasBackdrop: true,
			autoFocus: false,
			closeOnNavigation: true,
			disableClose: false,
			position: { top: '64px', right: '1em' },
			panelClass: 'custom-userbox'
		});
	}

	public onInvalidUrl(): boolean {
		return this.currentRoute === '/language-editor' || this.currentRoute === '/profile';
	}

	public openLanguageDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(LanguageDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true
				}
			});
		}
	}
}
