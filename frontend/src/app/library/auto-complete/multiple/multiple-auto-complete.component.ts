import { FocusMonitor } from '@angular/cdk/a11y';
import { ENTER } from '@angular/cdk/keycodes';
import { Component, DoCheck, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Optional, Output, Self, SimpleChanges, TemplateRef, Type, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormGroupDirective, NgControl, NgForm } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { ErrorStateMatcher, mixinErrorState } from '@angular/material/core';
import { MatFormFieldControl } from '@angular/material/form-field';
import { AutoCompleteGroup } from '@app/library/auto-complete/auto-complete-group';
import {
	MultipleAutoCompleteConfiguration
} from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { BehaviorSubject, Observable, Subject, Subscription, interval, of as observableOf, of } from 'rxjs';
import { catchError, debounceTime, delayWhen, distinctUntilChanged, map, mergeMap, startWith, switchMap, take, takeUntil } from 'rxjs/operators';

export class CustomComponentBase extends BaseComponent {
	constructor(
		public _defaultErrorStateMatcher: ErrorStateMatcher,
		public _parentForm: NgForm,
		public _parentFormGroup: FormGroupDirective,
		public ngControl: NgControl,
		public stateChanges: Subject<void>
	) {
		super();
	}
}

export const _CustomComponentMixinBase = mixinErrorState(CustomComponentBase);

@Component({
	selector: 'app-multiple-auto-complete',
	templateUrl: './multiple-auto-complete.component.html',
	styleUrls: ['./multiple-auto-complete.component.scss'],
	providers: [{ provide: MatFormFieldControl, useExisting: MultipleAutoCompleteComponent }]
})
export class MultipleAutoCompleteComponent extends _CustomComponentMixinBase implements OnInit, MatFormFieldControl<string>, ControlValueAccessor, OnDestroy, DoCheck, OnChanges {

	static nextId = 0;
	@ViewChild('autocomplete', { static: true }) autocomplete: MatAutocomplete;
	@ViewChild('autocompleteTrigger', { static: true }) autocompleteTrigger: MatAutocompleteTrigger;
	@ViewChild('autocompleteInput', { static: true }) autocompleteInput: ElementRef;

	@Input()
	get configuration(): MultipleAutoCompleteConfiguration {
		return this._configuration;
	}

	set configuration(configuration: MultipleAutoCompleteConfiguration) {
		this._configuration = configuration;
	}

	private _configuration: MultipleAutoCompleteConfiguration;

	@Input()
	separatorKeysCodes = [];

	// Selected Option Event
	@Output() optionSelected: EventEmitter<any> = new EventEmitter();
	@Output() optionRemoved: EventEmitter<any> = new EventEmitter();

	@Output() optionActionClicked: EventEmitter<any> = new EventEmitter();


	id = `multiple-autocomplete-${MultipleAutoCompleteComponent.nextId++}`;
	stateChanges = new Subject<void>();

	valueOnBlur = new BehaviorSubject<any>(null);
	onSelectAutoCompleteValue = new BehaviorSubject<any>(null);
	queryValue: string = "";

	focused = false;
	controlType = 'multiple-autocomplete';
	describedBy = '';
	inputValue = '';
	_inputSubject = new Subject<string>();
	_items: Observable<any[]>;
	_selectedItems: Map<string, any> = new Map<any, any>();
	_groupedItems: Observable<AutoCompleteGroup[]>;
	selectable = false;
	selected: boolean = false;

	isMouseOverPanel: boolean = false;
	panelClosedSubscription: Subscription;

	get empty() {
		return (!this.value || this.value.length === 0) && (!this.inputValue || this.inputValue.length === 0);
	}

	get shouldLabelFloat() {
		return this.focused || !this.empty;
	}

	@Input() minLength: number = 0;
	@Input() showNoResultsLabel: boolean = true;

	@Input()
	get placeholder() {
		return this._placeholder;
	}

	set placeholder(placeholder) {
		this._placeholder = placeholder;
		this.stateChanges.next();
	}

	private _placeholder: string;

	@Input()
	get required() {
		return this._required;
	}

	set required(req) {
		this._required = !!(req);
		this.stateChanges.next();
	}

	private _required;

	@Input()
	get disabled() {
		return this._disabled;
	}

	set disabled(dis) {
		this._disabled = !!(dis);
		this.stateChanges.next();
	}

	private _disabled = false;

	@Input()
	get value(): any | null {
		return this._selectedValue;
	}

	set value(value: any | null) {
		this._selectedValue = (value != null && value.length === 0) ? null : value;
		this.stateChanges.next();
	}

	private _selectedValue;

