import { NgTemplateOutlet } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnDestroy, Output, SimpleChanges, TemplateRef, ViewChild, inject, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxChange, MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import moment from 'moment';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { ColumnDefinition, ColumnSortEvent, ColumnsChangedEvent, PageLoadEvent, RowActivateEvent, SortDescriptor, SortDirection, TableColumnProp } from '@common/modules/admin-listing/listing.types';

/** Which rows an admin listing shows on a non-default tenant: the tenant's own, the default tenant's shared rows, or both. */
export type ListingScope = 'own' | 'shared' | 'all';
export interface ListingScopeCounts { own?: number; shared?: number; all?: number; }
export type ListingDensity = 'comfortable' | 'compact';

interface DisplayColumn {
	definition: ColumnDefinition;
	prop?: TableColumnProp;
	name: string;
	sortable: boolean;
	cellTemplate?: TemplateRef<any>;
	primary: boolean;
	actions: boolean;
	/** `createdAt`, `updatedAt`, …: shown as "3 days ago" with the exact date in a tooltip. */
	date: boolean;
}

interface RowCache {
	editable?: boolean;
	link?: any[] | string | null;
	dates: Map<DisplayColumn, { value: any; relative: string }>;
}

const DENSITY_STORAGE_KEY = 'admin-listing-density';
/** Width of the actions column: the open icon and the "more" menu. Fixed so header and rows share one grid. */
const ACTIONS_COLUMN = '104px';

/**
 * The admin listing: one table for every admin page, one table, one behaviour.
 *
 * Data contract: `columns`, `visibleColumns`, `rows`, paging, `defaultSort`, the `columnSort` / `columnsChanged` /
 * `pageLoad` / `rowActivated` events, and the `[listing-filters]` / `[user-preference-settings]` content slots. On top of that it owns what
 * every admin page used to hide in its filter popup: the search field, the tenant scope (a tab strip with counts),
 * the "show deleted" toggle, and the empty states that explain them. Sorting is by clicking a column header.
 */
@Component({
	selector: 'app-admin-listing',
	standalone: true,
	imports: [RouterModule, TranslateModule, MatButtonModule, MatIconModule, MatMenuModule, MatCheckboxModule, MatPaginatorModule, MatTooltipModule, NgTemplateOutlet],
	templateUrl: './admin-listing.component.html',
	changeDetection: ChangeDetectionStrategy.Eager,
	styleUrl: './admin-listing.component.scss',
})
export class AdminListingComponent implements OnChanges, OnDestroy {
	private readonly language = inject(TranslateService);
	private readonly destroyed$ = new Subject<void>();
	private readonly search$ = new Subject<string>();
	@ViewChild('searchInput') private searchInput?: ElementRef<HTMLInputElement>;

	// ---- data -------------------------------------------------------------------------------------
	@Input() rows: any[] = [];
	@Input() columns: ColumnDefinition[] = [];
	@Input() visibleColumns: TableColumnProp[] = [];
	@Input() count = 0;
	@Input() offset = 0;
	@Input() limit = 10;
	@Input() pageSizeOptions = [10, 25, 50, 100];
	@Input() loadingIndicator = false;
	@Input() trackBy: (item: any) => any = x => x;
	/** Current ordering as the lookup stores it: `['-updatedAt']`. Only the first entry drives the header indicator. */
	@Input() set defaultSort(value: string[]) {
		const first = value?.[0];
		if (!first) { this.currentSort = null; return; }
		const desc = first.startsWith('-');
		this.currentSort = { prop: first.replace(/^[+-]/, ''), direction: desc ? SortDirection.Descending : SortDirection.Ascending };
	}

	// ---- search -------------------------------------------------------------------------------------
	@Input() searchTerm: string | null = null;
	/** Pages whose lookup has no free-text search hide the box. */
	@Input() showSearch = true;
	@Input() searchPlaceholderKey = 'ADMIN-LISTING.SEARCH';

	// ---- rows ----------------------------------------------------------------------------------------
	/** Decides pencil (editable) vs eye (read-only) for the row's open icon. */
	@Input() rowEditable: (row: any) => boolean = () => false;
	/** Show the open/edit icon at the start of the actions cell. */
	@Input() rowOpenIcon = true;
	/** Router link of a row's editor: the title becomes a real link (middle-click, copy link). Plain clicks still go through `rowActivated`. */
	@Input() rowLink: ((row: any) => any[] | string | null) | null = null;

