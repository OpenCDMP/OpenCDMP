
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime, shareReplay, startWith, takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-expandable-search-field',
	templateUrl: './expandable-search-field.component.html',
	styleUrls: ['./expandable-search-field.component.scss']
})
export class ExpandableSearchFieldComponent extends BaseComponent implements OnInit, OnChanges {
	@Input() typeaheadMS = 700;
	@Input() disableTransform = false;
	@Input() placeholder: string;
	@Input() value: string;
	@Output() valueChange = new EventEmitter<string>();

	private valueSubject: Subject<string>;

	private _inputValue: string;
	public get inputValue(): string { return this._inputValue; }
	public set inputValue(value: string) {
		this._inputValue = value;
		this.valueSubject.next(this.disableTransform ? value : this.filterService.transformLike(value));
	}

	constructor(
		private language: TranslateService,
		private filterService: FilterService
	) {
		super();
	}

	ngOnInit() {
		this.valueSubject = new Subject<string>();
		this.valueSubject.pipe(
			debounceTime(this.typeaheadMS),
			takeUntil(this._destroyed))
			.subscribe(value => {
				if (value === '') { value = null; }
				if (this.value !== value) {
					this.value = value;
					this.valueChange.emit(value);
				}
			});
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


	ngOnChanges(changes: SimpleChanges): void {
		if (changes['value']) {
			this._inputValue = this.filterService.reverseLikeTransformation(this.value);
		}
	}

	getPlaceholder(): string {
		return this.placeholder ? this.language.instant(this.placeholder) : this.language.instant('COMMONS.TEXT-FILTER.LIKE');
	}
}
