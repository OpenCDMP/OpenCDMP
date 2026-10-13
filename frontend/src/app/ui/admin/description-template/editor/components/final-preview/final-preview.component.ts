import { Component, Input, OnInit, SimpleChanges, OnChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { DescriptionTemplate, DescriptionTemplateDefinition, BaseField, BaseFieldSet, FieldReferenceTypeData, BaseSection } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateDefinitionPersist, BaseFieldPersist, FieldSetPersist, DescriptionTemplatePersist, FieldReferenceTypePersist, SectionPersist } from '@app/core/model/description-template/description-template-persist';
import { Description } from '@app/core/model/description/description';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { DescriptionEditorForm, DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { DescriptionFormComponent } from '../../../../../description/editor/description-form/description-form.component';


@Component({
    selector: 'app-final-preview-component',
    templateUrl: './final-preview.component.html',
    providers: [VisibilityRulesService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [DescriptionFormComponent, FormsModule, ReactiveFormsModule]
})

export class FinalPreviewComponent implements OnInit, OnChanges {


	@Input() descriptionTemplatePersist: DescriptionTemplatePersist;
	@Input() availableReferenceTypes: ReferenceType[] = [];

	descriptionTemplate: DescriptionTemplate;

	previewPropertiesFormGroup: UntypedFormGroup;
    formGroup: FormGroup<DescriptionEditorForm>;

	constructor(public visibilityRulesService: VisibilityRulesService) {

	}

	ngOnInit(): void {
		this.generatePreviewForm();
	}

	ngOnChanges(changes: SimpleChanges) {
		if(changes['descriptionTemplatePersist']) this.generatePreviewForm();
	}

	private generatePreviewForm() {

		if(this.descriptionTemplatePersist){
			this.descriptionTemplate = this.buildDescriptionTemplate(this.descriptionTemplatePersist);

			const mockDescription: Description = {
				descriptionTemplate: this.descriptionTemplate
			}
			const descriptionEditorModel = new DescriptionEditorModel().fromModel(mockDescription, mockDescription.descriptionTemplate);
            this.formGroup = descriptionEditorModel.buildForm(null, false, this.visibilityRulesService);
			this.previewPropertiesFormGroup = descriptionEditorModel.properties.buildForm({visibilityRulesService: this.visibilityRulesService}) as UntypedFormGroup;

			this.visibilityRulesService.setContext(this.descriptionTemplate.definition, this.previewPropertiesFormGroup);
		}

	}

	private buildDescriptionTemplate(persist: DescriptionTemplatePersist) : DescriptionTemplate{
		if (persist == null) return null;
		return {
			id: persist.id,
			label: persist.label,
			description: persist.description,
			groupId: undefined,
			version: undefined,
			language: persist.language,
			type: undefined,
			status: persist.status,
			definition: this.buildDescriptionTemplateDefinition(persist.definition),
			users: persist.users
		}
	}

	private buildDescriptionTemplateDefinition(persist: DescriptionTemplateDefinitionPersist) : DescriptionTemplateDefinition{
		if (persist == null) return null;
		return {
			sections: persist.sections.map(x => this.buildDescriptionTemplateSection(x)),
		}
	}

	// private buildDescriptionTemplatePage(persist: DescriptionTemplatePagePersist) : DescriptionTemplatePage{
	// 	if (persist == null) return null;
	// 	return {
	// 		id: persist.id,
	// 		ordinal: persist.ordinal,
	// 		title: persist.title,
	// 		sections: persist.sections.map(x => this.buildDescriptionTemplateSection(x)),
	// 	}
	// }

	private buildDescriptionTemplateSection(persist: SectionPersist) : BaseSection{
		if (persist == null) return null;
		return {
			id: persist.id,
			ordinal: persist.ordinal,
			title: persist.title,
			description: persist.description,
			sections: persist.sections.map(x => this.buildDescriptionTemplateSection(x)),
			fieldSets: persist.fieldSets.map(x => this.buildDescriptionTemplateFieldSet(x)),
		}
	}

	private buildDescriptionTemplateFieldSet(persist: FieldSetPersist) : BaseFieldSet{
		if (persist == null) return null;
		return {
			id: persist.id,
			ordinal: persist.ordinal,
			title: persist.title,
			description: persist.description,
			extendedDescription: persist.extendedDescription,
			additionalInformation: persist.additionalInformation,
			hasMultiplicity: persist.hasMultiplicity,
			multiplicity: {
				max: persist.multiplicity?.max,
				min: persist.multiplicity?.min,
				placeholder: persist.multiplicity?.placeholder,
				tableView: persist.multiplicity?.tableView,
				transposedTableView: persist.multiplicity?.transposedTableView,
			},
			hasCommentField: persist.hasCommentField,
			fields: persist.fields.map(x => this.buildDescriptionTemplateField(x)),
		}
	}

	private buildDescriptionTemplateField (persist: BaseFieldPersist) : BaseField{
		if (persist == null) return null;
		const convertedField: BaseField ={
			id: persist.id,
			ordinal: persist.ordinal,
			semantics: persist.semantics,
			defaultValue: persist.defaultValue,
			visibilityRules: persist.visibilityRules,
			validations: persist.validations,
			includeInExport: persist.includeInExport,
			data: persist.data,
		}

		if (persist.data.fieldType === FieldType.REFERENCE_TYPES) {
			convertedField.data = persist.data;
			const selectedReferenceType = this.availableReferenceTypes.find(referenceType => referenceType.id == (persist.data as FieldReferenceTypePersist).referenceTypeId);
			(convertedField.data as FieldReferenceTypeData).referenceType = {
					id: (persist.data as FieldReferenceTypePersist).referenceTypeId,
					name: selectedReferenceType?.name,
				};
		} else {
			convertedField.data = persist.data;
		}
		return convertedField;
	}

}
