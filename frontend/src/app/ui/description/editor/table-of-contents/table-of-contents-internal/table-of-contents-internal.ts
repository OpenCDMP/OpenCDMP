import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { ToCEntry } from '../models/toc-entry';
import { ToCEntryType } from '../models/toc-entry-type.enum';
import { TableOfContentsComponent } from '../table-of-contents.component';

@Component({
	selector: 'table-of-contents-internal',
	styleUrls: ['./table-of-contents-internal.scss'],
	templateUrl: './table-of-contents-internal.html'
})
export class TableOfContentsInternal extends BaseComponent implements OnInit, OnDestroy {

	@Input() tocentries: ToCEntry[] = null;
	@Input() selected: ToCEntry = null;
	@Output() entrySelected = new EventEmitter<ToCEntry>();
	@Input() propertiesFormGroup: UntypedFormGroup;

	expandChildren: boolean[];
	@Input() showErrors: boolean = false;
	@Input() visibilityRulesService: VisibilityRulesService;

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

	toggleExpand(index) {
		this.expandChildren[index] = !this.expandChildren[index];
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
		const myClass = {};

		if (this.selected && entry.id === this.selected.id) {
			myClass['selected'] = true;
		}

		if (entry.type != ToCEntryType.FieldSet) {
			myClass['section'] = true;
		}
		return myClass;
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
