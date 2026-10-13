import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, Input, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TagService } from '@app/core/services/tag/tag.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MultipleAutoCompleteComponent } from '../../../library/auto-complete/multiple/multiple-auto-complete.component';

@Component({
    selector: 'app-tags-field-component',
    templateUrl: 'tags-field.component.html',
    styleUrls: ['./tags-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatLabel, MultipleAutoCompleteComponent, FormsModule, ReactiveFormsModule]
})
export class TagsComponent extends BaseComponent {
    static nextId: number = 0;
	@Input() form: FormControl<string> = null;
	@Input() label: string;
	@Input() hideLabel: boolean = false;
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
