import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { UserSettingsKey } from '@app/core/model/user-settings/user-settings.model';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseComponent } from '@common/base/base.component';
import { Lookup } from '@common/model/lookup';
import { QueryResult } from '@common/model/query-result';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnSortEvent, ColumnsChangedEvent, PageLoadEvent, RowActivateEvent, SortDirection, TableColumnProp } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { UserSettingsPickerComponent } from '@common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { switchMap, takeUntil, tap } from 'rxjs/operators';

@Component({
    selector: 'app-base-listing-component',
    template: '',
    standalone: false
})
export abstract class BaseListingComponent<ItemModel, LookupModel extends Lookup> extends BaseComponent implements OnInit {


	@ViewChild(UserSettingsPickerComponent) userSettingsPickerComponent?: UserSettingsPickerComponent;


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
	private _latestLoadedResults$ = new BehaviorSubject<QueryResult<ItemModel>>(null);
	protected latestLoadedResults$ = this._latestLoadedResults$.asObservable();

	breadcrumb: string; //TODO maybe delete

	abstract userSettingsKey: UserSettingsKey;
	protected abstract loadListing(): Observable<QueryResult<ItemModel>>;
	protected abstract initializeLookup(): LookupModel;
	protected abstract setupColumns();

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
				tap(_ => this._isNoResults = false),
				switchMap(_ => this.loadListing())
			)
			.subscribe(
				data => {
					this._currentPageNumber = this.lookup.page.offset / this.lookup.page.size;
					this.gridRows = data.items;
					this._totalElements = data.count;
					this._isNoResults = data.items.length === 0 ? true : false;
					this._latestLoadedResults$.next(data);
				},
				error => { this.onCallbackError(error); this._registerListing(); },
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
				this.queryParamsService.deSerializeAndApplyLookup(params.get('lookup'), this.lookup);
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