	// ---- create action in the empty states ------------------------------------------------------------
	/** Router link of the page's create action; shown as a button in the empty states when set and allowed. */
	@Input() createRoute: any[] | string | null = null;
	@Input() canCreate = true;
	@Input() createLabelKey = 'ADMIN-LISTING.EMPTY.CREATE';

	// ---- empty states ------------------------------------------------------------------------------
	/** A search term or non-default filter is applied: the empty state offers "Clear filters". */
	@Input() hasActiveFilters = false;
	@Input() emptyIcon = 'inbox';
	@Input() emptyTitleKey = 'HYBRID-LISTING.EMPTY.TITLE';
	@Input() emptyMessageKey = 'HYBRID-LISTING.EMPTY.MESSAGE';
	/** The "own" scope is empty but the shared scope has rows: wording assumes the tenant can create its own (wrong for system-created-only entities like Locks). Override when `createRoute`/`canCreate` never apply. */
	@Input() emptyOwnMessageKey = 'ADMIN-LISTING.EMPTY-OWN.MESSAGE';

	// ---- tenant scope and deleted rows ---------------------------------------------------------------
	/** `null` on pages (or tenants) without a tenant scope: no tab strip. */
	@Input() scope: ListingScope | null = null;
	@Input() scopeCounts: ListingScopeCounts = {};
	/** Name of the selected tenant, for the "Owned by …" tab. */
	@Input() tenantName: string | null = null;
	/** `null` when the page has no active/deleted state: the switch is not shown. */
	@Input() showDeleted: boolean | null = false;

	@Output() rowActivated = new EventEmitter<RowActivateEvent>();
	@Output() pageLoad = new EventEmitter<PageLoadEvent>();
	@Output() columnSort = new EventEmitter<ColumnSortEvent>();
	@Output() columnsChanged = new EventEmitter<ColumnsChangedEvent>();
	@Output() clearFilters = new EventEmitter<void>();
	@Output() scopeChange = new EventEmitter<ListingScope>();
	@Output() showDeletedChange = new EventEmitter<boolean>();
	@Output() searchChange = new EventEmitter<string | null>();

	protected displayColumns: DisplayColumn[] = [];
	protected availableColumns: { property: TableColumnProp; name: string }[] = [];
	protected columnSelections = new Set<TableColumnProp>();
	protected gridTemplate = '';
	protected currentSort: { prop: string; direction: SortDirection } | null = null;
	protected density: ListingDensity = readDensity();
	protected readonly skeletonRows = Array.from({ length: 6 });
	protected readonly SortDirection = SortDirection;

	// Per row: the template would otherwise re-run permission checks, tenant URL building and moment parsing on every
	// change detection (each keystroke in the search box). Keyed by row object, so every reload starts fresh.
	private rowCache = new WeakMap<object, RowCache>();

