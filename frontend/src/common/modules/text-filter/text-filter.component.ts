
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { MatFormField, MatPrefix, MatSuffix, MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { ReactiveAsteriskDirective } from '../../forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-text-filter',
    templateUrl: './text-filter.component.html',
    styleUrls: ['./text-filter.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatIcon, MatPrefix, MatSuffix, MatLabel, ReactiveAsteriskDirective, MatInput, FormsModule, TranslatePipe]
})
export class TextFilterComponent extends BaseComponent implements OnInit, OnChanges {
	@Input() typeaheadMS = 700;
	@Input() disableTransform = false;
	@Input() floatLabel: string = "never";
	@Input() label: string | null;
	@Input() placeholder: string | null;
	@Input() suffixIcon: string | null;
	@Input() prefixIcon: string | null;
	@Input() value: string;
	@Output() valueChange = new EventEmitter<string>();

	private valueSubject: Subject<string>;

	get textPlaceholder(): string {
		return this.placeholder ? this.placeholder : 'GENERAL.CRITERIA.LIKE';
	}

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

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['value']) {
			this._inputValue = this.filterService.reverseLikeTransformation(this.value);
		}
	}
}
