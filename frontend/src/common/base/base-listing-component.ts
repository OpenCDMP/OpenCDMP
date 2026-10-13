import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, inject, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UserSettingsKey } from '@app/core/model/user-settings/user-settings.model';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseComponent } from '@common/base/base.component';
import { Lookup } from '@common/model/lookup';
import { QueryResult } from '@common/model/query-result';
import { ListingScope, ListingScopeCounts } from '@common/modules/admin-listing/admin-listing.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnSortEvent, ColumnsChangedEvent, PageLoadEvent, RowActivateEvent, SortDirection, TableColumnProp } from '@common/modules/admin-listing/listing.types';
import { UserSettingsPickerComponent } from '@common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { switchMap, takeUntil, tap } from 'rxjs/operators';

/** The lookup fields the admin listing state is stored in (see the admin lookups, e.g. PlanBlueprintLookup). */
interface AdminListingLookup {
	like?: string | null;
	isActive?: IsActive[];
	tenantSubQuery?: TenantLookup | null;
	tenantIsSet?: boolean;
}

@Component({
    selector: 'app-base-listing-component',
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export abstract class BaseListingComponent<ItemModel, LookupModel extends Lookup> extends BaseComponent implements OnInit {


	@ViewChild(UserSettingsPickerComponent) userSettingsPickerComponent?: UserSettingsPickerComponent;

	private readonly scopeAuthService = inject(AuthService);

	public static readonly _DEFAULT_ITEMS_PER_PAGE = 10;
	protected ITEMS_PER_PAGE = BaseListingComponent._DEFAULT_ITEMS_PER_PAGE;

	private _totalElements = 0;
	public get totalElements() { return this._totalElements };

	private _currentPageNumber = 0;
	public get currentPageNumber() { return this._currentPageNumber };


	public gridColumns = new Array<ColumnDefinition>();
	public visibleColumns = new Array<TableColumnProp>();
	public gridRows = new Array<ItemModel>();
	public lookup: LookupModel;
	public autoSelectUserSettings = false;
	public orderingDirection: string;
	public selectedOrderColumnProp: string;

	private lv = 0;

	private loadingPipe$ = new Subject<void>();
	private _isNoResults = false;
	public get isNoResults() {
		return this._isNoResults;
	}

	private _isLoading = false;
	/** True while a listing query is in flight — drives the listing skeleton and prevents an empty-state flash. */
	public get isListingLoading() {
		return this._isLoading;
	}
	private _latestLoadedResults$ = new BehaviorSubject<QueryResult<ItemModel>>(null);
	protected latestLoadedResults$ = this._latestLoadedResults$.asObservable();

	breadcrumb: string; //TODO maybe delete

	abstract userSettingsKey: UserSettingsKey;
	protected abstract loadListing(): Observable<QueryResult<ItemModel>>;
	protected abstract initializeLookup(): LookupModel;
	protected abstract setupColumns();

	// ---- admin listing: tenant scope, deleted rows, search, row actions ---------------------
	/**
	 * Set to true by listings whose lookup carries `tenantIsSet`: on a
	 * non-default tenant the admin listing then shows the own / shared / all scope tabs. Such listings also
	 * override `loadListingWith` so the counts of the other scopes can be fetched for the tab strip.
	 */
	protected readonly tenantScoped: boolean = false;
	public scopeCounts: ListingScopeCounts = {};

	private get adminLookup(): AdminListingLookup { return this.lookup as unknown as AdminListingLookup; }

	/** `null` when this page has no tenant scope or the default tenant is selected (nothing is shared there). */
	public get scope(): ListingScope | null {
		if (!this.tenantScoped || !this.scopeAuthService.getSelectedTenantId()) { return null; }
		return BaseListingComponent.scopeOf(this.adminLookup);
	}

	private static scopeOf(lookup: AdminListingLookup | null | undefined): ListingScope {
		if (lookup?.tenantIsSet === true) { return 'own'; }
		return lookup?.tenantIsSet === false ? 'shared' : 'all';
	}

	/** `null` when the lookup has no active/deleted state (nothing to toggle); the listing then hides the switch. */
	public get showDeleted(): boolean | null {
		const isActive = this.adminLookup?.isActive;
		if (!Array.isArray(isActive)) { return null; }
		return isActive[0] === IsActive.Inactive;
	}

	/** Display name of the selected tenant, for the "Owned by …" tab. */
	public get tenantName(): string | null { return this.scopeAuthService.getSelectedTenantName(); }

	/** Router link of a row's editor, relative to the listing route; pages that open rows under another route override it. */
	public readonly rowLink = (row: ItemModel): any[] | string | null => (row as any)?.id ? ['./', String((row as any).id)] : null;

	/** Pencil vs eye on the row: pages that gate editing define `canEdit(row)`; a page without one opens every row in its editor. */
	public readonly rowEditable = (row: ItemModel): boolean => typeof (this as any).canEdit === 'function' ? !!(this as any).canEdit(row) : true;


	public onScopeChange(scope: ListingScope): void {
		// The rows on screen belong to the previous tab: show the loading skeleton until the new ones arrive.
		this.gridRows = [];
		this._isLoading = true;
		this.applyScope(this.lookup, scope);
		this.filterChanged(this.lookup);
	}

	public onShowDeletedChange(show: boolean): void {
		this.adminLookup.isActive = show ? [IsActive.Inactive] : [IsActive.Active];
		this.filterChanged(this.lookup);
	}

	public onSearchChange(term: string | null): void {
		this.adminLookup.like = term || null;
		this.filterChanged(this.lookup);
	}

	/** Same query as `loadListing`, for an arbitrary lookup; tenant-scoped listings override it (one line). */
	protected loadListingWith(lookup: LookupModel): Observable<QueryResult<ItemModel>> | null {
		return null;
	}

	/**
	 * The backend already narrows every query to the selected tenant plus the shared (tenant-less) rows, so the
	 * scope is carried by `tenantIsSet` alone. No tenant id goes into the lookup: it is serialized into the URL and
	 * saved user settings, and a pinned id (`tenantSubQuery`) would match nothing after a tenant switch.
	 * On the default tenant only tenant-less rows exist, so `tenantIsSet: true` is never set there.
	 */
	protected applyScope(lookup: LookupModel, scope: ListingScope): void {
		const target = lookup as unknown as AdminListingLookup;
		const tenantId = this.scopeAuthService.getSelectedTenantId();
		target.tenantSubQuery = null;
		target.tenantIsSet = undefined;
		if (scope === 'own' && tenantId) {
			target.tenantIsSet = true;
		} else if (scope === 'shared') {
			target.tenantIsSet = false;
		}
	}

	/** Re-derives the tenant filter of a restored lookup (URL, user settings) for the tenant selected now. */
	private normalizeScope(lookup: LookupModel): void {
		if (!this.tenantScoped || !lookup) { return; }
		const restored = lookup as unknown as AdminListingLookup & { viewOnlyTenant?: boolean };
		// Links and user settings saved before `tenantIsSet` carried the scope marked "own" with these two.
		const legacyOwn = restored.viewOnlyTenant === true || !!restored.tenantSubQuery;
		delete restored.viewOnlyTenant;
		this.applyScope(lookup, legacyOwn ? 'own' : BaseListingComponent.scopeOf(restored));
	}

	/** The counts of the scopes not on screen, so every tab carries a number. Two extra 1-row queries per load. */
	private refreshScopeCounts(currentCount: number): void {
		const scope = this.scope;
		if (!scope) { this.scopeCounts = {}; return; }
		this.scopeCounts = { ...this.scopeCounts, [scope]: currentCount };
		for (const other of (['own', 'shared', 'all'] as ListingScope[]).filter(s => s !== scope)) {
			const probe = Object.assign(Object.create(Object.getPrototypeOf(this.lookup)), this.lookup) as LookupModel;
			probe.page = { offset: 0, size: 1 };
			probe.metadata = { countAll: true };
			probe.project = { fields: ['id'] };
			this.applyScope(probe, other);
			this.loadListingWith(probe)?.pipe(takeUntil(this._destroyed)).subscribe({
				next: result => { this.scopeCounts = { ...this.scopeCounts, [other]: result?.count ?? 0 }; },
				error: () => { this.scopeCounts = { ...this.scopeCounts, [other]: undefined }; },
			});
		}
	}

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
	) {
		super();
		this._registerListing();
	}

	private _registerListing() {
		this.loadingPipe$
			.pipe(
				takeUntil(this._destroyed),
				tap(_ => { this._isNoResults = false; this._isLoading = true; }),
				switchMap(_ => this.loadListing())
			)
			.subscribe(
				data => {
					this._currentPageNumber = this.lookup.page.offset / this.lookup.page.size;
					this.gridRows = data.items;
					this._totalElements = data.count;
					this._isNoResults = data.items.length === 0 ? true : false;
					this._isLoading = false;
					this._latestLoadedResults$.next(data);
					this.refreshScopeCounts(data.count);
				},
				error => { this._isLoading = false; this.onCallbackError(error); this._registerListing(); },
				() => this._registerListing()
			)
	}

	ngOnInit() {
		// Table setup
		this.setupColumns();
		this.setupVisibleColumns(this.lookup.project.fields);

		this.route.queryParamMap.pipe(takeUntil(this._destroyed)).subscribe((params: ParamMap) => {
			// If lookup is on the query params load it
			if (params.keys.length > 0 && params.has('lookup')) {
				// The serialized lookup omits null/undefined tenant-scope fields (see serializeLookup), so applying it
				// on top of `this.lookup` as constructed (defaults to the "own" scope) would leak that default's
				// `tenantSubQuery` into an "all"/"shared" lookup being restored. Reset to neutral first.
				if (this.tenantScoped) { this.applyScope(this.lookup, 'all'); }
				this.queryParamsService.deSerializeAndApplyLookup(params.get('lookup'), this.lookup);
				this.normalizeScope(this.lookup);
				this.onPageLoad({ offset: this.lookup.page.offset / this.lookup.page.size } as PageLoadEvent);
			} else {
				//else load user settings
				this.autoSelectUserSettings = true;
			}
		});
	}


	filterChanged(value: LookupModel, skipPageReset = false) {
		if (!skipPageReset) {
			value.page = { offset: 0, size: this.ITEMS_PER_PAGE }
		}
		this.userSettingsPickerComponent?.resetToDraft();
		this.router.navigate([], {
            queryParams: { 'lookup': this.queryParamsService.serializeLookup(value), 'lv': ++this.lv },
            replaceUrl: true
        });
	}

	changeSetting(lookup: LookupModel): void {
		queueMicrotask(() => {
			const tmpLookup = lookup || this.initializeLookup();
			this.normalizeScope(tmpLookup);
			this.lookup = tmpLookup;
			this.updateOrderUiFields(tmpLookup.order);
			this.setupVisibleColumns(tmpLookup.project.fields);
			this.router.navigate([], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(tmpLookup), 'lv': ++this.lv }, replaceUrl: true });
		})
	}

	private setupVisibleColumns(fields: string[]) {
		this.visibleColumns = this.gridColumns.filter(x => x.prop && fields.includes(x.prop.toString())).map(x => x.prop);
	}

	public updateOrderUiFields(order: Lookup.Ordering) {
		if (order && order.items && order.items.length > 0) {
			this.selectedOrderColumnProp = order.items[0].startsWith('-') || order.items[0].startsWith('+') ?
				order.items[0].substring(1) :
				order.items[0];
			this.orderingDirection = order.items[0].startsWith('-') ? '-' : '+';
		}
	}


	//
	// Listing Component functions
	//
	onPageLoad(event: PageLoadEvent) {
		if (event) {
			this.lookup.page.offset = event.offset * this.lookup.page.size;
		}
		this.loadingPipe$.next();
	}
	refresh() {
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}
	alterPage(event: PageLoadEvent) {
		if (event) {
			this.lookup.page.offset = event.offset * (event.pageSize ?? this.lookup.page.size);
			this.lookup.page.size = event.pageSize ?? this.lookup.page.size;

		}
		this.router.navigate([], { relativeTo: this.route, queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookup), 'lv': ++this.lv }, replaceUrl: true });
	}

	onNewItem(route: string = './new') {
		this.router.navigate([route], { relativeTo: this.route, queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookup), 'lv': ++this.lv } });
	}

	onRowActivated(event: RowActivateEvent, baseRoute: string = null) {
		if (event?.column?.checkboxable) {
			return;
		}
		if (event && event.type === 'click' && event.row && event.row.id) {
			let route = '';
			if (baseRoute) {
				route += `${baseRoute}/`;
			}
			route += event.row.id;
			this.router.navigate([route], { relativeTo: this.route, queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookup), 'lv': ++this.lv } });
		}
	}

	onColumnSort(event: ColumnSortEvent) {
		const sortItems = event.sortDescriptors.map(x => (x.direction === SortDirection.Ascending ? '' : '-') + x.property);
		this.lookup.order = { items: sortItems };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	onColumnsChanged(event: ColumnsChangedEvent) {
		this.userSettingsPickerComponent?.resetToDraft();
	}

	protected onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);
	}

	deepValueGetter(obj: any, path: string): any {
		if (obj == null) {
			return '';
		}
		if (!obj || !path) {
			return obj;
		}

		// check if path matches a root-level field
		// { "a.b.c": 123 }
		let current = obj[path];
		if (current !== undefined) {
			return current;
		}

		current = obj;
		const split = path.split('.');

		if (split.length) {
			for (let i = 0; i < split.length; i++) {
				current = current[split[i]];

				// if found undefined, return empty string
				if (current === undefined || current === null) {
					return '';
				}
			}
		}

		return current;
	}

	public toDescSortField(value: string): string {
		return '-' + value;
	}

	public toAscSortField(value: string): string {
		return '+' + value;
	}
}