	constructor(
		private fm: FocusMonitor,
		private elRef: ElementRef,
		@Optional() @Self() public ngControl: NgControl,
		@Optional() _parentForm: NgForm,
		@Optional() _parentFormGroup: FormGroupDirective,
		_defaultErrorStateMatcher: ErrorStateMatcher
	) {
		super(_defaultErrorStateMatcher, _parentForm, _parentFormGroup, ngControl, new Subject<void>());

		fm.monitor(elRef.nativeElement, true)
			.pipe(
				distinctUntilChanged(),
				switchMap(
					(origin) => of(origin).pipe(
						delayWhen((origin) => interval(!!origin ? 0 : 200).pipe(take(1))), // delay focusout (user may have made a selection) then close options if open
					)
				),
				takeUntil(this._destroyed)
			)
			.subscribe((origin) => {
				this.focused = !!origin;
				this.stateChanges.next();

				if (!this.isMouseOverPanel && !this.focused) {
					this.autocompleteTrigger?.closePanel();
				}
			});

		if (this.ngControl != null) {
			// Setting the value accessor directly (instead of using
			// the providers) to avoid running into a circular import.
			this.ngControl.valueAccessor = this;
		}
	}

	ngOnInit() {
		this.panelClosedSubscription = this.autocomplete.closed.subscribe((next) => {
			this.isMouseOverPanel=false;
		})
	 }

	ngDoCheck(): void {
		if (this.ngControl) {
			this.updateErrorState();
		}
	}

	ngOnChanges(changes: SimpleChanges) {

	}

	private gettingSelectedItem$ = new Subject<void>();
	getSelectedItems(value: any) {
		if (value != null && Array.isArray(value) && this.configuration) {
			const newSelections = value.filter(x => !this._selectedItems.has(JSON.stringify(x)));
			if (newSelections.length > 0 && this.configuration.getSelectedItems != null) {
				this.gettingSelectedItem$.next();
				this.configuration.getSelectedItems(newSelections).pipe(takeUntil(this._destroyed), takeUntil(this.gettingSelectedItem$)).subscribe(x => {

					x.forEach(element => {
						this._selectedItems.set(JSON.stringify(
							this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(element) :
								this.configuration.valueAssign != null ? this.configuration.valueAssign(element) : element
						), element);
					});
					this.value = value;

				});
			} else {
				newSelections.forEach(element => {
					this._selectedItems.set(JSON.stringify(
						this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(element) :
							this.configuration.valueAssign != null ? this.configuration.valueAssign(element) : element
					), element);
				});
			}
		}
	}

	getSelectedItem(value: any) {
		return this._selectedItems.get(JSON.stringify(this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(value) : value));
	}

	filter(query: string): Observable<any[]> {
		// If loadDataOnStart is enabled and query is empty we return the initial items.
		if (this.isNullOrEmpty(query) && this.loadDataOnStart) {
			return this.configuration.initialItems(this.value || [], this.configuration.extraData) || observableOf([]);
		} else if (query && query.length >= this.minFilteringChars) {
			if (this.configuration.filterFn) {
				return this.configuration.filterFn(query, this.value || [], this.configuration.extraData);
			} else {
				return this.configuration.initialItems(this.value || [], this.configuration.extraData) || observableOf([]);
			}
		} else {
			return observableOf([]);
		}
	}

	stringify(value: any): string {
		return JSON.stringify(value);
	}

	isNullOrEmpty(query: string): boolean {
		return typeof query !== 'string' || query === null || query.length === 0;
	}

	_optionSelected(event: MatAutocompleteSelectedEvent) {
		this.optionSelectedInternal(event.option.value);
		this.autocompleteInput.nativeElement.value = '';
		this._items = null; // refresh excluding previous item selected
	}

	private optionSelectedInternal(item: any) {
		const newValue = this._valueToAssign(item);

		//Update selected items
		this._selectedItems.set(JSON.stringify(this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(newValue) : newValue), item);

		const newValueArray = (Array.isArray(this.value) ? this.value : []);
		newValueArray.push(newValue);
		this._setValue(newValueArray);

		this.stateChanges.next();
		this.optionSelected.emit(item);
	}

	public onKeyUp(event) {
		this.inputValue = event.target.value;
		// prevent filtering results if arrow were pressed
		if (event.keyCode !== ENTER && (event.keyCode < 37 || event.keyCode > 40)) {


			if (!this._items) { // possibly from an error request here
				this._onInputFocus();
			}

			this._inputSubject.next(this.inputValue);
		}
	}

	private _setValue(value: any) {
		this.value = value;
		this.pushChanges(this.value);
	}

	_onInputFocus() {
		// We set the items observable on focus to avoid the request being executed on component load.
		if (!this._items) {
			this._items = this._inputSubject.pipe(
				startWith(null),
				debounceTime(this.requestDelay),
				distinctUntilChanged(),
				mergeMap(query => this.filter(query)),
				catchError(error => {
					this._items = null;
					console.error(error);
					return of([])
				})

			);

			if (this.configuration.groupingFn) { this._groupedItems = this._items.pipe(map(items => this.configuration.groupingFn(items))); }
		}
	}

	public onBlur($event: MouseEvent) {
		if (this.inputValue.length > 1 && this.autocomplete.options && this.autocomplete.options.length > 0 && this.autoSelectFirstOptionOnBlur) {
			this.optionSelectedInternal(this.autocomplete.options.first.value);
		}
        this._onTouched();
	}


