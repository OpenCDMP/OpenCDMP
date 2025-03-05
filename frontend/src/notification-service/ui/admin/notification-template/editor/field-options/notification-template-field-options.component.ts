import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormArray } from '@angular/forms';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { NotificationFieldInfoEditorModel, NotificationFieldOptionsEditorModel } from '../notification-template-editor.model';
import { MatChipEditedEvent, MatChipInputEvent } from '@angular/material/chips';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { NotificationDataType } from '@notification-service/core/enum/notification-data-type';

@Component({
    selector: 'app-notification-template-field-options-component',
    templateUrl: 'notification-template-field-options.component.html',
    styleUrls: ['./notification-template-field-options.component.scss'],
    standalone: false
})
export class NotificationTemplateFieldOptionsComponent extends BaseComponent implements OnInit {

	@Input() form;
	@Input() validationErrorModel: ValidationErrorModel = null;
	@Input() validationRootPath: string = null;
	@Input() mandatoryFields: string[] = [];
	@Input() formatting: { [key: string]: string } = {};

	public notificationDataTypeEnum = this.enumUtils.getEnumValues(NotificationDataType);
	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	constructor(
		public enumUtils: NotificationServiceEnumUtils,
	) { super(); }

	ngOnInit() {
	}

	addChipListValues(event: MatChipInputEvent){
		this.mandatoryFields = this.add(event, this.mandatoryFields);
	}

	editChipListValues(event: MatChipEditedEvent, field: string){
		this.mandatoryFields = this.edit(field, this.mandatoryFields, event);
		this.editFormmattingKey(field, event.value.trim());
	}

	removeChipListValues(field: string){		
		this.mandatoryFields = this.remove(field, this.mandatoryFields);
		this.deleteFormattingItem(field);
	}

	add(event: MatChipInputEvent, values: string[]) {
		const value = (event.value || '').trim();

		if (value) values.push(value)
		event.chipInput!.clear();
		return values;
		
	}
	
	remove(field: string, values: string[]) {
		const index = values.indexOf(field);
	
		if (index >= 0) values.splice(index, 1);
		return values;
	}
	
	edit(field: string, values: string[], event: MatChipEditedEvent) {
		const value = event.value.trim();
	
		if (!value) {
		  values = this.remove(field, values);
		  return values;
		}
	
		const index = values.indexOf(field);
		if (index >= 0) values[index] = value

		return values;
	}

	//options fields

	addOptionalItem() {
		const formArray = (this.form.get('optional') as UntypedFormArray);
		const optional: NotificationFieldInfoEditorModel = new NotificationFieldInfoEditorModel(this.validationErrorModel);

		formArray.push(optional.buildForm({ rootPath: this.validationRootPath + 'optional[' + formArray.length + '].' }));
	}

	removeSubjectOptionalItem(optionalIndex: number): void {
		const key = (this.form.get('optional') as UntypedFormArray).at(optionalIndex).get("key").value;
		this.deleteFormattingItem(key);
	
		(this.form.get('optional') as UntypedFormArray).removeAt(optionalIndex);

		//Reapply validators
		NotificationFieldOptionsEditorModel.reapplyValidators(
			{
				formGroup: this.form,
				validationErrorModel: this.validationErrorModel,
				rootPath: this.validationRootPath
			}
		);
		this.form.get('optional').markAsDirty();
	}

	// subject formatting
	insertFormattingItem(key: string, value: string){
		this.formatting[key] = value;
		this.form.get('formatting').setValue(this.formatting);
	}

	formattingValueChange(event: Event, key: string){
		this.formatting[key] = (event.target as HTMLInputElement).value;
		this.form.get('formatting').setValue(this.formatting);
	}

	editFormmattingKey(oldKey: string, newKey: string){
		this.insertFormattingItem(newKey, this.formatting[oldKey]);
		this.deleteFormattingItem(oldKey);
	}

	deleteFormattingItem(key:string){
		if (key in this.formatting) {
			delete this.formatting[key];
			this.form.get('formatting').setValue(this.formatting);
		}
	}

}
