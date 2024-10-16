import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormArray, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { DescriptionTemplateRule } from '@app/core/model/description-template/description-template';
import { TranslateService } from '@ngx-translate/core';
import { ToCEntryType } from '../../table-of-contents/description-template-table-of-contents-entry';
import { DescriptionTemplateRuleEditorModel } from '../../description-template-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Component({
	selector: 'app-description-template-editor-visibility-rule-component',
	templateUrl: './description-template-editor-visibility-rule.component.html',
	styleUrls: ['./description-template-editor-visibility-rule.component.scss']
})

export class DescriptionTemplateEditorRuleComponent implements OnInit {


	@Input() form: UntypedFormArray;

	@Input() fieldTypeForCheck: DescriptionTemplateFieldType;
	@Input() formArrayOptionsForCheck: UntypedFormArray;
	@Input() viewOnly: boolean;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;
	@Input() fieldId: string = null;
	@Input() fieldSetId: string = null;


	options: OptionItem[];
	sectionOptions: OptionItem[];
	fieldSetOptions: OptionItem[];
	fieldOptions: OptionItem[];

	parentIds: string[] = [];
	hiddenBy: string[] = [];

	rootForm: AbstractControl = null;

	constructor(private language: TranslateService) {

	}

	isTextType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.FREE_TEXT ||
			type == DescriptionTemplateFieldType.TEXT_AREA || type == DescriptionTemplateFieldType.RICH_TEXT_AREA ||
			type == DescriptionTemplateFieldType.RADIO_BOX ;
	}

	isTextListType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS ||
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

	isTagType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.TAGS;
	}

	isExternalIdentifierType(type: DescriptionTemplateFieldType){
		return type == DescriptionTemplateFieldType.VALIDATION || type == DescriptionTemplateFieldType.DATASET_IDENTIFIER;;
	}

	deleteRule(index) {
		this.form.removeAt(index);
		this.form.controls?.forEach(
			(control, index) => DescriptionTemplateRuleEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${this.validationRootPath}visibilityRules[${index}].`,
				validationErrorModel: this.validationErrorModel
			})
		);
		this.form.markAsDirty();//deactivate guard
	}

	ngOnInit(): void {
		this.form.controls?.forEach(
			(control, index) => {
				if (control.get('target').value == this.fieldId || control.get('target').value == this.fieldSetId) control.get('target').setValue(null);
			}
		);
		this.rootForm = this.findRootForm();
		this._computeOptions();
	}

	private _computeOptions() {
		this.options = this.getOptions();

		this.sectionOptions = [];
		this.fieldOptions = [];
		this.fieldSetOptions = [];
		this.options.forEach(option => {
			switch (option.type) {
				case ToCEntryType.Field:
					this.fieldOptions.push(option);
					break;
				case ToCEntryType.FieldSet:
					this.fieldSetOptions.push(option);
					break;
				case ToCEntryType.Section:
					this.sectionOptions.push(option);
					break;
				default:
					break;
			}
		});
		//remove options to hide if given fieldset is already hidden by option

		this.fieldOptions.forEach(e => this._buildHiddenBy(e));
		this.fieldSetOptions.forEach(e => this._buildHiddenBy(e));
		this.parentIds = this.computeParentIds();
		this.hiddenBy = this.computeHiddenBy();
	}



	computeOptions(isOpened: boolean) {
		if (isOpened) {
			this._computeOptions();
		}
	}

	private _buildHiddenBy(fo: OptionItem) {
		try {
			this.fieldOptions.forEach(foption => {
				const rules = (foption.form.get('visibilityRules') as UntypedFormArray).controls.map(c => (c as UntypedFormGroup).getRawValue()) as DescriptionTemplateRule[]
				const targets = rules.map(rule => rule.target);
				targets.forEach(target => {
					if (fo.parentsIds.includes(target) && !fo.hiddenBy.includes(foption.id)) {
						fo.hiddenBy.push(...foption.parentsIds);
					}
				})

			});
		} catch {
			console.log('error');
		}
	}



	findRootForm() {
		let currentForm: AbstractControl = this.form;
		while (currentForm.parent != null){
			currentForm = currentForm.parent;
		}
		return currentForm;
	}


	getOptions(): OptionItem[] {

		if (this.rootForm) {

			const result: OptionItem[] = [];

			(this.rootForm.get('definition').get('pages') as UntypedFormArray).controls.forEach(pageForm => {

				const sections = pageForm.get('sections') as UntypedFormArray;
				if (sections) {
					sections.controls.forEach(section => {
						const subResult = this.buildOptions(section as UntypedFormGroup, ToCEntryType.Section, []);
						result.push(...subResult);
					});
				}
			});

			return result;
		}
		//nothing found
		return [];
	}

	private buildOptions(form: UntypedFormGroup, type: ToCEntryType, parentIds: string[]): OptionItem[] {

		const sections = form.get('sections') as UntypedFormArray;
		const fieldSets = form.get('fieldSets') as UntypedFormArray;
		const fields = form.get('fields') as UntypedFormArray;

		const result: OptionItem[] = [];

		const currentOptionItem: OptionItem = {
			id: form.get('id').value,
			type: type,
			label: type === ToCEntryType.Field ? form.get('data').get('label').value : form.get('title').value,
			parentsIds: [...parentIds, form.get('id').value],
			form: form,
			hiddenBy: []
		}
		result.push(currentOptionItem);

		if (sections) {
			sections.controls.forEach(section => {
				result.push(...this.buildOptions(section as UntypedFormGroup, ToCEntryType.Section, currentOptionItem.parentsIds));
			});
		}
		if (fieldSets) {
			fieldSets.controls.forEach(fieldset => {
				result.push(...this.buildOptions(fieldset as UntypedFormGroup, ToCEntryType.FieldSet, currentOptionItem.parentsIds));
			});
		}
		if (fields) {
			fields.controls.forEach(field => {
				result.push(...this.buildOptions(field as UntypedFormGroup, ToCEntryType.Field, currentOptionItem.parentsIds)); //TODO NA TO DOUME
			});
		}

		return result;
	}

	computeParentIds(): string[] {
		if (!this.rootForm.get('id')) return [];

		const current = this.options.find(opt => opt.id === this.rootForm.get('id').value);
		if (current) {
			return current.parentsIds;
		}
		return [];
	}

	computeHiddenBy(): string[] {
		if (!this.rootForm.get('id')) return [];

		const current = this.options.find(opt => opt.id === this.rootForm.get('id').value);
		if (current) {
			return current.hiddenBy;
		}
		return [];
	}

	getToolTipMessage(id: string) {
		if (this.parentIds.includes(id)) {
			// return 'Cannot hide element that contain the field';
			return this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.RULE.HINTS.ELEMENT-CHILD-OF-TARGET');
		} else if (this.hiddenBy.includes(id)) {
			return this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.RULE.HINTS.ELEMENT-HIDDEN-FROM-ELEMENT');
		}
		return '';
	}

}


interface OptionItem {
	id: string,
	label: string,
	type: ToCEntryType,
	parentsIds: string[],
	form: UntypedFormGroup,
	hiddenBy: string[]
}
