import { Location, NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, Validators, FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SideNavService } from '@app/core/services/sidenav/side-nav.sevice';
import { UIStatisticsService } from '@app/core/services/statistics/ui-statistics.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth/auth.service';
import { StartNewDescriptionDialogComponent } from '../description/start-new-description-dialog/start-new-description-dialog.component';
import { LanguageDialogComponent } from '../language/dialog/language-dialog.component';
import { UserDialogComponent } from '../navbar/user-dialog/user-dialog.component';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle } from '@angular/material/expansion';
import { MatList, MatListItem } from '@angular/material/list';
import { filter } from 'rxjs';

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
	count?: () => number;
	// other pages that belong to this item, so it stays highlighted on them
	activePaths?: string[];
}
class GroupMenuItem {
	title?: string;
	routes: RouteInfo[];
}

@Component({
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [
        MatIconButton,
        MatIcon,
        MatButtonToggleGroup,
        FormsModule,
        MatButtonToggle,
        MatExpansionPanel,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        MatList,
        NgTemplateOutlet,
        MatListItem,
        RouterLink,
        MatButton,
        TranslatePipe,
    ],
})
export class SidebarComponent implements OnInit {
	generalItems: GroupMenuItem;
	planItems: GroupMenuItem;
	libraryItems: GroupMenuItem;
	descriptionItems: GroupMenuItem;
	grantItems: GroupMenuItem;
	infoItems: GroupMenuItem;
	adminGroups: GroupMenuItem[] = [];
	groupMenuItems: GroupMenuItem[] = [];
	currentRoute: string;
	routeType = RouteType;
	adminView: boolean = false;

	constructor(
		public translate: TranslateService,
		private authentication: AuthService,
		private dialog: MatDialog,
		public router: Router,
		private location: Location,
		private analyticsService: AnalyticsService,
		public routerUtils: RouterUtilsService,
		private configurationService: ConfigurationService,
		protected sidenavService: SideNavService,
		private uiStatisticsService: UIStatisticsService,
	) {
		this.router.events.subscribe(() => {
			if (window.innerWidth <= 500) {
				this.sidenavService.setStatus(false);
			}
		});
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Sidebar);

		this.currentRoute = this.router.url;

		this.authentication
			.getAuthenticationStateObservable()
			.pipe()
			.subscribe((authenticationState) => {
				this.reCalculateMenu();
				this.syncAdminViewWithRoute();
			});

		this.reCalculateMenu();
		this.syncAdminViewWithRoute();

