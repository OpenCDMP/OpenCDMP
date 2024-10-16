import { DOCUMENT } from '@angular/common';
import { Component, EventEmitter, Inject, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { BaseComponent } from '@common/base/base.component';
import { NewEntryType, ToCEntry, ToCEntryType } from '../description-template-table-of-contents-entry';

@Component({
	selector: 'app-description-template-table-of-contents-internal-section',
	styleUrls: ['./description-template-table-of-contents-internal-section.scss'],
	templateUrl: './description-template-table-of-contents-internal-section.html'
})
export class DescriptionTemplateTableOfContentsInternalSection extends BaseComponent implements OnInit {

	@Input() links: ToCEntry[];
	@Output() itemClick = new EventEmitter<ToCEntry>();
	@Output() removeEntry = new EventEmitter<ToCEntry>();

	@Output() createFooEntry = new EventEmitter<NewEntryType>();

	@Output() dataNeedsRefresh = new EventEmitter<void>();



	@Input() parentLink: ToCEntry;
	@Input() itemSelected: ToCEntry;
	@Input() DRAGULA_ITEM_ID_PREFIX;
	@Input() overContainerId: string;
	@Input() isDragging;
	@Input() draggingItemId: string;
	@Input() parentRootId: string;

	@Input() colorizeInvalid: boolean = false;

	@Input() viewOnly: boolean;
	@Input() showErrors: boolean = false;

	constructor(
		@Inject(DOCUMENT) private _document: Document) {
		super();
	}

	tocEntryType = ToCEntryType;

	ngOnInit(): void {
	}

	ngOnChanges(changes: SimpleChanges) {

	}

	itemClicked(item: ToCEntry) {
		//leaf node
		this.itemClick.emit(item);
	}

	deleteEntry(currentLink: ToCEntry) {
		this.removeEntry.emit(currentLink);
	}

	createNewEntry(foo: NewEntryType) {
		this.createFooEntry.emit(foo);
	}

	get selectedItemInLinks() {
		if (!this.links || !this.itemSelected) return false;

		const link = this.links.find(l => l.id === this.itemSelected.id);

		if (link) return true;
		return false;
	}

	onDataNeedsRefresh() {
		this.dataNeedsRefresh.emit();
	}

	get linksType(): ToCEntryType {
		if (!this.links || !this.itemSelected) return;

		return this.links[0].type;
	}

	public _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
		if (!tocentries) {
			return null;
		}

		let tocEntryFound = tocentries.find(entry => entry.id === id);

		if (tocEntryFound) {
			return tocEntryFound;
		}

		for (let entry of tocentries) {
			const result = this._findTocEntryById(id, entry.subEntries);
			if (result) {
				tocEntryFound = result;
				break;
			}
		}

		return tocEntryFound ? tocEntryFound : null;
	}



	colorError(): boolean {

		if (!this.colorizeInvalid) return false;

		const form = this.parentLink.form;
		if ((!form || form.valid || !form.touched) && this.parentLink.type !== this.tocEntryType.Page) return false;

		const allFieldsAreTouched = this.allFieldsAreTouched(form);

		//fieldset may have errros that are inside its controls and not in the fieldsetFormGroup
		if (this.parentLink.type === this.tocEntryType.FieldSet && allFieldsAreTouched) return true;

		if (form.errors && allFieldsAreTouched) return true;

		//check if page has sections
		if (this.parentLink.type === this.tocEntryType.Page && allFieldsAreTouched) {
			const rootForm = form.root;
			if (rootForm) {
				const sections = rootForm.get('sections') as UntypedFormArray;
				if (!sections.controls.find(section => section.get('page').value === this.parentLink.id)) {
					return true;
				}
			}
		}


		//checking first child form controls if have errors
		let hasErrors = false;
		if (allFieldsAreTouched) {
			if (form instanceof UntypedFormGroup) {
				const formGroup = form as UntypedFormGroup;

				const controls = Object.keys(formGroup.controls);

				controls.forEach(control => {
					if (formGroup.get(control).errors) {
						hasErrors = true;
					}
				})

			}
		}

		return hasErrors;
	}


	allFieldsAreTouched(aControl: AbstractControl) {//auto na testaroume

		if (!aControl || aControl.untouched) return false;

		if (aControl instanceof UntypedFormControl) {
			return aControl.touched;
		} else if (aControl instanceof UntypedFormGroup) {
			const controlKeys = Object.keys((aControl as UntypedFormGroup).controls);
			let areAllTouched = true;
			controlKeys.forEach(key => {
				if (!this.allFieldsAreTouched(aControl.get(key))) {
					areAllTouched = false;
				}
			})
			return areAllTouched;

		} else if (aControl instanceof UntypedFormArray) {
			const controls = (aControl as UntypedFormArray).controls;
			let areAllTouched = true;
			controls.forEach(control => {
				if (!this.allFieldsAreTouched(control)) {
					areAllTouched = false;
				}
			});
			return areAllTouched;
		}


		return false;
	}
}