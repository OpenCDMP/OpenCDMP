import { FocusMonitor } from '@angular/cdk/a11y';
import { ENTER } from '@angular/cdk/keycodes';
import { Component, DoCheck, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Optional, Output, Self, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormGroupDirective, NgControl, NgForm } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { ErrorStateMatcher, mixinErrorState } from '@angular/material/core';
import { MatFormFieldControl } from '@angular/material/form-field';
import { AutoCompleteGroup } from '@app/library/auto-complete/auto-complete-group';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { Observable, Subject, Subscription, of as observableOf, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, mergeMap, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';


export class CustomComponentBase extends BaseComponent {
	constructor(
		public _defaultErrorStateMatcher: ErrorStateMatcher,
		public _parentForm: NgForm,
		public _parentFormGroup: FormGroupDirective,
		public ngControl: NgControl,
		public stateChanges: Subject<void>
	) { super(); }
}
export const _CustomComponentMixinBase = mixinErrorState(CustomComponentBase);

@Component({
    selector: 'app-single-auto-complete',
    templateUrl: './single-auto-complete.component.html',
    styleUrls: ['./single-auto-complete.component.scss'],
    providers: [{ provide: MatFormFieldControl, useExisting: SingleAutoCompleteComponent }],
    standalone: false
})
export class SingleAutoCompleteComponent extends _CustomComponentMixinBase implements OnInit, MatFormFieldControl<string>, ControlValueAccessor, OnDestroy, DoCheck, OnChanges {

	isMouseOverPanel: boolean = false;
	panelClosedSubscription: Subscription;

	static nextId = 0;
	@ViewChild('autocomplete', { static: true }) autocomplete: MatAutocomplete;
	@ViewChild('autocompleteTrigger', { static: true }) autocompleteTrigger: MatAutocompleteTrigger;
	@ViewChild('autocompleteInput', { static: true }) autocompleteInput: ElementRef;

	@Input() initialSelectedData: any;
    @Input({required: false}) id: string = `single-autocomplete-${SingleAutoCompleteComponent.nextId++}`;

	@Input()
	get configuration(): SingleAutoCompleteConfiguration { return this._configuration; }
	set configuration(configuration: SingleAutoCompleteConfiguration) {
		this._configuration = configuration;
		//On startup, fill the input with the existing value
		if (this.autocompleteInput && this.value) {
			this.inputValue = this._displayFn(this.value);
		}
	}
	private _configuration: SingleAutoCompleteConfiguration;

	// Selected Option Event
	@Output() optionSelected: EventEmitter<any> = new EventEmitter();
	@Output() optionActionClicked: EventEmitter<any> = new EventEmitter();

	stateChanges = new Subject<void>();
	focused = false;
	controlType = 'single-autocomplete';
	describedBy = '';
	inputValue = '';
	_inputSubject = new Subject<string>();
	loading = false;
	_items: Observable<any[]>;
	_groupedItems: Observable<AutoCompleteGroup[]>;
	_selectedItems: Map<string, any> = new Map<any, any>();

	get empty() { return (this.value == null) && (!this.inputValue || this.inputValue.length === 0); }

	get shouldLabelFloat() { return this.focused || !this.empty; }

	@Input()
	get placeholder() { return this._placeholder; }
	set placeholder(placeholder) {
		this._placeholder = placeholder;
		this.stateChanges.next();
	}
	private _placeholder: string;

	@Input()
	get required() { return this._required; }
	set required(req) {
		this._required = !!(req);
		this.stateChanges.next();
	}
	private _required;

	@Input()
	get disabled() { return this._disabled; }
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
		this._selectedValue = value;
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

		fm.monitor(elRef.nativeElement, true).pipe(takeUntil(this._destroyed)).subscribe((origin) => {
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
			this.isMouseOverPanel = false;
		});
	}

	ngDoCheck(): void {
		if (this.ngControl) {
			this.updateErrorState();
		}
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['initialSelectedData'] && changes['initialSelectedData'].currentValue && changes['initialSelectedData'].isFirstChange && this.configuration != null) {
			this._selectedItems.set(this.selectedItemKey(this.initialSelectedData), this.initialSelectedData);
			this.inputValue = this._displayFn(this.initialSelectedData);
		}

		if (changes['configuration'] && changes['configuration'].isFirstChange) {
			if (changes['initialSelectedData'] != null && this.value != null) {
				if (this._valueToAssign(this.initialSelectedData) != this.value) { this.getSelectedItems(this.value); }
			} else {
				this.getSelectedItems(this.value);
			}
		}
	}

	getSelectedItems(value: any) {
		if (value != null && !this._selectedItems.has(this.selectedItemKey(value)) && this.configuration) {
			if (this.configuration.getSelectedItem != null) {
				this.configuration.getSelectedItem(value).pipe(takeUntil(this._destroyed)).subscribe(x => {
					if (x != null) {
						this._selectedItems.set(this.selectedItemKey(x), x);
						this.inputValue = this._displayFn(x);
					}
				});
			} else {
				this._selectedItems.set(this.selectedItemKey(value), value);
				this.inputValue = this._displayFn(value);
			}
		}
	}

	filter(query: string): Observable<any[]> {
		// If loadDataOnStart is enabled and query is empty we return the initial items.
		if (this.isNullOrEmpty(query) && this.loadDataOnStart) {
			return this.configuration.initialItems(this.configuration.extraData) || observableOf([]);
		} else if (query && query.length >= this.minFilteringChars) {
			if (this.configuration.filterFn) {
				return this.configuration.filterFn(query, this.configuration.extraData);
			} else {
				return this.configuration.initialItems(this.configuration.extraData) || observableOf([]);
			}
		} else {
			return observableOf([]);
		}
	}

	stringify(value: any): string {
		if (typeof value === 'string' || value instanceof String)
			return value as string;
		else
			return JSON.stringify(value);
	}

	isNullOrEmpty(query: string): boolean {
		return typeof query !== 'string' || query === null || query.length === 0;
	}

	_optionSelected(event: MatAutocompleteSelectedEvent) {
		this.inputValue = this._displayFn(event.option.value);
		this.optionSelectedInternal(event.option.value);
	}

	_optionActionClick(item: any, event: MouseEvent): void {
		if (event != null) {
			event.stopPropagation();
		}
		this.optionActionClicked.emit(item);
	}

	public clearValue(emitEvent: boolean = true): void {
		this._setValue(null);
		this.stateChanges.next();
        if(emitEvent){
            this.optionSelected.emit(null);
        }
		this.inputValue = null;
		this._items = null;
	}

	private optionSelectedInternal(item: any) {
		const newValue = this._valueToAssign(item);

		//Update selected items
		this._selectedItems.set(this.stringify(this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(newValue) : newValue), item);

		this._setValue(newValue);

		this.stateChanges.next();
		this.optionSelected.emit(item);
	}

	public onKeyUp(event) {
		this.inputValue = event.target.value;
		// prevent filtering results if arrow were pressed
		if (event.keyCode !== ENTER && (event.keyCode < 37 || event.keyCode > 40)) {
			if (this.inputValue.length === 0 && this.value != null) {
				this.clearValue();
				this._onInputFocus();
				return;
			}
			this._inputSubject.next(this.inputValue);
		}
	}

	private _setValue(value: any) {
		this.value = value;
		this.pushChanges(this.value);
	}

	_onInputFocus() {
		if (!this._items) {
			this._items = this._inputSubject.pipe(
				startWith(null),
				debounceTime(this.requestDelay),
				distinctUntilChanged(),
				mergeMap(query => this.filter(query)),
				catchError(error => {
					this._items = null;
					console.error(error);
					return of(null)
				})
			);
			if (this.configuration.groupingFn) { this._groupedItems = this._items.pipe(map(items => this.configuration.groupingFn(items))); }
		}
	}

	public onBlur($event: MouseEvent) {
		if (this.value != null) {
			const inputLabel = this.inputValue;
			const selectedLabel = this._displayFn(this._selectedItems.get(this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(this.value) : this.value));
			if (inputLabel && selectedLabel !== inputLabel) {
				this.inputValue = selectedLabel;
			}
		} else if (this.inputValue && this.inputValue.length > 1 && this.autocomplete.options && this.autocomplete.options.length > 0 && this.autoSelectFirstOptionOnBlur) {
			this.inputValue = this._displayFn(this.autocomplete.options.first.value);
			this.optionSelectedInternal(this.autocomplete.options.first.value);
		}

		// Clear text if not an option
		else if (this.inputValue && this.inputValue.length > 1) {
			this.inputValue = '';
			document.getElementById((<HTMLInputElement>$event.target).id).focus();
		}

        this._onTouched();
	}

	onChange = (_: any) => { };
	private _onTouched = () => { };
	writeValue(value: any): void {
		this.value = value;
		// Update chips observable
		if (value != null) { this.getSelectedItems(value); } else {
			if (this.autocompleteInput && this.autocompleteInput.nativeElement && this.autocompleteInput.nativeElement.value) { this.autocompleteInput.nativeElement.value = ''; }
			this.inputValue = null;
			this._items = null;
		}
	}
	pushChanges(value: any) { this.onChange(value); }
	registerOnChange(fn: (_: any) => {}): void { this.onChange = fn; }
	registerOnTouched(fn: () => {}): void { this._onTouched = fn; }
	setDisabledState(isDisabled: boolean): void { this.disabled = isDisabled; }
	setDescribedByIds(ids: string[]) { this.describedBy = ids.join(' '); }

	onContainerClick(event: MouseEvent) {
		event.stopPropagation();
		if (this.disabled) { return; }
		this._onInputFocus();
		if (!this.autocomplete.isOpen) {
			this.autocompleteTrigger.openPanel();
		}
	}

	ngOnDestroy() {
		this.panelClosedSubscription?.unsubscribe();
		this.stateChanges?.complete();
		this.fm?.stopMonitoring(this.elRef?.nativeElement);
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
		return this.configuration.requestDelay != null ? this.configuration.requestDelay : 600;
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

	get popupItemActionIcon(): string {
		return this.configuration.popupItemActionIcon != null ? this.configuration.popupItemActionIcon : '';
	}

	private selectedItemKey(item: any) {
		return this.stringify(
			this.configuration.uniqueAssign != null ? this.configuration.uniqueAssign(item) :
				this.configuration.valueAssign != null ? this.configuration.valueAssign(item) : item
		)
	}
}