	onChange = (_: any) => { };
	private _onTouched = () => { };
	writeValue(value: any): void {
		this.value = Array.isArray(value) ? value : null;
		// Update chips observable
		this.autocompleteInput.nativeElement.value = '';
		this._items = null;
		this.getSelectedItems(this.value);
	}
	pushChanges(value: any) { this.onChange(value); }
	registerOnChange(fn: (_: any) => {}): void { this.onChange = fn; }
	registerOnTouched(fn: () => {}): void { this._onTouched = fn; }
	setDisabledState(isDisabled: boolean): void { this.disabled = isDisabled; }
	setDescribedByIds(ids: string[]) { this.describedBy = ids.join(' '); }

	onContainerClick(event: MouseEvent) {
		event.stopPropagation();
		if (this.disabled) { return; }
		if (this.autocompleteInput) {
			this.fm.focusVia(this.autocompleteInput.nativeElement, 'program');
		}
		this._onInputFocus();
		if (!this.autocomplete.isOpen) {
			this.autocompleteTrigger.openPanel();
		}
	}

	ngOnDestroy() {
		this.stateChanges.complete();
		this.fm.stopMonitoring(this.elRef.nativeElement);
		this.gettingSelectedItem$.complete();
		this.panelClosedSubscription.unsubscribe();
	}

	//Configuration getters
	_displayFn(item: any): string {
		if (this.configuration.displayFn && item) { return this.configuration.displayFn(item); }
		return item;
	}

	_titleFn(item: any): string {
		if (this.configuration.titleFn && item) { return this.configuration.titleFn(item); }
		return item;
	}

	_optionComponent(item: any): Type<any> {
		if (this.configuration.optionComponent && item) { return this.configuration.optionComponent; }
		return null;
	}

	_optionTemplate(item: any): TemplateRef<any> {
		if (this.configuration.optionTemplate && item) { return this.configuration.optionTemplate; }
		return null;
	}

	_selectedValueTemplate(item: any): TemplateRef<any> {
		if (this.configuration.selectedValueTemplate && item) { return this.configuration.selectedValueTemplate; }
		return null;
	}

	_subtitleFn(item: any): string {
		if (this.configuration.subtitleFn && item) { return this.configuration.subtitleFn(item); }
		return null;
	}

	_valueToAssign(item: any): any {
		if (this.configuration.valueAssign && item) { return this.configuration.valueAssign(item); }
		return item;
	}

	get requestDelay(): number {
		return this.configuration.requestDelay != null ? this.configuration.requestDelay : 200;
	}

	get minFilteringChars(): number {
		return this.configuration.minFilteringChars != null ? this.configuration.minFilteringChars : 1;
	}

	get loadDataOnStart(): boolean {
		return this.configuration.loadDataOnStart != null ? this.configuration.loadDataOnStart : true;
	}

	get autoSelectFirstOptionOnBlur(): boolean {
		return this.configuration.autoSelectFirstOptionOnBlur != null ? this.configuration.autoSelectFirstOptionOnBlur : false;
	}

	//Chip Functions
	_addItem(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			this.inputValue = '';
		}
	}
	public reset(): void {
		this._inputSubject.unsubscribe();
		this._inputSubject = new Subject<string>();
		this._items = null;
	}

	_canRemoveItem(item: any): boolean {
		if(item != null && this.configuration.canRemoveItem != null) {
			return this.configuration.canRemoveItem(item)?.canRemove;
		}
		return true;
	}

	_canRemoveItemMessage(item: any): string {
		if(item != null && this.configuration.canRemoveItem != null) {
			const canRemoveResuslt = this.configuration.canRemoveItem(item);
			if (!canRemoveResuslt?.canRemove) return canRemoveResuslt.message;
		}
		return null;
	}

	_removeSelectedItem(item: any, event: MouseEvent): void {
		if (event != null) { event.stopPropagation(); }
		const valueToDelete = this._valueToAssign(item);
		this.value = this.value.filter(x => JSON.stringify(x) !== JSON.stringify(valueToDelete)); //TODO, maybe we need to implement equality here differently.
		this.optionRemoved.emit(item);
		this._items = null; // refresh query with excluded items

		//Update chips
		this._selectedItems.delete(JSON.stringify(valueToDelete));

		this.autocompleteInput.nativeElement.focus();
		this.pushChanges(this.value);
	}

	_optionActionClick(item: any, event: MouseEvent): void {
		if (event != null) {
			event.stopPropagation();
		}
		this.optionActionClicked.emit(item);
	}

	get popupItemActionIcon(): string {
		return this.configuration.popupItemActionIcon != null ? this.configuration.popupItemActionIcon : '';
	}

	get appendClassToItem(): { class: string, applyFunc: (item: any) => boolean }[] {
		return this.configuration.appendClassToItem !== null ? this.configuration.appendClassToItem : null;
	}

	private readonly empyObj = {};
	computeClass(value: any) {
		if (!(this.appendClassToItem && this.appendClassToItem.length)) {
			return this.empyObj;
		}

		const classes = this.appendClassToItem.filter(e => e.applyFunc(value));
		return classes.reduce((r, current) => {
			r[current.class] = true;
			return r;
		}, {});

	}
}

export interface MultipleAutoCompleteCanRemoveItem {
	canRemove: boolean;
	message: string;
}
