
import { TAB } from '@angular/cdk/keycodes';
import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { BaseComponent } from '@common/base/base.component';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime, shareReplay, startWith, takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-expandable-search-field',
    templateUrl: './expandable-search-field.component.html',
    styleUrls: ['./expandable-search-field.component.scss'],
    standalone: false,
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: ExpandableSearchFieldComponent, multi: true }]
})
export class ExpandableSearchFieldComponent extends BaseComponent implements OnInit, ControlValueAccessor {
    @ViewChild('inputField', { static: true }) inputField: ElementRef;
    
	@Input() typeaheadMS = 700;
	@Input() disableTransform = false;
	@Input() placeholder: string;

	@Input() value: string; //remove

	@Output() valueChange = new EventEmitter<string>();

	private valueSubject: Subject<string>  = new Subject<string>();
    disabled: boolean;

	inputValue: string;
	// public get inputValue(): string { return this._inputValue; }
	// public set inputValue(value: string) {
	// 	this._inputValue = value;
	// 	this.valueSubject.next(this.disableTransform ? value : this.filterService.transformLike(value));
	// }

	constructor(
		private language: TranslateService,
		private filterService: FilterService
	) {
		super();
	}

	onChange = (_: any) => { };
	private _onTouched = () => { };
    writeValue(value: string): void {
		if (value) {
            this.inputValue = this.filterService.reverseLikeTransformation(value);
        } else {
			if (this.inputField && this.inputField.nativeElement && this.inputField.nativeElement.value) { 
                this.inputField.nativeElement.value = ''; 
            }
			this.inputValue = null;
		}
    }
	registerOnChange(fn: (_: any) => {}): void { this.onChange = fn; }
	registerOnTouched(fn: () => {}): void { this._onTouched = fn; }
    setDisabledState(isDisabled: boolean): void { this.disabled = isDisabled; }


    private setValue(value){
        this.inputValue = value ?? null;
        const transformedValue = this.disableTransform ? this.inputValue : this.filterService.transformLike(this.inputValue);
        this.onChange(transformedValue);
        this.valueChange.emit();
    }

	ngOnInit() {
		this.valueSubject.pipe(
			debounceTime(this.typeaheadMS),
			takeUntil(this._destroyed))
			.subscribe(value => {
				if (value === '') { value = null; }
				if (this.inputValue !== value) {
                    this.setValue(value);
				}
			});
	}

    public onKeyUp(event) {
        if (event.keyCode !== TAB) {
            this.valueSubject.next(event.target.value);
        }
    }

	private subject$ = new Subject<boolean>();



	protected a$ = this.subject$.asObservable().pipe(
		debounceTime(200),
		takeUntil(this._destroyed),
		shareReplay(),
		startWith(false)
	);


	protected onOpen(){
		this.subject$.next(true);
	}
	
	protected onClose(){
		this.subject$.next(false);
	}


	// ngOnChanges(changes: SimpleChanges): void {
	// 	if (changes['value']) {
	// 		this._inputValue = this.filterService.reverseLikeTransformation(this.value);
	// 	}
	// }

	getPlaceholder(): string {
		return this.placeholder ? this.language.instant(this.placeholder) : this.language.instant('COMMONS.TEXT-FILTER.LIKE');
	}
}
