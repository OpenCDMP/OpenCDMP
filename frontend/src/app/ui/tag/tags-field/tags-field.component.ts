import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormControl, UntypedFormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { Tag } from '@app/core/model/tag/tag';
import { TagService } from '@app/core/services/tag/tag.service';
import { BaseComponent } from '@common/base/base.component';
import { QueryResult } from '@common/model/query-result';
import { Observable } from 'rxjs';
import { map, mergeMap, startWith, takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-tags-field-component',
    templateUrl: 'tags-field.component.html',
    styleUrls: ['./tags-field.component.scss'],
    standalone: false
})
export class TagsComponent extends BaseComponent implements OnInit, OnChanges {
    static nextId: number = 0;
	@Input() form: UntypedFormControl = null;
	@Input() label: string;
	@Input() placeholder: string;
    @Input() name: string = `tag-field-${TagsComponent.nextId++}`;
	separatorKeysCodes: number[] = [ENTER, COMMA];
	filteredTags: Observable<string[]>;
	tags: string[] = [];

	@ViewChild('tagInput') tagInput: ElementRef<HTMLInputElement>;

	tagsCtrl = new FormControl();

	constructor(
		private tagService: TagService,
	) {
		super();
	}

	ngOnInit(): void {
		this.applyTags();
	}

	ngOnChanges(changes: SimpleChanges) {
		if(changes['form']) {
			this.form?.valueChanges.pipe(takeUntil(this._destroyed)).subscribe( _ => this.applyTags());

			this.applyTags();
		}
	}

	applyTags(){
		this.tags = this.form.value || [];
		this.filteredTags = this.tagsCtrl.valueChanges.pipe(
			startWith(null),
			mergeMap((tag: string | null) => (this.tagService.query(this.tagService.buildAutocompleteLookup(tag)))),
			map((queryResult: QueryResult<Tag>) => queryResult.items.map(x => x.label)),
		);
	}

	add(event: MatChipInputEvent): void {
		if(this.form.disabled == true) return;

		const value = (event.value || '').trim();

		// Add our tag
		if (value) {
			this.tags.push(value);
		}

		// Clear the input value
		event.chipInput!.clear();

		this.form.setValue(this.tags);
	}

	remove(tag: string): void {
		if(this.form.disabled == true) return;

		const index = this.tags.indexOf(tag);

		if (index >= 0) {
			this.tags.splice(index, 1);
			this.form.setValue(this.tags);
            this.form.markAsDirty();
		}
	}

	selected(event: MatAutocompleteSelectedEvent): void {
		this.tags.push(event.option.viewValue);
		this.tagInput.nativeElement.value = '';
		this.form.setValue(this.tags);
        this.form.markAsDirty();
	}
}
