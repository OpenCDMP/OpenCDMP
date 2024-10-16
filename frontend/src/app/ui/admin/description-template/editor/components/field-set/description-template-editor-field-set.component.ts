import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { ValidationType } from '@app/core/common/enum/validation-type';
import { DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateLabelData, DescriptionTemplateRadioBoxData, DescriptionTemplateReferenceTypeData, DescriptionTemplateSelectData, DescriptionTemplateSelectOption, DescriptionTemplateUploadData } from '@app/core/model/description-template/description-template';
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { TransitionGroupComponent } from "@app/ui/transition-group/transition-group.component";
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime, delay, map, takeUntil, tap } from 'rxjs/operators';
import { GENERAL_ANIMATIONS } from '../../animations/animations';
import { DescriptionTemplateFieldEditorModel, DescriptionTemplateRuleEditorModel, DescriptionTemplateSectionEditorModel } from '../../description-template-editor.model';
import { DescriptionTemplateFieldSetPersist, DescriptionTemplateReferenceTypeFieldPersist } from '@app/core/model/description-template/description-template-persist';
import { DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { Description } from '@app/core/model/description/description';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';

@Component({
	selector: 'app-description-template-editor-field-set-component',
	templateUrl: './description-template-editor-field-set.component.html',
	styleUrls: ['./description-template-editor-field-set.component.scss'],
	animations: [GENERAL_ANIMATIONS]
})
export class DescriptionTemplateEditorFieldSetComponent extends BaseComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	@Input() viewOnly: boolean;

	@Input() descriptionTemplateId?: string;

	@Input() numbering: string;
	@Input() hasFocus: boolean = false;
	@Input() reorderingMode: boolean = false;
	@ViewChild("inputs") inputs: TransitionGroupComponent;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;

	showPreview: boolean = true;
	previewDirty: boolean = false;


	showDescription: boolean = true;
	showAdditionalInfo: boolean = false;
	showExtendedDescription: boolean = false;

	//Preview
	previewFieldSet: DescriptionTemplateFieldSet = null;
	previewPropertiesFormGroup: UntypedFormGroup = null;
	descriptionTemplateFieldTypeEnum = DescriptionTemplateFieldType;
	@Input() availableReferenceTypes: ReferenceType[] = [];

	constructor(
		private dialog: MatDialog,
		private language: TranslateService,
		public enumUtils: EnumUtils,
		public descriptionTemplateService: DescriptionTemplateService,
		private configurationService: ConfigurationService,
		public visibilityRulesService: VisibilityRulesService,
	) {
		super();
	}

	get firstField() {
		try {
			return (this.form.get('fields') as UntypedFormArray).at(0);
		} catch {
			return null;
		}
	}

	ngOnInit() {
		if (this.viewOnly) {
			this.form.get('hasCommentField').disable();
		}

		this.showExtendedDescription = !!this.form.get('extendedDescription').value;
		this.showAdditionalInfo = !!this.form.get('additionalInformation').value;

		this.form.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(changes => {
			this.previewDirty = true;
			this.generatePreviewForm();

		});
		this.previewSubject$
			.pipe(debounceTime(600))
			.pipe(
				takeUntil(this._destroyed),
				map(model => model.buildForm()),
				map(updatedForm => {
					const previewContainer = document.getElementById('preview_container' + this.form.get('id').value);
					if (previewContainer) {
						const clientHeight = previewContainer.clientHeight;
						if (clientHeight) {
							previewContainer.style.height = clientHeight.toString() + 'px';
						}
					}
					this.showPreview = false;
					this.previewDirty = true;
					return previewContainer;
				}),
				delay(100),
				tap(previewContainer => {
					this.showPreview = true;
					this.previewDirty = false;
				}),
				delay(100)
			)
			.subscribe(previewContainer => {

				if (previewContainer) {
					previewContainer.style.height = 'auto';
				}
			});


		this.generatePreviewForm();
	}


	get updatedClass() {
		if (this.previewDirty) return '';
		else return 'updated';
	}

	previewSubject$: Subject<DescriptionTemplateSectionEditorModel> = new Subject<DescriptionTemplateSectionEditorModel>();

	private generatePreviewForm() {
		const formValue: DescriptionTemplateFieldSetPersist = this.form.getRawValue();

		const fields: DescriptionTemplateField[] = formValue.fields.map(editorField => {
			const convertedField = {
				id: editorField.id,
				ordinal: editorField.ordinal,
				numbering: '',
				semantics: editorField.semantics,
				defaultValue: editorField.defaultValue,
				visibilityRules: editorField.visibilityRules,
				validations: editorField.validations,
				includeInExport: editorField.includeInExport,
				data: editorField.data
			} as DescriptionTemplateField;

			if (editorField.data.fieldType === DescriptionTemplateFieldType.REFERENCE_TYPES) {
				convertedField.data = editorField.data;
				let selectedReferenceType: ReferenceType = this.availableReferenceTypes.find(referenceType => referenceType.id == (editorField.data as DescriptionTemplateReferenceTypeFieldPersist).referenceTypeId);
				(convertedField.data as DescriptionTemplateReferenceTypeData).referenceType = {
						id: (editorField.data as DescriptionTemplateReferenceTypeFieldPersist).referenceTypeId,
						name: selectedReferenceType?.name
					};
			} else {
				convertedField.data = editorField.data;
			}
			return convertedField;
		});

		const fieldSet: DescriptionTemplateFieldSet = {
			id: formValue.id,
			ordinal: formValue.ordinal,
			title: formValue.title,
			description: formValue.description,
			extendedDescription: formValue.extendedDescription,
			additionalInformation: formValue.additionalInformation,
			hasMultiplicity: formValue.hasMultiplicity,
			multiplicity: {
				max: formValue.multiplicity?.max,
				min: formValue.multiplicity?.min,
				placeholder: formValue.multiplicity?.placeholder, tableView: formValue.multiplicity?.tableView
			},
			hasCommentField: formValue.hasCommentField,
			fields: fields
		}

		const mockDescription: Description = {
			descriptionTemplate: {
				definition: {
					pages: [
						{
							sections: [{
								fieldSets: [fieldSet]
							}]
						}
					]
				}
			}
		}

		const descriptionEditorModel = new DescriptionEditorModel().fromModel(mockDescription, mockDescription.descriptionTemplate);
		const previewProperties = descriptionEditorModel.properties.buildForm({visibilityRulesService: this.visibilityRulesService}) as UntypedFormGroup;
		this.previewPropertiesFormGroup = previewProperties.get("fieldSets").get(fieldSet.id) as UntypedFormGroup;
		this.previewFieldSet = fieldSet;

		this.visibilityRulesService.setContext(mockDescription.descriptionTemplate.definition, previewProperties);
	}

	onIsMultiplicityEnabledChange(isMultiplicityEnabled: MatCheckboxChange) {
		const multiplicity = this.form.get('multiplicity') as UntypedFormGroup;

		const minControl = multiplicity.get('min');
		const maxControl = multiplicity.get('max');
		const placeholder = multiplicity.get('placeholder');
		const tableView = multiplicity.get('tableView');

		if (isMultiplicityEnabled.checked) {
			minControl.setValue(0);
			maxControl.setValue(1);
			placeholder.setValue('');
			tableView.setValue(false);
		} else {
			minControl.setValue(null);
			maxControl.setValue(null);
			placeholder.setValue(null);
			tableView.setValue(null);
		}

		minControl.updateValueAndValidity();
		maxControl.updateValueAndValidity();

	}

	DeleteField(index) {

		const fieldsForm = <UntypedFormArray>this.form.get('fields');
		fieldsForm.removeAt(index);
		this.inputs.init();
		// calculate ordinals
		fieldsForm.controls.forEach((field, idx) => {
			field.get('ordinal').setValue(idx);
			field.updateValueAndValidity();
		});
		this.form.markAsDirty();//deactivate guard
	}

	getFieldTile(formGroup: UntypedFormGroup, index: number) {
		if (formGroup.get('title') && formGroup.get('title').value && formGroup.get('title').value.length > 0) { return formGroup.get('title').value; }
		return "Field " + (index + 1);
	}


	targetField: UntypedFormGroup;
	validationTypeEnum = ValidationType;


	addVisibilityRule(targetField: UntypedFormGroup) {
		const rule: DescriptionTemplateRuleEditorModel = new DescriptionTemplateRuleEditorModel();
		(<UntypedFormArray>targetField.get('visible').get('rules')).push(rule.buildForm());
	}
	toggleRequired(targetField: UntypedFormGroup, event: MatCheckboxChange) {

		let validationsControl = targetField.get('validations') as UntypedFormControl;
		let validations: Array<ValidationType> = validationsControl.value;

		if (event.checked) {
			if (!validations.includes(ValidationType.Required)) {//IS ALREADY REQUIRED
				validations.push(ValidationType.Required);
				validationsControl.updateValueAndValidity();
			}
		} else {
			validationsControl.setValue(validations.filter(validator => validator != ValidationType.Required));
			validationsControl.updateValueAndValidity();
		}
	}
	setTargetField(field: AbstractControl) {
		this.targetField = <UntypedFormGroup>field;
	}


	deleteTargetField() {

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().subscribe(result => {
			if (result) {
				this._deleteTargetField();
			}
		});

	}


	private _deleteTargetField() {
		if (!this.targetField) return;

		let index = -1;

		const fields = this.form.get('fields') as UntypedFormArray;

		for (let i = 0; i < fields.length; i++) {
			let field = fields.at(i);
			if (field.get('id').value === this.targetField.get('id').value) {//index found
				index = i;
				break;
			}
		}

		if (index >= 0) {//target found in fields
			this.DeleteField(index);
			this.targetField = null;
		}

	}

	deleteField(index: number) {

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().subscribe(result => {
			if (result) {
				this.DeleteField(index);
			}
		});

	}

	addNewInput(type: DescriptionTemplateFieldType) {

		const fieldsArray = this.form.get('fields') as UntypedFormArray;

		let targetOrdinal = fieldsArray.length;
		try {
			targetOrdinal = fieldsArray.controls.map(control => control.get('ordinal').value).reduce((a, b) => Math.max(a, b)) + 1;
		} catch {

		}

		const field = {
			id: Guid.create().toString(),
			ordinal: targetOrdinal,
			validations: [],
			includeInExport: true
		} as DescriptionTemplateField;

		switch (type) {
			case DescriptionTemplateFieldType.REFERENCE_TYPES: {
				const data: DescriptionTemplateReferenceTypeData = {
					label: '',
					multipleSelect: false,
					fieldType: type
				}
				field.data = data;
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
			case DescriptionTemplateFieldType.DATE_PICKER:
			case DescriptionTemplateFieldType.TAGS:
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
			case DescriptionTemplateFieldType.VALIDATION: {
				const data: DescriptionTemplateLabelData = {
					label: '',
					fieldType: type
				}
				field.data = data;

				break;
			}
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS:
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS: {
				const data: DescriptionTemplateLabelAndMultiplicityData = {
					label: '',
					multipleSelect: false,
					fieldType: type
				}
				field.data = data;
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
				break;
			}
		}

		(<UntypedFormArray>this.form.get('fields')).push(new DescriptionTemplateFieldEditorModel(this.validationErrorModel).fromModel(field).buildForm({ rootPath: this.validationRootPath + '.fields[' + this.fieldsArray.length + ']' }));
		this.inputs.init();
	}

	calculateLabelWidth(numbering: string) {

		const width = numbering.split('.').reduce((acc, item) => item + acc, '').length;


		return { 'width': width + 'em' }
	}

	get fieldsArray(): UntypedFormArray {
		if (this.form && this.form.get('fields')) {
			return this.form.get('fields') as UntypedFormArray;
		}
		return null;
	}

	move(index, direction: "up" | "down" = "up") {
		this.inputs.init();
		if (direction === "up" && this.canGoUp(index)) {
			let temp = this.fieldsArray.at(index);
			this.fieldsArray.removeAt(index);
			this.fieldsArray.insert(index - 1, temp);
		} else if (direction === "down" && this.canGoDown(index)) {
			let temp = this.fieldsArray.at(index + 1);
			this.fieldsArray.removeAt(index + 1);
			this.fieldsArray.insert(index, temp);
		}
		this.fieldsArray.controls.forEach((field, index) => {
			field.get('ordinal').setValue(index);
		});
	}

	canGoUp(index: number): boolean {
		return index > 0 && !this.viewOnly;
	}

	canGoDown(index: number): boolean {
		return index < (this.fieldsArray.length - 1) && !this.viewOnly;
	}
}