	constructor() {
		this.search$.pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroyed$)).subscribe(term => this.searchChange.emit(term || null));
		this.language.onLangChange.pipe(takeUntil(this.destroyed$)).subscribe(() => this.rowCache = new WeakMap());
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['columns'] || changes['visibleColumns']) { this.refreshColumns(); }
		if (changes['columns'] || changes['visibleColumns'] || changes['rowEditable'] || changes['rowLink']) { this.rowCache = new WeakMap(); }
	}

	ngOnDestroy(): void { this.destroyed$.next(); this.destroyed$.complete(); }

	/** `/` focuses the search field from anywhere on the page that is not already a text field. */
	@HostListener('document:keydown', ['$event'])
	protected onDocumentKeydown(event: KeyboardEvent): void {
		if (event.key !== '/' || event.ctrlKey || event.metaKey || event.altKey) { return; }
		const target = event.target as HTMLElement | null;
		if (target && (target.closest('input, textarea, select, [contenteditable="true"]'))) { return; }
		if (!this.searchInput?.nativeElement || this.searchInput?.nativeElement.offsetParent === null) { return; }
		event.preventDefault();
		this.searchInput?.nativeElement.focus();
	}

	// ---- derived state ----------------------------------------------------------------------------
	protected get showSkeleton(): boolean { return this.loadingIndicator && !(this.rows?.length); }
	protected get showEmpty(): boolean { return !this.loadingIndicator && !(this.rows?.length); }
	protected get rowsAreClickable(): boolean { return this.rowActivated.observed; }
	protected get showCreate(): boolean { return !!this.createRoute && this.canCreate; }
	/** Rows of the default tenant that the "own" scope hides; drives the hint strip and the empty state. */
	protected get sharedCount(): number {
		const { own, shared, all } = this.scopeCounts ?? {};
		if (shared != null) { return shared; }
		return own != null && all != null ? Math.max(all - own, 0) : 0;
	}
	protected get showSharedHint(): boolean { return this.scope === 'own' && this.sharedCount > 0 && !this.showEmpty && !this.showDeleted; }
	/** Other scopes that do have matches for the current search and filters (their counts share the lookup). */
	protected get matchesElsewhere(): { scope: ListingScope; count: number }[] {
		if (!this.scope) { return []; }
		return (['own', 'shared', 'all'] as ListingScope[])
			.filter(s => s !== this.scope && (this.scopeCounts?.[s] ?? 0) > 0)
			.map(s => ({ scope: s, count: this.scopeCounts[s] }));
	}
	protected scopeLabelKey(scope: ListingScope): string { return `ADMIN-LISTING.SCOPE.${scope.toUpperCase()}`; }

	// ---- columns ---------------------------------------------------------------------------------
	private refreshColumns(): void {
		const columns = this.columns ?? [];
		const visible = this.visibleColumns?.length ? this.visibleColumns : columns.map(c => c.prop).filter(p => p != null);
		this.columnSelections = new Set(visible);
		const shown = columns.filter(c => c.alwaysShown || (c.prop != null && this.columnSelections.has(c.prop)));
		const firstProp = shown.find(c => c.prop != null);
		this.displayColumns = shown.map(c => ({
			definition: c,
			prop: c.prop,
			name: c.languageName ? this.language.instant(c.languageName) : (c.name ?? ''),
			sortable: !!c.sortable && c.prop != null,
			cellTemplate: c.cellTemplate,
			primary: c === firstProp,
			actions: c.prop == null && !!c.cellTemplate,
			date: !c.cellTemplate && !c.valueFunction && /At$/.test(String(c.prop ?? '')),
		}));
		// A page without an actions column still gets the open icon: give it a cell of its own.
		if (this.rowOpenIcon && !this.displayColumns.some(c => c.actions)) {
			this.displayColumns.push({ definition: { alwaysShown: true }, name: '', sortable: false, primary: false, actions: true, date: false });
		}
		this.gridTemplate = this.displayColumns.map(c => {
			if (c.actions) { return ACTIONS_COLUMN; }
			if (c.definition.width) { return `${c.definition.width}px`; }
			if (c.primary) { return 'minmax(220px, 2fr)'; }
			return c.definition.maxWidth ? `minmax(0, ${c.definition.maxWidth}px)` : 'minmax(0, 1fr)';
		}).join(' ');
		this.availableColumns = columns.filter(c => !c.alwaysShown && c.prop != null).map(c => ({
			property: c.prop,
			name: c.languageName ? this.language.instant(c.languageName) : (c.name ?? ''),
		}));
	}

	protected cellValue(row: any, column: DisplayColumn): any {
		const definition = column.definition;
		if (definition.valueFunction) { return definition.valueFunction(row); }
		const raw = column.prop == null ? undefined : deepValue(row, String(column.prop));
		return definition.pipe ? definition.pipe.transform(raw) : raw;
	}

	protected rawValue(row: any, column: DisplayColumn): any {
		return column.prop == null ? undefined : deepValue(row, String(column.prop));
	}

	/** "3 days ago" for date columns; the exact, page-formatted date goes in the tooltip. */
	protected relativeDate(row: any, column: DisplayColumn): string {
		const raw = this.rawValue(row, column);
		if (!raw) { return ''; }
		const m = moment(raw);
		return m.isValid() ? m.fromNow() : String(this.cellValue(row, column) ?? '');
	}

	protected isEditable(row: any): boolean {
		const cache = this.cacheFor(row);
		if (cache.editable === undefined) { cache.editable = this.rowEditable(row); }
		return cache.editable;
	}

	protected linkFor(row: any): any[] | string | null {
		const cache = this.cacheFor(row);
		if (cache.link === undefined) { cache.link = this.rowLink ? this.rowLink(row) : null; }
		return cache.link;
	}

	protected dateCell(row: any, column: DisplayColumn): { value: any; relative: string } {
		const cache = this.cacheFor(row);
		let cell = cache.dates.get(column);
		if (!cell) {
			cell = { value: this.cellValue(row, column), relative: this.relativeDate(row, column) };
			cache.dates.set(column, cell);
		}
		return cell;
	}

	private cacheFor(row: any): RowCache {
		let cache = this.rowCache.get(row);
		if (!cache) {
			cache = { dates: new Map() };
			this.rowCache.set(row, cache);
		}
		return cache;
	}

	protected onColumnSelectionChange(event: MatCheckboxChange, property: TableColumnProp): void {
		if (event.checked) { this.columnSelections.add(property); } else { this.columnSelections.delete(property); }
		this.visibleColumns = [...this.columnSelections];
		this.refreshColumns();
		this.columnsChanged.emit({ properties: this.visibleColumns });
	}

	// ---- sorting (column headers) ---------------------------------------------------------------------
	protected sortBy(property: TableColumnProp): void {
		const prop = String(property);
		let direction: SortDirection;
		if (this.currentSort?.prop === prop) {
			direction = this.currentSort.direction === SortDirection.Ascending ? SortDirection.Descending : SortDirection.Ascending;
		} else {
			// Dates read best newest first; names and codes A to Z.
			direction = /At$/.test(prop) ? SortDirection.Descending : SortDirection.Ascending;
		}
		const definition = this.columns.find(c => c.prop === property);
		const keys = (definition?.sortKeys ?? []).filter(k => !!k);
		const sortDescriptors: SortDescriptor[] = (keys.length ? keys : [prop]).map(property => ({ property, direction }));
		this.columnSort.emit({ sortDescriptors, column: definition, previousValue: this.currentSort?.direction, newValue: direction });
	}

	protected sortIndicator(column: DisplayColumn): 'ascending' | 'descending' | null {
		if (!column.sortable || this.currentSort?.prop !== String(column.prop)) { return null; }
		return this.currentSort.direction === SortDirection.Ascending ? 'ascending' : 'descending';
	}

	// ---- rows, search, paging ----------------------------------------------------------------------
	protected activate(row: any, event?: Event): void {
		if (!this.rowsAreClickable) { return; }
		this.rowActivated.emit({ type: 'click', event: event as MouseEvent, row } as RowActivateEvent);
	}

	/** The title link: a plain click navigates like the row (keeps the listing state in the URL); modified clicks are left to the browser. */
	protected onTitleClick(row: any, event: MouseEvent): void {
		event.stopPropagation();
		if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) { return; }
		event.preventDefault();
		this.activate(row, event);
	}

	protected onSearchInput(value: string): void { this.search$.next(value?.trim() ?? ''); }
	protected clearSearch(): void { this.searchTerm = null; this.search$.next(''); }
	protected onSearchKeydown(event: KeyboardEvent): void {
		if (event.key === 'Escape') { event.preventDefault(); this.clearSearch(); (event.target as HTMLInputElement).value = ''; }
	}

	protected onPage(event: PageEvent): void {
		this.pageLoad.emit({ count: event.length, pageSize: event.pageSize, limit: event.pageSize, offset: event.pageIndex });
	}

	protected setScope(scope: ListingScope): void {
		if (scope !== this.scope) { this.scopeChange.emit(scope); }
	}

	protected toggleDeleted(): void { this.showDeletedChange.emit(!this.showDeleted); }

	protected toggleDensity(): void {
		this.density = this.density === 'compact' ? 'comfortable' : 'compact';
		try { localStorage.setItem(DENSITY_STORAGE_KEY, this.density); } catch { /* private mode: keep it for the session only */ }
	}
}

function readDensity(): ListingDensity {
	try { return localStorage.getItem(DENSITY_STORAGE_KEY) === 'compact' ? 'compact' : 'comfortable'; } catch { return 'comfortable'; }
}

function deepValue(obj: any, path: string): any {
	if (obj == null) { return ''; }
	if (obj[path] !== undefined) { return obj[path]; }
	let current = obj;
	for (const part of path.split('.')) {
		current = current?.[part];
		if (current === undefined || current === null) { return ''; }
	}
	return current;
}
