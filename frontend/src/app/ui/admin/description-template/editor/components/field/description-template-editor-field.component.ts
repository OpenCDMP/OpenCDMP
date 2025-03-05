import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroupDirective, NgForm, UntypedFormArray, UntypedFormControl, UntypedFormGroup, } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { ValidationType } from '@app/core/common/enum/validation-type';
import { DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateLabelData, DescriptionTemplateRadioBoxData, DescriptionTemplateSelectData, DescriptionTemplateSelectOption,DescriptionTemplateUploadData } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateFieldPersist } from '@app/core/model/description-template/description-template-persist';
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { DescriptionTemplateFieldEditorModel, DescriptionTemplateRuleEditorModel } from '../../description-template-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { takeUntil } from 'rxjs';

@Component({
    selector: 'app-description-template-editor-field-component',
    templateUrl: './description-template-editor-field.component.html',
    styleUrls: ['./description-template-editor-field.component.scss'],
    standalone: false
})
export class DescriptionTemplateEditorFieldComponent extends BaseComponent implements OnInit, ErrorStateMatcher {
	@Input() viewOnly: boolean;
	@Input() form: UntypedFormGroup;
	validationTypeEnum = ValidationType;

	fieldType: DescriptionTemplateFieldType;
	descriptionTemplateFieldTypeEnum = DescriptionTemplateFieldType;

	@Input() expandView: boolean = true;
	@Input() canBeDeleted: boolean = true;

	@Output() delete = new EventEmitter<void>();
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;
	@Input() fieldSetId: string;

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	constructor(
		public enumUtils: EnumUtils,
		public descriptionTemplateService: DescriptionTemplateService,
		private configurationService: ConfigurationService,
		public semanticsService: SemanticsService
	) {
		super();
	}


	isErrorState(control: UntypedFormControl, form: FormGroupDirective | NgForm): boolean {

		if (this.form.get('data').untouched) return false;

		return this.form.get('data').invalid;
	}

	ngOnInit() {

		const fieldType = this.form.get('data').get('fieldType').value;
		if (fieldType) {
			this.fieldType = fieldType;
			if (this.fieldType !== DescriptionTemplateFieldType.FREE_TEXT) {
				this.setValidator(ValidationType.URL, false);
			}
		}
	}

	private clearVisibilityRulesValue() {
		(this.form.get('visibilityRules') as UntypedFormArray).controls?.forEach(
			(visibilityRule, index) => {
				visibilityRule.get('textValue').setValue(null);
				visibilityRule.updateValueAndValidity();
			}	
		)							
	}
	

	addNewRule() {
		const rule: DescriptionTemplateRuleEditorModel = new DescriptionTemplateRuleEditorModel(this.validationErrorModel);
		const ruleArray = this.form.get('visibilityRules') as UntypedFormArray;
		(<UntypedFormArray>this.form.get('visibilityRules')).push(rule.buildForm({rootPath: this.validationRootPath + 'visibilityRules[' + ruleArray.length +'].'}));
	}

	get canApplyVisibility(): boolean {

		switch (this.fieldType) {
			case DescriptionTemplateFieldType.TEXT_AREA:
			case DescriptionTemplateFieldType.RICH_TEXT_AREA:
			case DescriptionTemplateFieldType.FREE_TEXT:
			case DescriptionTemplateFieldType.BOOLEAN_DECISION:
			case DescriptionTemplateFieldType.RADIO_BOX:
			case DescriptionTemplateFieldType.SELECT:
			case DescriptionTemplateFieldType.CHECK_BOX:
			case DescriptionTemplateFieldType.DATE_PICKER:
				return true;
		}
		return false;
	}

