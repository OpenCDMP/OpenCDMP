import { booleanAttribute, Component, EventEmitter, input, Input, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { ToCEntry } from '../models/toc-entry';
import { ToCEntryType } from '../models/toc-entry-type.enum';
import { TableOfContentsComponent } from '../table-of-contents.component';
import { Guid } from '@common/types/guid';

@Component({
    selector: 'table-of-contents-internal',
    styleUrls: ['./table-of-contents-internal.scss'],
    templateUrl: './table-of-contents-internal.html',
    standalone: false
})
export class TableOfContentsInternal extends BaseComponent implements OnInit, OnDestroy {

	@Input() tocentries: ToCEntry[] = null;
	@Input() selected: ToCEntry = null;
	@Output() entrySelected = new EventEmitter<ToCEntry>();
	@Input() propertiesFormGroup: UntypedFormGroup;

	expandChildren: boolean[];
	@Input() showErrors: boolean = false;
	@Input() visibilityRulesService: VisibilityRulesService;
    @Input({transform: booleanAttribute}) isTopLevel: boolean = false;

    ordinal = input<string>();

	constructor() { super(); }

	ngOnInit(): void {
		if (this.tocentries) {
			this.expandChildren = this.tocentries.map(() => false);
			if (this.selected) {
				for (let i = 0; i < this.tocentries.length; i++) {
					if (TableOfContentsComponent._findTocEntryById(this.selected.id, this.tocentries[i].subEntries)) {
						if (this.expandChildren) {
							this.expandChildren[i] = true;
						}
						break;
					}
				}
			}
		}
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes.selected && this.selected) {
			for (let i = 0; i < this.tocentries.length; i++) {
				if (TableOfContentsComponent._findTocEntryById(this.selected.id, this.tocentries[i].subEntries)) {
					if (this.expandChildren) {
						this.expandChildren[i] = true;
					}
					break;
				}
			}
		}
	}

	ngOnDestroy(): void {
		super.ngOnDestroy();
	}

	toggleExpand(index, entry: ToCEntry) {
		this.expandChildren[index] = !this.expandChildren[index];
        if(this.expandChildren[index]) {
            this.onEntrySelected(entry);
        }
	}

	onEntrySelected(entry: ToCEntry) {
		this.entrySelected.emit(entry);
	}


	calculateStyle(entry: ToCEntry) {
		const style = {};
		style['font-size'] = entry.type === ToCEntryType.FieldSet ? '.9em' : '1em';
		return style;
	}

	calculateClass(entry: ToCEntry) {
		return {
            'selected': this.selected && (entry.id === this.selected.id || this.selected.pathToEntry?.[0] === entry.id),
            'section': entry.type != ToCEntryType.FieldSet,
            'field-set': entry.type === ToCEntryType.FieldSet
        };
	}

	isTocEntryValid(entry: ToCEntry): boolean {
		if (entry == null) return true;

		if (entry.validityAbstractControl && (!entry.validityAbstractControl.touched || entry.validityAbstractControl.disabled)){
            return true;
        }
		let currentValidity = entry.validityAbstractControl?.valid ?? true;
		if (!currentValidity) return currentValidity;
		entry.subEntries?.forEach(subEntry => {
			currentValidity = currentValidity && this.isTocEntryValid(subEntry);
			if (!currentValidity){
                return currentValidity;
            }
		});

		return currentValidity;
	}
}
