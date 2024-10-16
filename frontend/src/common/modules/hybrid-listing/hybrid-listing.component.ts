import { ChangeDetectorRef, Component, ContentChild, ElementRef, EventEmitter, Input, NgZone, OnChanges, OnInit, Output, PipeTransform, SimpleChanges, TemplateRef, ViewChild, ViewEncapsulation, inject } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { DatatableComponent, SelectionType, TableColumn } from '@swimlane/ngx-datatable';
import { debounceTime, filter, takeUntil } from 'rxjs/operators';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { BatchActionsDirective } from '@common/modules/hybrid-listing/directives/batch-actions.directive';
import { PageEvent } from '@angular/material/paginator';
import { nameof } from 'ts-simple-nameof';
import { Subject } from 'rxjs';
import { CollectionUtils } from '@app/core/services/utilities/collection-utils.service';

@Component({
	selector: 'app-hybrid-listing',
	templateUrl: './hybrid-listing.component.html',
	styleUrls: ['./hybrid-listing.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class HybridListingComponent extends BaseComponent implements OnInit, OnChanges {




	@ViewChild('wrapperCard', {static: true, read: ElementRef}) wrapperCard: ElementRef<any>;
	@ViewChild(DatatableComponent) table: DatatableComponent;
  
	private changeDetectorRef = inject(ChangeDetectorRef);
	private ngZone  = inject(NgZone);


	@ViewChild('functionValueTemplate', { static: true }) functionValueTemplate: TemplateRef<any>;

	private _batchActions?: BatchActionsDirective
	@ContentChild(BatchActionsDirective) set batchActions(batchActions: BatchActionsDirective){
		this._batchActions = batchActions;
		this.refreshColumnDefinitionData();
		if(this._batchActions){
			this._batchActions.getSelections = () =>{
				return this.selected;
			}
		}
	}
	get batchActions(): BatchActionsDirective{
		return this._batchActions;
	}


	private _listItemTemplate: TemplateRef<any>;
	@Input() public set listItemTemplate(value: TemplateRef<any>){
		if(!value){
			this.mode = HybridListingMode.Table;
		}
		this._listItemTemplate = value;
	}
	public get listItemTemplate(): TemplateRef<any>{
		return this._listItemTemplate;
	}


	private _cardItemTemplate: TemplateRef<any>;
	@Input() public set cardItemTemplate(value: TemplateRef<any>){
		if(!value){
			this.mode = HybridListingMode.Table;
		}
		this._cardItemTemplate = value;
	}
	public get cardItemTemplate(): TemplateRef<any>{
		return this._cardItemTemplate;
	}

	protected _rowIdentity = x => x

	@Input()
	trackBy: ((item: any) => any ) = this._rowIdentity;

	@Input() class: TableClass = TableClass.Material;
	@Input() forceShowShorting = false;
	@Input() columns: ColumnDefinition[];
	@Input() rows: any[];
	@Input() columnMode: ColumnMode = ColumnMode.Force;
	@Input() headerHeight = 50;
	@Input() footerHeight = 50;
	@Input() rowHeight: number | string = 'auto';
	@Input() messages: StaticTableMessages;
	@Input() externalPaging = true;
	@Input() count = 0;
	@Input() offset = 0;
	@Input() limit: number = undefined;
	@Input() propertySelectable: boolean = true;

	@Input() hideModeSelection: boolean = false;

	public _defaultSort: any;
	@Input() set defaultSort(input: string[]) {
		if (input && input.length > 0) {
			this._defaultSort = input.map(x => {
				let sortProp: String = x;
				let sortDir: String = SortDirection.Ascending;
				if (x.startsWith('+')) {
					sortProp = x.substring(1);
					sortDir = SortDirection.Ascending;
				} else if (x.startsWith('-')) {
					sortProp = x.substring(1);
					sortDir = SortDirection.Descending;
				}
				return { prop: sortProp, dir: sortDir };
			});
		} else {
			this._defaultSort = undefined;
		}
	}
	@Input() loadingIndicator = false;
	@Input() externalSorting = false;
	@Input() initialMode: HybridListingMode;

	@Output() rowActivated: EventEmitter<RowActivateEvent> = new EventEmitter<RowActivateEvent>();
	@Output() pageLoad: EventEmitter<PageLoadEvent> = new EventEmitter<PageLoadEvent>();
	@Output() columnSort: EventEmitter<ColumnSortEvent> = new EventEmitter<ColumnSortEvent>();
	@Output() columnsChanged: EventEmitter<ColumnsChangedEvent> = new EventEmitter<ColumnsChangedEvent>();
	@Output() modeChanged: EventEmitter<HybridListingMode> = new EventEmitter<HybridListingMode>();

	public internalColumns: TableColumn[];
	@Input() visibleColumns: TableColumnProp[];
	private columnSortKeys: Map<TableColumnProp, string[]>;

	protected sortableListColumns: {property: TableColumnProp, name: string}[];
	protected availableColumns: {property: TableColumnProp, name: string}[];
	protected columnSelections: Set<TableColumnProp>;

	



	SelectionType = SelectionType;
	protected selected=[]

	

	get rowIdentity(){
		return this.batchActions?.rowIdentity ?? this.trackBy ??  this._rowIdentity;
	}

	HybridListingMode = HybridListingMode;
	SortDirection = SortDirection;
	protected mode: HybridListingMode = HybridListingMode.Table;
	constructor(
		private collectionUtils: CollectionUtils,
		private language: TranslateService,
	) {
		super();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['columns']) {
			this.refreshColumnDefinitionData();
		}
		if (changes['visibleColumns'] && !changes['visibleColumns'].isFirstChange()) {
			this.refreshColumnDefinitionData();
		}

		const modeChanges = changes[nameof<HybridListingComponent>(x => x.initialMode)];
		if(modeChanges?.isFirstChange){
			if(![null, undefined].includes(this.initialMode)){
				this.mode = this.initialMode;
			}
		}

	}


	ngOnDestroy(): void {
		super.ngOnDestroy();
		this.resizeObserver?.unobserve(this.wrapperCard.nativeElement)
		this.resizeObserver?.disconnect();
		this.resizeObserver = null;
		this.resizeSubject$?.complete();
	}

	private resizeObserver: ResizeObserver;
	private resizeSubject$: Subject<void>;

	ngOnInit() {
		this.setTableMessages();

		this.language.onLangChange.pipe(takeUntil(this._destroyed)).subscribe(event => {
			this.setTableMessages();
			this.refreshColumnDefinitionData();
		});

		this.ngZone.runOutsideAngular(() => {
			this.resizeSubject$ = new Subject<void>;

			this.resizeSubject$.pipe(
				debounceTime(500),
				filter(() => !!this.table)
			).subscribe(() => {
				this.table.recalculate();
				this.changeDetectorRef.detectChanges();
			})


			this.resizeObserver = new ResizeObserver(
				(resize) => {
					this.resizeSubject$.next();
				},
			)
			this.resizeObserver.observe(this.wrapperCard.nativeElement)
		})


	}


	public setModeSilently(mode: HybridListingMode):void{
		this.mode = mode;
	}

	public getMode(): HybridListingMode{
		return this.mode;
	}

	public isColumnSelected = (columnProp: string) => !!this.columnSelections?.has(columnProp)
	
	protected onListItemActivated(row: any){
		this.rowActivated.emit({
			type: 'click',
			row: row,
		} as any);
	}

	protected emptySelections(): void{
		this.selected = [];
	}

	protected toggleMode(): void{
		if(!this.listItemTemplate && !this.cardItemTemplate){
			this.mode = HybridListingMode.Table;
			this.modeChanged.emit(this.mode);
			return;
		}


		switch(this.mode){
			case HybridListingMode.Table:{
				if(this.listItemTemplate){
					this.mode = HybridListingMode.List;
					this.modeChanged.emit(this.mode);
					return;
				}
			}
			case HybridListingMode.List:{
				if(this.cardItemTemplate){
					this.mode = HybridListingMode.Card;
					this.modeChanged.emit(this.mode);
					return;
				}
			}
			case HybridListingMode.Card:
			default: {
				this.mode = HybridListingMode.Table;
				this.modeChanged.emit(this.mode);
				return;
			}
		}


	}


	onsomePage(event: PageEvent){
		if(!event){
			return;
		}
		this.pageLoad.emit({
			count: event.length,
			pageSize: event.pageSize,
			limit: event.pageSize,
			offset: event.pageIndex,
		});
	}

	protected isListingItemSelected(item: any){
		return !!this.selected?.find(x => this.rowIdentity && (this.rowIdentity(item) === this.rowIdentity(x)));	
	}

	onSelectionListChange(event: MatCheckboxChange, item: any){
		this.selected = this.selected?.filter(x  => this.rowIdentity && (this.rowIdentity(item) !== this.rowIdentity(x)))
		if(event.checked){
			this.selected = [ ...(this.selected ?? []), item].filter(x => x);
		}
	}

	private setTableMessages() {
		this.messages = {
			emptyMessage: this.language.instant('COMMONS.LISTING-COMPONENT.MESSAGE.EMPTY'),
			totalMessage: this.language.instant('COMMONS.LISTING-COMPONENT.MESSAGE.TOTAL'),
			selectedMessage: this.language.instant('COMMONS.LISTING-COMPONENT.MESSAGE.SELECTED')
		};
	}

	private refreshColumnDefinitionData() {
		const visibleColumns = this.getVisibleColumns();
		this.internalColumns = this.columns.filter(x => x.alwaysShown || visibleColumns.includes(x.prop)).map(x => {
			const item = {
				checkboxable: x.checkboxable,
				frozenLeft: x.frozenLeft,
				frozenRight: x.frozenRight,
				flexGrow: x.flexGrow,
				minWidth: x.minWidth,
				maxWidth: x.maxWidth,
				width: x.width,
				resizeable: x.resizeable,
				comparator: x.comparator,
				pipe: x.pipe,
				sortable: x.sortable,
				draggable: x.draggable,
				canAutoResize: x.canAutoResize,
				name: x.languageName ? this.language.instant(x.languageName) : x.name,
				prop: x.prop,
				cellTemplate: x.cellTemplate,
				headerTemplate: x.headerTemplate,
				cellClass: x.cellClass,
				headerClass: x.headerClass,
				headerCheckboxable: x.headerCheckboxable,
				summaryFunc: x.summaryFunc,
				summaryTemplate: x.summaryTemplate
			} as TableColumn;

			if (x.valueFunction) {
				item.cellTemplate = this.functionValueTemplate;
				item['valueFunction'] = x.valueFunction;
			}
			return item;
		});
		this.internalColumns = [ this.batchActions?.rowIdentity && {
			checkboxable: true,
			sortable: false,
			width: 30,
			maxWidth: 30,
			canAutoRisize: false,
			headerCheckboxable: false
		} ,...this.internalColumns].filter(x => !!x)
		this.columnSortKeys = new Map<TableColumnProp, string[]>(this.columns.map(buildColumnSortKeys));

		this.availableColumns = this.columns?.filter(x => !x?.alwaysShown).map(x => ({
			name: x.languageName ? this.language.instant(x.languageName) : x.name,
			property: x.prop
		}));
		this.sortableListColumns = this.internalColumns.filter(x => x?.sortable).map(x => ({
			name: x.name,
			property: x.prop
		}))
		this.columnSelections = new Set(visibleColumns);
	}


	protected getPropertyName(string): string{
		return this.sortableListColumns?.find(x => x.property === string)?.name;
	}

	protected onColumnSelectionChange(event: MatCheckboxChange, property: TableColumnProp){

		if(event.checked){
			this.columnSelections.add(property)
		}else{
			this.columnSelections.delete(property)
		}
		this.visibleColumns = [...this.columnSelections].filter(x => x);
		this.refreshColumnDefinitionData();
		const visibleColumns = this.getVisibleColumns();
		this.columnSelections = new Set(visibleColumns);
		this.columnsChanged.emit({
			properties: visibleColumns
		})
	}
	onRowActivated(event) {
		if (this.rowActivated && event) {
			this.rowActivated.emit({
				type: event.type,
				event: event.event,
				row: event.row,
				column: event.column,
				value: event.value,
				cellElement: event.cellElement,
				rowElement: event.rowElement,
			});
		}
	}

	onColumnSort(event) {
		if (this.columnSort && event) {
			const sortDescriptorsCollection: SortDescriptor[][] = event.sorts.map(x => getColumnSortDescriptors(x, this.columnSortKeys.get(x.prop)));
			const sortEvent = {
				column: event.column,
				previousValue: event.prevValue,
				newValue: event.newValue,
				sortDescriptors: this.collectionUtils.flatten(sortDescriptorsCollection)
			};
			this.columnSort.emit(sortEvent);
		}
	}

	protected toggleSortListViewSort(): void{
		if(this.columnSort){
			const previousSort = this._defaultSort?.[0]?.dir as SortDirection;
			const sortField = this._defaultSort?.[0]?.prop as string;
			if(!sortField){
				return ;
			}

			switch(previousSort){
				case SortDirection.Ascending:{
					this.columnSort.emit({
						newValue: SortDirection.Descending,
						previousValue: SortDirection.Ascending,
						sortDescriptors: [{
							direction: SortDirection.Descending,
							property: sortField
						}]
					})
					return;
				}
				case SortDirection.Descending:{
					this.columnSort.emit({
						newValue: SortDirection.Ascending,
						previousValue: SortDirection.Descending,
						sortDescriptors: [{
							direction: SortDirection.Ascending,
							property: sortField
						}]
					})
					return;
				}
				default: return;
			}
		}
	}

	protected onListViewSort(byProperty: string): void{

		if(!byProperty){
			return;
		}

		if(!this.columnSort){
			return;
		}




		const previousSort = this._defaultSort?.[0]?.dir;
		const sort = previousSort ?? SortDirection.Descending;


		this.columnSort.emit({
			newValue: sort,
			previousValue: previousSort,
			sortDescriptors:[
				{
					direction: sort,
					property: byProperty
				}
			]
		})


	}


	getVisibleColumns(): TableColumnProp[] {
		if (!this.visibleColumns || this.visibleColumns.length === 0) { return this.columns.map(x => x.prop).filter(x => x); }
		return this.visibleColumns;
	}
}


export enum TableClass {
	Material = 'material',
	Bootstrap = 'bootstrap',
	Dark = 'dark'
}

export enum ColumnMode {
	Standard = 'standard',
	Flex = 'flex',
	Force = 'force'
}

export interface RowActivateEvent<T = any> {
	type: 'keydown' | 'click' | 'dblclick' | 'mouseenter';
	event: MouseEvent;
	row: T;
	column: ColumnDefinition;
	value: any;
	cellElement: Element;
	rowElement: Element;
}

export interface PageLoadEvent {
	count: number;
	pageSize: number;
	limit: number;
	offset: number;
}

export interface ColumnSortEvent {
	sortDescriptors: SortDescriptor[];
	column?: ColumnDefinition;
	previousValue: SortDirection;
	newValue: SortDirection;
}

export interface ColumnsChangedEvent {
	properties: TableColumnProp[];
}

export interface SortDescriptor {
	direction: SortDirection;
	property: string;
}

export enum SortDirection {
	Ascending = 'asc',
	Descending = 'desc'
}


export declare type TableColumnProp = string | number;
export interface ColumnDefinition {
	/**
     * Determines if column is checkbox
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	checkboxable?: boolean;
	/**
     * Determines if the column is frozen to the left
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	frozenLeft?: boolean;
	/**
     * Determines if the column is frozen to the right
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	frozenRight?: boolean;
	/**
     * The grow factor relative to other columns. Same as the flex-grow
     * API from http =//www.w3.org/TR/css3-flexbox/. Basically;
     * take any available extra width and distribute it proportionally
     * according to all columns' flexGrow values.
     *
     * @type {number}
     * @memberOf TableColumn
     */
	flexGrow?: number;
	/**
     * Min width of the column
     *
     * @type {number}
     * @memberOf TableColumn
     */
	minWidth?: number;
	/**
     * Max width of the column
     *
     * @type {number}
     * @memberOf TableColumn
     */
	maxWidth?: number;
	/**
     * The default width of the column, in pixels
     *
     * @type {number}
     * @memberOf TableColumn
     */
	width?: number;
	/**
     * Can the column be resized
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	resizeable?: boolean;
	/**
     * Custom sort comparator
     *
     * @type {*}
     * @memberOf TableColumn
     */
	comparator?: any;
	/**
     * Custom pipe transforms
     *
     * @type {PipeTransform}
     * @memberOf TableColumn
     */
	pipe?: PipeTransform;
	/**
     * Can the column be sorted
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	sortable?: boolean;
	/**
     * Can the column be re-arranged by dragging
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	/**
     * Override for the sort property that will be used (default is column.prop)
     *
     * @type {string}
     * @memberOf TableColumn
     */
	sortKeys?: string[];
	draggable?: boolean;
	/**
     * Whether the column can automatically resize to fill space in the table.
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	canAutoResize?: boolean;
	/**
     * Column name or label
     *
     * @type {string}
     * @memberOf TableColumn
     */
	name?: string;
	/**
     * Language Key Column name or label
     *
     * @type {string}
     * @memberOf TableColumn
     */
	languageName?: string;
	/**
     * Property to bind to the row. Example:
     *
     * `someField` or `some.field.nested`, 0 (numeric)
     *
     * If left blank, will use the name as camel case conversion
     *
     * @type {TableColumnProp}
     * @memberOf TableColumn
     */
	prop?: TableColumnProp;
	/**
     * Cell template ref
     *
     * @type {*}
     * @memberOf TableColumn
     */
	cellTemplate?: any;
	/**
     * Header template ref
     *
     * @type {*}
     * @memberOf TableColumn
     */
	headerTemplate?: any;
	/**
     * CSS Classes for the cell
     *
     *
     * @memberOf TableColumn
     */
	cellClass?: string | ((data: any) => string | any);
	/**
     * CSS classes for the header
     *
     *
     * @memberOf TableColumn
     */
	headerClass?: string | ((data: any) => string | any);
	/**
     * Header checkbox enabled
     *
     * @type {boolean}
     * @memberOf TableColumn
     */
	headerCheckboxable?: boolean;
	/**
     * Summary function
     *
     * @type {(cells: any[]) => any}
     * @memberOf TableColumn
     */
	summaryFunc?: (cells: any[]) => any;
	/**
     * Summary cell template ref
     *
     * @type {*}
     * @memberOf TableColumn
     */
	summaryTemplate?: any;
	alwaysShown?: boolean;
	valueFunction?: (cell: any) => any;
}
export interface StaticTableMessages {
	emptyMessage: string;
	totalMessage: string;
	selectedMessage: string;
}

function getColumnSortDescriptors(internalSortDescriptor, sortKeys: string[]): SortDescriptor[] {
	const direction = internalSortDescriptor.dir;
	return sortKeys.map(x => ({
		direction: direction,
		property: x
	}));
}

function buildColumnSortKeys(column: ColumnDefinition): [TableColumnProp, string[]] {
	return [column.prop, getColumnSortKeys(column)];
}

function getColumnSortKeys(column: ColumnDefinition): string[] {
	const sortKeys = extractSortKeys(column.sortKeys);
	if (sortKeys.length === 0) { sortKeys.push(String(column.prop)); }
	return sortKeys;
}

function extractSortKeys(sortKeys: string[]): string[] {
	sortKeys = sortKeys || [];
	return sortKeys.filter(x => Boolean(x));
}



export enum HybridListingMode{
	Table,
	List,
	Card
}