	onInputTypeChange() {

		const type = this.fieldType;

		const field: DescriptionTemplateFieldPersist = this.form.getRawValue();
		field.defaultValue = {
			booleanValue: null,
			dateValue: null,
			textValue: null,
		};
		if (!this.canApplyVisibility) {
			field.visibilityRules = [];
		}

		switch (type) {
			case DescriptionTemplateFieldType.REFERENCE_TYPES: {
				const data: DescriptionTemplateLabelData = {
					label: '',
					fieldType: type
				}
				field.data = data;
				field.defaultValue = null;
				break;
			}
			case DescriptionTemplateFieldType.RADIO_BOX: {
				const data: DescriptionTemplateRadioBoxData = {
					label: '',
					options: [],
					fieldType: type
				}
				field.data = data;
				break;
			}
			case DescriptionTemplateFieldType.SELECT: {
				const firstOption = { label: '', value: '' } as DescriptionTemplateSelectOption;
				const data: DescriptionTemplateSelectData = {
					label: '',
					multipleSelect: false,
					options: [firstOption],
					fieldType: type
				}
				field.data = data;
				break;
			}
			case DescriptionTemplateFieldType.BOOLEAN_DECISION:
			case DescriptionTemplateFieldType.CHECK_BOX:
			case DescriptionTemplateFieldType.FREE_TEXT:
			case DescriptionTemplateFieldType.TEXT_AREA:
			case DescriptionTemplateFieldType.RICH_TEXT_AREA:
			case DescriptionTemplateFieldType.DATE_PICKER:{
				const data: DescriptionTemplateLabelData = {
					label: '',
					fieldType: type
				}
				field.data = data;

				break;
			}
			case DescriptionTemplateFieldType.TAGS:
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
			case DescriptionTemplateFieldType.VALIDATION: {
				const data: DescriptionTemplateLabelData = {
					label: '',
					fieldType: type
				}
				field.data = data;
				field.defaultValue = null;

				break;
			}
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS:
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS:{
				const data: DescriptionTemplateLabelAndMultiplicityData = {
					label: '',
					multipleSelect: false,
					fieldType: type
				}
				field.data = data;
				field.defaultValue = null;
				break;
			}
			case DescriptionTemplateFieldType.UPLOAD: {
				const data: DescriptionTemplateUploadData = {
					label: '',
					types: [],
					maxFileSizeInMB: this.configurationService.maxFileSizeInMB,
					fieldType: type
				}
				field.data = data;
				field.defaultValue = null;
				break;
			}
		}

		const form = (new DescriptionTemplateFieldEditorModel(this.validationErrorModel)).fromModel(field)
			.buildForm({rootPath: this.validationRootPath});


		const fields = this.form.parent as UntypedFormArray;
		let index = -1;

		fields.controls.forEach((control, i) => {
			if (this.form.get('id').value === control.get('id').value) {
				index = i
			}
		});
		if (index >= 0) {
			fields.removeAt(index);
			fields.insert(index, form);
			this.form = form;


		}
		this.clearVisibilityRulesValue();
	}


	toggleRequired(event: MatSlideToggleChange) {
		this.setValidator(ValidationType.Required, event.checked);
	}

	toggleURL(event: MatSlideToggleChange) {
		this.setValidator(ValidationType.URL, event.checked);
	}

	private setValidator(validationType: ValidationType, add: boolean) {
		let validationsControl = this.form.get('validations') as UntypedFormControl;
		let validations: Array<ValidationType> = validationsControl.value;

		if (add) {
			if (!validations.includes(validationType)) {
				validations.push(validationType);
				validationsControl.updateValueAndValidity();
			}
		} else {
			validationsControl.setValue(validations.filter(validator => validator != validationType));
			validationsControl.updateValueAndValidity();
		}
	}

	get isRequired() {
		let validationsControl = this.form.get('validations') as UntypedFormControl;
		let validations: Array<ValidationType> = validationsControl.value;
		return validations.includes(ValidationType.Required);
	}

	get isURL() {
		let validationsControl = this.form.get('validations') as UntypedFormControl;
		let validations: Array<ValidationType> = validationsControl.value;
		return validations.includes(ValidationType.URL);
	}


	onDelete() {
		this.delete.emit();
	}


	isTextType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.FREE_TEXT ||
			type == DescriptionTemplateFieldType.TEXT_AREA || type == DescriptionTemplateFieldType.RICH_TEXT_AREA ||
			type == DescriptionTemplateFieldType.RADIO_BOX ;
	}

	isTextListType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.TAGS || type == DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS ||
			type == DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS || type == DescriptionTemplateFieldType.SELECT;
	}

	isDateType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.DATE_PICKER;
	}

	isBooleanType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.CHECK_BOX || type == DescriptionTemplateFieldType.BOOLEAN_DECISION;
	}

	isReferenceType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.REFERENCE_TYPES;
	}

	isExternalIdentifierType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.VALIDATION || type == DescriptionTemplateFieldType.DATASET_IDENTIFIER;
	}

	canSetDefaultValue(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.FREE_TEXT ||
			type == DescriptionTemplateFieldType.TEXT_AREA || type == DescriptionTemplateFieldType.RICH_TEXT_AREA ||
			type == DescriptionTemplateFieldType.RADIO_BOX || type == DescriptionTemplateFieldType.SELECT || type == DescriptionTemplateFieldType.DATE_PICKER ||
			type == DescriptionTemplateFieldType.CHECK_BOX || type == DescriptionTemplateFieldType.BOOLEAN_DECISION;
	}
}
