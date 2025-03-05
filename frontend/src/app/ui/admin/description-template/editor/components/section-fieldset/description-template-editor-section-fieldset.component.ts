import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { Guid } from '@common/types/guid';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { DragulaService } from 'ng2-dragula';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { GENERAL_ANIMATIONS } from '../../../../../../library/animations/animations';
import { DescriptionTemplateFieldEditorModel, DescriptionTemplateFieldSetEditorModel } from '../../description-template-editor.model';
import { ToCEntry, ToCEntryType } from '../../table-of-contents/description-template-table-of-contents-entry';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';

@Component({
    selector: 'app-description-template-editor-section-fieldset-component',
    templateUrl: './description-template-editor-section-fieldset.component.html',
    styleUrls: ['./description-template-editor-section-fieldset.component.scss'],
    animations: [GENERAL_ANIMATIONS],
    standalone: false
})

export class DescriptionTemplateEditorSectionFieldSetComponent implements OnInit, OnChanges, OnDestroy {
	@Input() viewOnly: boolean;
	@Input() tocentry: ToCEntry;

	@Input() descriptionTemplateId?: string;

	@Output() selectedEntryId = new EventEmitter<string>();
	@Output() dataNeedsRefresh = new EventEmitter<void>();
	@Output() removeFieldSet = new EventEmitter<string>();
	@Output() addNewFieldSet = new EventEmitter<UntypedFormGroup>();
	@Output() cloneFieldSet = new EventEmitter<UntypedFormGroup>();
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;

	@Input() availableReferenceTypes: ReferenceType[] = [];

	idprefix = "id";
	reorderingMode = false;
	readonly dragula_prefix = "dragulaid";
	private subs = new Subscription();
	private FIELDSETS = 'FIELDSETS';

	private initializer = new Subject<void>();
	private scroller = new Subject<string>();
	constructor(
		private dragulaService: DragulaService,
		private myElement: ElementRef
	) {
		if (this.dragulaService.find(this.FIELDSETS)) {
			this.dragulaService.destroy(this.FIELDSETS);
		}


		this.dragulaService.createGroup(this.FIELDSETS, {
			moves: (el, container, handle) => {
				if (handle.className && handle.classList.contains('handle')) return true;
				return false;
			}
		});
		this.subs.add(this.dragulaService.drop(this.FIELDSETS).subscribe(obs => {


			(this.form.get('fieldSets') as UntypedFormArray).controls.forEach((e, i) => {
				e.get('ordinal').setValue(i);
			});


			// obs.
			
			this.dataNeedsRefresh.emit();
			return;
		}));

		this.subs.add(this.dragulaService.dragend(this.FIELDSETS).subscribe(({ el }) => {
			this.exitReordering();
		}));

		const initializerSub = this.initializer
			.pipe(
				debounceTime(100)
			)
			.subscribe(() => {
				this.initialize();
			});
		this.subs.add(initializerSub);
		this.subs.add(
			this.scroller
				.pipe(debounceTime(700))
				.subscribe(id => {
					if (isNullOrUndefined(id)) {
						this._scrollOnTop();
						return;
					}
					this._scrollToElement(id);
				})
		);
	}
	ngOnDestroy() {
		this.subs.unsubscribe();
	}
	ngOnChanges(changes: SimpleChanges): void {
		this.initializer.next();
	}


	form;
	numbering;



	private _selectedFieldSetId: string = null;
	get selectedFieldSetId() {
		return this._selectedFieldSetId;
	}
	set selectedFieldSetId(id: string) {

		if (id === this._selectedFieldSetId) return;
		this._selectedFieldSetId = id;
		this.selectedEntryId.emit(id);
	}


	private initialize() {
		if (this.tocentry.type === ToCEntryType.Section) {
			this.form = this.tocentry.form;
			this.numbering = this.tocentry.numbering;
			this._selectedFieldSetId = null;

			this._scrollOnTop(true);
		} else if (this.tocentry.type === ToCEntryType.FieldSet) {
			this.form = this.tocentry.form.parent.parent;
			const numberingArray = this.tocentry.numbering.split('.');
			if (numberingArray.length) {
				numberingArray.splice(numberingArray.length - 1);
				this.numbering = numberingArray.join('.');
			} else {
			}
			this._selectedFieldSetId = this.tocentry.id;

			this.scroller.next(this.tocentry.id);
		} else {
			this.scroller.next(null);
		}
	}

	private _scrollToElement(id: string) {
		let el = this.myElement.nativeElement.querySelector("#" + this.idprefix + id);
		if (el) {
			el.scrollIntoView({ behavior: "smooth", block: 'start' });
		}
	}
	private _scrollOnTop(instant?: boolean) {
		const el = this.myElement.nativeElement.querySelector('#topofcontainer');
		if (el) {
			if (instant) {
				el.scrollIntoView({ block: 'end' });
			} else {
				el.scrollIntoView({ behavior: 'smooth', block: 'end' });
			}
		}
	}
	ngOnInit() {

	}

	onRemoveFieldSet(fieldsetId: string) {
		this.removeFieldSet.emit(fieldsetId);
	}

	onCloneFieldSet(fieldset: UntypedFormGroup) {
		this.cloneFieldSet.emit(fieldset);
	}
	onAddFieldSet() {
		// this.addNewFieldSet.emit(this.form);
		try {
			const length = (this.form.get('fieldSets') as UntypedFormArray).length;
			if (length === 0) {
				this.addFieldSetAfter(-9999, 0);
				return;
			}
			else {
				const lastElement = (this.form.get('fieldSets') as UntypedFormArray).at(length - 1);
				this.addFieldSetAfter(lastElement.get('ordinal').value, length - 1);
			}
		} catch { }
	}

	addFieldSetAfter(afterOrdinal: number, afterIndex: number): void {
		const field: DescriptionTemplateFieldEditorModel = new DescriptionTemplateFieldEditorModel(this.validationErrorModel);
		field.id = Guid.create().toString();
		field.ordinal = 0;//first filed in the fields list
		const fieldForm = field.buildForm({rootPath: this.validationRootPath + '.fieldSets[' + this.form.length + ']' + '.fields[' + 0 + '].'});

		//give fieldset id and ordinal
		const fieldSet: DescriptionTemplateFieldSetEditorModel = new DescriptionTemplateFieldSetEditorModel(this.validationErrorModel);
		const fieldSetId = Guid.create().toString();
		fieldSet.id = fieldSetId;
		fieldSet.ordinal = afterOrdinal < 0 ? 0 : afterOrdinal;

		const parentArray = this.form.get('fieldSets') as UntypedFormArray;

		parentArray.controls.forEach(fieldset => {
			const ordinalControl = fieldset.get('ordinal');
			const ordinalValue = ordinalControl.value;
			if (ordinalValue > afterOrdinal) {
				ordinalControl.setValue(ordinalValue + 1);
			}
		});
		const fieldsetForm = fieldSet.buildForm({rootPath: this.validationRootPath + '.fieldSets[' + this.form.length + '].'});
		(fieldsetForm.get('fields') as UntypedFormArray).push(fieldForm);

		const index = afterOrdinal < 0 ? 0 : afterIndex + 1;
		parentArray.insert(index, fieldsetForm);
		this.dataNeedsRefresh.emit();

		setTimeout(() => {
			this.selectedFieldSetId = fieldSetId;
		}, 200);
	}

	enableReordering(): void {
		this.reorderingMode = true;
		this.scroller.next(this.selectedFieldSetId);
	}

	exitReordering(): void {
		this.reorderingMode = false;
		this.scroller.next(this.selectedFieldSetId);
	}

	private _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
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
}
