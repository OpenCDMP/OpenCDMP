import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormControl, UntypedFormControl, Validators } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { Tag } from '@app/core/model/tag/tag';
import { TagService } from '@app/core/services/tag/tag.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { QueryResult } from '@common/model/query-result';
import { Observable, of } from 'rxjs';
import { filter, map, mergeMap, startWith, takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-tags-field-component',
    templateUrl: 'tags-field.component.html',
    styleUrls: ['./tags-field.component.scss'],
    standalone: false
})
export class TagsComponent extends BaseComponent {
    static nextId: number = 0;
	@Input() form: FormControl<string> = null;
	@Input() label: string;
	@Input() placeholder: string;
    @Input() name: string = `tag-field-${TagsComponent.nextId++}`;
    @Input() required: boolean = false;
	separatorKeysCodes: number[] = [ENTER, COMMA];
	filteredTags: Observable<string[]>;
	tags: string[] = [];

	@ViewChild('tagInput') tagInput: ElementRef<HTMLInputElement>;

	constructor(
		private tagService: TagService,
	) {
		super();
	}

    get isRequired(): boolean {
        return this.required || this.form?.hasValidator(Validators.required)
    }

    multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
        initialItems: (excludedItems: any[], data?: any) => this.tagService.query(this.tagService.buildAutocompleteLookup()).pipe(map(x => x.items?.filter((item) => !excludedItems.includes(item.label))?.map((x) => x.label))),
        filterFn: (searchQuery: string, excludedItems: any[]) => this.tagService.query(this.tagService.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items?.filter((item) => !excludedItems.includes(item.label))?.map((x) => x.label))),
        getSelectedItems: (selectedItems: any[]) => of(selectedItems),
        displayFn: (item: string) => item,
        titleFn: (item: string) => item,
        valueAssign: (item: string) => item,
    }
}