		// router.url still holds the previous page until NavigationEnd, so syncing earlier flips the toggle back mid-navigation
		this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
			this.currentRoute = this.router.url;
			this.syncAdminViewWithRoute();
		});
	}

	private syncAdminViewWithRoute() {
		this.adminView = this.adminGroups?.some((g) => g.routes?.some((r) => this.isRouteActive(r))) ?? false;
	}

	isRouteActive(route: RouteInfo): boolean {
		if (!route.path) return false;
		const currentPath = this.currentRoute?.split('?')[0];
		return [route.path, ...(route.activePaths ?? [])].some((path) => {
			const routePath = this.routerUtils.generateUrl(path);
			return currentPath === routePath || currentPath?.startsWith(routePath + '/');
		});
	}

	toggleNav() {
		this.sidenavService.toggle();
	}
	private reCalculateMenu() {
		this.groupMenuItems = [];
		this.generalItems = {
			routes: [],
		};
		this.generalItems.routes.push({ path: '/home', title: 'SIDE-BAR.DASHBOARD', icon: 'home', routeType: RouteType.System });

		this.groupMenuItems.push(this.generalItems);

		this.planItems = {
			title: 'SIDE-BAR.WORKSPACE',
			routes: [],
		};

		if (this.authentication.hasPermission(AppPermission.ViewMyPlanPage))
			this.planItems.routes.push({
				path: '/plans',
				title: 'SIDE-BAR.MY-PLANS',
				icon: 'docs',
				routeType: RouteType.System,
				count: () => this.uiStatisticsService.dashboardStatistics()?.planCount ?? 0,
			});
		if (this.authentication.hasPermission(AppPermission.ViewMyDescriptionPage))
			this.planItems.routes.push({
				path: '/assets',
				title: 'SIDE-BAR.MY-DESCRIPTIONS',
				icon: 'database',
				routeType: RouteType.System,
				count: () => this.uiStatisticsService.dashboardStatistics()?.descriptionCount ?? 0,
			});

		this.groupMenuItems.push(this.planItems);

		this.descriptionItems = {
			title: 'SIDE-BAR.EXPLORE',
			routes: [],
		};

		if (this.authentication.hasPermission(AppPermission.ViewPublicPlanPage) || !this.isAuthenticated()) {
			this.descriptionItems.routes.push({
				path: '/explore-plans',
				title: 'SIDE-BAR.PUBLIC-PLANS',
				icon: 'language',
				routeType: RouteType.System,
				count: () => this.uiStatisticsService.dashboardStatistics()?.publicPlanCount ?? 0,
			});
		}
		if (this.authentication.hasPermission(AppPermission.ViewPublicDescriptionPage) || !this.isAuthenticated()) {
			this.descriptionItems.routes.push({
				path: '/explore-assets',
				title: 'SIDE-BAR.PUBLIC-DESC',
				icon: 'groups',
				routeType: RouteType.System,
				count: () => this.uiStatisticsService.dashboardStatistics()?.publicDescriptionCount ?? 0,
			});
		}
		this.groupMenuItems.push(this.descriptionItems);

		this.libraryItems = {
			title: 'SIDE-BAR.LIBRARY',
			routes: [
				this.authentication.hasPermission(AppPermission.ViewPlanBlueprintLibraryPage)
					? {
							path: 'plan-blueprint-library',
							title: 'SIDE-BAR.PLAN-BLUEPRINTS',
							icon: 'assignment',
							routeType: RouteType.System,
							count: () => this.uiStatisticsService.dashboardStatistics()?.planBlueprintsCount ?? 0,
						}
					: null,
				this.authentication.hasPermission(AppPermission.ViewDescriptionTemplateLibraryPage)
					? {
							path: 'asset-template-library',
							title: 'SIDE-BAR.DESCRIPTION-TEMPLATES',
							icon: 'browse',
							routeType: RouteType.System,
							count: () => this.uiStatisticsService.dashboardStatistics()?.descriptionTemplatesCount ?? 0,
						}
					: null,
			].filter((x) => !!x),
		};
		this.groupMenuItems.push(this.libraryItems);

		const settingsPaths = ['/tenant-configuration', '/languages', '/webhooks', '/notification-templates', '/supportive-materials', '/annotation-statuses'];
		const canViewWorkflows = this.authentication.hasPermission(AppPermission.ViewPlanWorkflowPage) || this.authentication.hasPermission(AppPermission.ViewDescriptionWorkflowPage);
		this.adminGroups = [
			{
				// Reading the tenant's data, as opposed to configuring it
				title: 'SIDE-BAR.INSIGHTS',
				routes: [
					this.authentication.hasPermission(AppPermission.ViewIndicatorDashboardPage) && this.configurationService.kpi.enabled == true ?
						{ path: '/usage', title: 'SIDE-BAR.INDICATOR-DASHBOARD', icon: 'monitoring', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewReportsPage) ?
						{ path: '/reports', title: 'SIDE-BAR.REPORTS', icon: 'analytics', routeType: RouteType.System } : null
				].filter(x => !!x)
			},
			{
				title: 'SIDE-BAR.TEMPLATES',
				routes: [
					this.authentication.hasPermission(AppPermission.ViewPlanBlueprintPage) ?
						{ path: '/plan-blueprints', title: 'SIDE-BAR.PLAN-BLUEPRINTS', icon: 'description', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewDescriptionTemplatePage) ?
						{ path: '/asset-templates', title: 'SIDE-BAR.DESCRIPTION-TEMPLATES', icon: 'browse', routeType: RouteType.System } : null,
					this.authentication.hasAnyPermission([AppPermission.ViewPlanBlueprintTypePage, AppPermission.ViewDescriptionTemplateTypePage]) ?
						{ path: '/types', title: 'SIDE-BAR.TYPES', icon: 'label', routeType: RouteType.System } : null
				].filter(x => !!x)
			},
			{
				title: 'SIDE-BAR.DATA-SOURCES',
				routes: [
					this.authentication.hasPermission(AppPermission.ViewReferenceTypePage) ?
						{ path: '/reference-type', title: 'SIDE-BAR.REFERENCE-TYPES', icon: 'category', routeType: RouteType.System, activePaths: ['/references'] } : null,
					this.authentication.hasPermission(AppPermission.ViewPrefillingSourcePage) ?
						{ path: '/prefilling-sources', title: 'SIDE-BAR.PREFILLING-SOURCES', icon: 'upload', routeType: RouteType.System } : null
				].filter(x => !!x)
			},
			{
				title: 'SIDE-BAR.ORGANISATION',
				routes: [
					this.authentication.hasPermission(AppPermission.ViewTenantUserPage) || this.authentication.hasPermission(AppPermission.ViewUserPage) ?
						{ path: this.authentication.hasPermission(AppPermission.ViewTenantUserPage) ? '/tenant-users' : '/users', title: 'SIDE-BAR.USERS', icon: 'group', routeType: RouteType.System, activePaths: ['/tenant-users', '/users'] } : null,
					this.authentication.hasPermission(AppPermission.ViewTenantConfigurationPage) || canViewWorkflows ?
						{ path: this.authentication.hasPermission(AppPermission.ViewTenantConfigurationPage) ? '/tenant-configuration' : '/tenant-configuration/workflows', title: 'SIDE-BAR.SETTINGS', icon: 'settings', routeType: RouteType.System, activePaths: settingsPaths } : null
				].filter(x => !!x)
			},
			{
				// Pages that act on the whole installation rather than the selected tenant
				title: 'SIDE-BAR.INSTALLATION',
				routes: [
					this.authentication.hasPermission(AppPermission.ViewTenantPage) ?
						{ path: '/tenants', title: 'SIDE-BAR.TENANTS', icon: 'apartment', routeType: RouteType.System } : null,
					(this.authentication.hasPermission(AppPermission.ViewUsageLimitPage) && this.configurationService.accountingServiceEnabled == true) ?
						{ path: '/usage-limits', title: 'SIDE-BAR.USAGE-LIMITS', icon: 'speed', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewTenantTransferPage) ?
						{ path: '/tenant-transfer', title: 'SIDE-BAR.TENANT-TRANSFER', icon: 'move_down', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewMaintenancePage) ?
						{ path: '/maintenance-tasks', title: 'SIDE-BAR.MAINTENANCE', icon: 'build', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewEntityLockPage) ?
						{ path: '/entity-locks', title: 'SIDE-BAR.ENTITY-LOCKS', icon: 'lock', routeType: RouteType.System } : null,
					this.authentication.hasPermission(AppPermission.ViewNotificationPage) ?
						{ path: '/notifications', title: 'SIDE-BAR.NOTIFICATION-LOG', icon: 'outgoing_mail', routeType: RouteType.System } : null
				].filter(x => !!x)
			}
		];
		this.adminGroups.forEach((g) => this.groupMenuItems.push(g));

		// let infoItems: RouteInfo[] = this.configurationService?.sidebar?.infoItems.map(infoItem => {
		// 	if (infoItem.accessLevel == AccessLevel.Authenticated && !this.isAuthenticated()) return null;
		// 	if (infoItem.accessLevel == AccessLevel.Unauthenticated && this.isAuthenticated()) return null;

		// 	if (infoItem.isExternalLink) {
		// 		return { title: infoItem.title, icon: infoItem.icon, externalUrl: infoItem.externalUrl, routeType: RouteType.Configurable } as RouteInfo;
		// 	} else {
		// 		return { title: infoItem.title, icon: infoItem.icon, path: infoItem.routerPath, routeType: RouteType.Configurable } as RouteInfo;
		// 	}
		// })?.filter(x => x!=null);
		// this.infoItems = {
		// 	title: "",
		// 	routes: infoItems ?? []
		// }

		// this.groupMenuItems.push(this.infoItems);
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

	showAdminToggle(): boolean {
		return this.adminGroups?.some((g) => g.routes?.length > 0) ?? false;
	}

	isAdminGroup(value: GroupMenuItem): boolean {
		return this.adminGroups?.includes(value) ?? false;
	}

	showItemForView(value: GroupMenuItem): boolean {
		if (!this.showItem(value)) return false;
		if (!this.showAdminToggle()) return true;
		if (value === this.generalItems) return true;
		const isAdmin = this.isAdminGroup(value);
		return this.adminView ? isAdmin : !isAdmin;
	}

	openProfile() {
		const dialogRef = this.dialog.open(UserDialogComponent, {
			hasBackdrop: true,
			closeOnNavigation: true,
			disableClose: false,
			position: { top: '64px', right: '1em' },
			panelClass: 'custom-userbox',
		});
	}

	public onInvalidUrl(): boolean {
		return this.currentRoute === '/language-editor' || this.currentRoute === '/profile';
	}

	public openLanguageDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		} else {
			const dialogRef = this.dialog.open(LanguageDialogComponent, {
				disableClose: true,
				data: {
					isDialog: true,
				},
			});
		}
	}

	openNewDescriptionDialog() {
		const formGroup = new FormGroup({
			planId: new FormControl(null, Validators.required),
		});

		const dialogRef = this.dialog.open(StartNewDescriptionDialogComponent, {
			disableClose: false,
			data: {
				startNewPlan: false,
				formGroup: formGroup,
			},
			maxWidth: '600px',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result?.formGroup) {
				// if (result.startNewPlan) {
				// 	this.openNewPlanDialog();
				// } else {
				const planId = result.formGroup.get('planId').value;
				this.router.navigateByUrl('/reload', { skipLocationChange: true }).then(() => {
					setTimeout(() => this.router.navigate([this.routerUtils.generateUrl(['/plans/', 'edit/', planId])]));
				});
				// }
			}
		});
	}
}
