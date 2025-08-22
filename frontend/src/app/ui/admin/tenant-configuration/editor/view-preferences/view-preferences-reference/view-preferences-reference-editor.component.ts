import { moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, computed, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, UntypedFormGroup } from '@angular/forms';
import { FormService } from '@common/forms/form-service';
import { TranslateService } from '@ngx-translate/core';
import { map } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { ViewPreference } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { BaseComponent } from '@common/base/base.component';
import { ViewPreferenceEditorModel } from '../view-preferences-editor.model';
import { Guid } from '@common/types/guid';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Component({
    selector: 'app-view-preferences-reference-editor',
    templateUrl: './view-preferences-reference-editor.component.html',
    styleUrls: ['./view-preferences-reference-editor.component.scss'],
    standalone: false
})
export class ViewPreferencesReferenceEditorComponent extends BaseComponent implements OnInit {
	@ViewChild('referenceSelect') referenceSelect: SingleAutoCompleteComponent;

	@Input() orderedReferenceTypesList: ReferenceType[] = [];
	@Input() form: any;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() rootBasePath: string;
	@Input() title: any;
	@Output() orderedReferenceTypesEmitter: EventEmitter<ReferenceType[]> = new EventEmitter<ReferenceType[]>();
 
    get autoCompleteConfig(): SingleAutoCompleteConfiguration {
        return {
            initialItems: (data?: any) => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup(
				null, this.form?.value?.map((x) => x.referenceTypeId)
            )).pipe(map(x => x.items)),
            filterFn: (searchQuery: string, data?: any) => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup(
                searchQuery,
                this.form.value?.map((x) => x.referenceTypeId)
            )).pipe(map(x => x.items)),
            getSelectedItem: (selectedItem: any) => null,
            displayFn: (item: ReferenceType) => null,
            titleFn: (item: ReferenceType) => item.name,
            valueAssign: (item: ReferenceType) => null,
        };
    }
	singleAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
    
    get reorderMode(){
        return this.dragAndDropService.reorderMode;
    }
	constructor(
		protected dialog: MatDialog,
		public language: TranslateService,
		protected formService: FormService,
		private referenceTypeService: ReferenceTypeService,
        private dragAndDropService: DragAndDropAccessibilityService
	) {
		super();
        this.singleAutoCompleteConfiguration = this.autoCompleteConfig;
	}


	ngOnInit(): void {
		
	}

	addReference(event: ReferenceType) {
		if (event?.id) {
			const formArray = this.form as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (event?.id == formArray.at(j).get('referenceTypeId').getRawValue()) {
					return;
				}
			}
			
			formArray.push(this.createPreference(formArray.length, event?.id));
			this.orderedReferenceTypesList.push(event);
			this.orderedReferenceTypesEmitter.emit(this.orderedReferenceTypesList);
            this.referenceSelect.clearValue(false);
		}
	}

	createPreference(index: number, referenceTypeId: Guid): UntypedFormGroup {
			const planPreference: ViewPreferenceEditorModel = new ViewPreferenceEditorModel(this.validationErrorModel);
			planPreference.referenceTypeId = referenceTypeId;
			planPreference.ordinal = index;
			return planPreference.buildForm({ rootPath: this.rootBasePath + '[' + index + '].' });
		}

	removeReference(item: ReferenceType) {
		if (item?.id) {
			const orderedListIndex = this.orderedReferenceTypesList.indexOf(item, 0);
			if (orderedListIndex > -1) {
				this.orderedReferenceTypesList.splice(orderedListIndex, 1);
				this.orderedReferenceTypesEmitter.emit(this.orderedReferenceTypesList);
			}
	
			const formArray = this.form as FormArray;

			for (let j = 0; j < formArray.length; j++) {
				if (item?.id == formArray.at(j).get('referenceTypeId').getRawValue()) {
					formArray.removeAt(j);
				}
			}
            this.referenceSelect.clearValue(false);
		}
	}

	droppedReferences(event: {previousIndex: number, currentIndex: number}) {
		const formArray = (this.form as FormArray);

		moveItemInArray(formArray.controls, event.previousIndex, event.currentIndex);
		formArray.updateValueAndValidity();
		formArray.controls.forEach((ViewPreference, index) => {
			ViewPreference.get('ordinal').setValue(index);
		});

		moveItemInArray(this.orderedReferenceTypesList, event.previousIndex, event.currentIndex);
		this.orderedReferenceTypesEmitter.emit(this.orderedReferenceTypesList);
	}

    onReferenceKeyDown($event: KeyboardEvent, index: number, blueprint: ViewPreference) {
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            listLength: this.orderedReferenceTypesList.length,
            itemName: blueprint.referenceType.name,
            moveDownFn: () => {
                this.droppedReferences({previousIndex: index, currentIndex: index + 1});
                setTimeout(() => document.getElementById(blueprint.referenceType?.id.toString())?.focus());
            },
            moveUpFn: () => {
                this.droppedReferences({previousIndex: index, currentIndex: index - 1});
                setTimeout(() => document.getElementById(blueprint.referenceType?.id.toString())?.focus());
            }
        })
    }
}
