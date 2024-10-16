import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { ActivatedRouteSnapshot, NavigationEnd, Router } from "@angular/router";
import { BaseComponent } from "@common/base/base.component";
import { combineLatest, of } from "rxjs";
import { debounceTime, distinctUntilChanged, filter, map, startWith, takeUntil, tap } from "rxjs/operators";
import { BreadCrumbRouteData, BreadcrumbService } from "./breadcrumb.service";
import { RouterUtilsService } from "@app/core/services/router/router-utils.service";


@Component({
	selector: 'app-navigation-breadcrumb',
	styleUrls: [
		'./navigation-breadcrumb.component.scss'
	],
	templateUrl: './navigation-breadcrumb.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavigationBreadcrumbComponent extends BaseComponent {

	breadCrumbs: BreadCrumbItem[] = [];

	paramToStringDictionary: Record<string, string> = {};
	excludedParamsDictionary: Record<string, boolean> = {};


	protected readonly HOME_SYMBOL = Symbol('home')


	constructor(
		public routerUtils: RouterUtilsService,
		private router: Router, 
		private breadcrumbService: BreadcrumbService, 
		private cdr: ChangeDetectorRef
	) {
		super();
		combineLatest([
			this.breadcrumbService.resolvedValues().pipe(
				tap(x => this.paramToStringDictionary = x),
			),
			this.breadcrumbService.excludedValues().pipe(
				tap(x => this.excludedParamsDictionary = x),
			),
			router.events.pipe(
				filter(event => event instanceof NavigationEnd),
				map((x: NavigationEnd) => x.url?.split('?')?.[0]),
				distinctUntilChanged(),
				debounceTime(200),
				startWith(of())
			)
		])
			.pipe(
				map(() => {
					const routeSnapshot = this.router.routerState.snapshot.root;
					const breadcrumbs = this._buildBreadCrumbs(routeSnapshot).filter(x => !!x.title);
					return breadcrumbs;
				}),
				takeUntil(this._destroyed),
			)
			.subscribe(breadCrumbs => {
				this.breadCrumbs = breadCrumbs;
				this.cdr.markForCheck();
			});
	}


	public computePath(index: number): string {
		if (!this.breadCrumbs?.length) {
			return null;
		}
		if (index < 0 || index >= this.breadCrumbs?.length) {
			return null;
		}

		if (this.breadCrumbs[index].skipNavigation) {
			return null;
		}

		const path = this.breadCrumbs.slice(0, index + 1)
			.map(x => x.path)
			.reduce((aggr, current) => [...aggr, ...current.split('/')], ['/'])
			.filter(x => !!x);


		const computedPath = this.breadcrumbService.getPathOverrideFor(
			path?.join('/').replace('//', '/')
		)?.split('/') ?? path;

		return this.routerUtils.generateUrl(computedPath.filter(x => x !== '/'), '/');
	}

	private _buildBreadCrumbs(activatedRoute: ActivatedRouteSnapshot): BreadCrumbItem[] {

		if (!activatedRoute) {
			return [];
		}

		let path = activatedRoute.routeConfig?.path ?? '/'; // undefined path is the root path


		const breadcrumbData: BreadCrumbRouteData | undefined = activatedRoute.routeConfig?.data?.[BreadcrumbService.ROUTE_DATA_KEY];

		const pathItems = path == '/' ? ['/'] : path.split('/');
		const currentItems: BreadCrumbItem[] = [];

		for (let pathItem of pathItems) {

			const [title, translateParams] = this._composeBreadCrumbTitle({
				path: pathItem,
				breadcrumbData,
				pathParams: activatedRoute.params
			});		
			
			let pathName = this._enrichPathParamNames(pathItem, activatedRoute.params); // somepath/:id => somepath/1239dfg123-123-123...etc
			
			const skipNavigation = activatedRoute.routeConfig?.data?.breadcrumb?.skipNavigation ?? false;
			const hideItem = (breadcrumbData?.hideNavigationItem || this.excludedParamsDictionary[pathName]) ?? false;

			const currentItem: BreadCrumbItem = {
				title,
				path: pathName,
				translateParams,
				skipNavigation,
				hideItem
			}
			currentItems.push(currentItem);
		}
		return [...currentItems, ...this._buildBreadCrumbs(activatedRoute.firstChild)];
	}


	private _composeBreadCrumbTitle(
		params: {
			breadcrumbData?: BreadCrumbRouteData,
			path: string,
			pathParams: Record<string, string>
		})
		: [string, Record<string, string>] | [string] {

		const { path, pathParams, breadcrumbData } = params;

		if (breadcrumbData?.title) {// higher priority if title exists
			return [breadcrumbData.title, null];
		}

		if (breadcrumbData?.titleFactory) {
			const { languageKey, translateParams } = breadcrumbData.titleFactory({
				pathResolutions: pathParams,
				valueResolutions: this.paramToStringDictionary
			});
			return [languageKey, translateParams];
		}


		if (path === '/') {
			return [this.HOME_SYMBOL as unknown as string, null];
		}

		if (!pathParams) {
			return [path, null];
		}




		// replace path params segments
		const title = Object.keys(pathParams)
			.sort((a, b) => b.length - a.length) // avoid param overlapping => :id2 (length 3) should be replaced before :id (length 2)
			.reduce(
				(aggr, current) => aggr.replace(`:${current}`, this.paramToStringDictionary[pathParams[current]] ?? pathParams[current])
				, path ?? ''
			);

		return [title, null];
	}


	private _enrichPathParamNames(path: string, pathParams: Record<string, string>): string {
		if (!pathParams || !path) {
			return path;
		}

		path = Object.keys(pathParams)
			.sort((a, b) => b.length - a.length) // avoid param overlapping => :id2 (length 3) should be replaced before :id (length 2)
			.reduce(
				(aggr, current) => aggr.replace(`:${current}`, pathParams[current])
				, path ?? ''
			);

		return path;
	}
}




interface BreadCrumbItem {
	title: string;
	path: string;
	skipNavigation: boolean;
	hideItem: boolean;
	translateParams?: Record<string, string>;
}