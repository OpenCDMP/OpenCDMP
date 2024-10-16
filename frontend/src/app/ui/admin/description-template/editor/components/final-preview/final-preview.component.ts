import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { DescriptionTemplate, DescriptionTemplateBaseFieldData, DescriptionTemplateDefinition, DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateMultiplicity, DescriptionTemplatePage, DescriptionTemplateReferenceTypeData, DescriptionTemplateRule, DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateBaseFieldDataPersist, DescriptionTemplateDefinitionPersist, DescriptionTemplateFieldPersist, DescriptionTemplateFieldSetPersist, DescriptionTemplateMultiplicityPersist, DescriptionTemplatePagePersist, DescriptionTemplatePersist, DescriptionTemplateReferenceTypeFieldPersist, DescriptionTemplateRulePersist, DescriptionTemplateSectionPersist } from '@app/core/model/description-template/description-template-persist';
import { Description } from '@app/core/model/description/description';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';


@Component({
	selector: 'app-final-preview-component',
	templateUrl: './final-preview.component.html',
	styleUrls: ['./final-preview.component.scss'],
	providers: [VisibilityRulesService]
})

export class FinalPreviewComponent implements OnInit {


	@Input() descriptionTemplatePersist: DescriptionTemplatePersist;
	@Input() availableReferenceTypes: ReferenceType[] = [];

	descriptionTemplate: DescriptionTemplate;

	previewPropertiesFormGroup: UntypedFormGroup;

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
			pages: persist.pages.map(x => this.buildDescriptionTemplatePage(x))
		}
	}

	private buildDescriptionTemplatePage(persist: DescriptionTemplatePagePersist) : DescriptionTemplatePage{
		if (persist == null) return null;
		return {
			id: persist.id,
			ordinal: persist.ordinal,
			title: persist.title,
			sections: persist.sections.map(x => this.buildDescriptionTemplateSection(x)),
		}
	}

	private buildDescriptionTemplateSection(persist: DescriptionTemplateSectionPersist) : DescriptionTemplateSection{
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

	private buildDescriptionTemplateFieldSet(persist: DescriptionTemplateFieldSetPersist) : DescriptionTemplateFieldSet{
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
				tableView: persist.multiplicity?.tableView
			},
			hasCommentField: persist.hasCommentField,
			fields: persist.fields.map(x => this.buildDescriptionTemplateField(x)),
		}
	}

	private buildDescriptionTemplateField (persist: DescriptionTemplateFieldPersist) : DescriptionTemplateField{
		if (persist == null) return null;
		let convertedField: DescriptionTemplateField ={
			id: persist.id,
			ordinal: persist.ordinal,
			semantics: persist.semantics,
			defaultValue: persist.defaultValue,
			visibilityRules: persist.visibilityRules,
			validations: persist.validations,
			includeInExport: persist.includeInExport,
			data: persist.data,
		}

		if (persist.data.fieldType === DescriptionTemplateFieldType.REFERENCE_TYPES) {
			convertedField.data = persist.data;
			let selectedReferenceType = this.availableReferenceTypes.find(referenceType => referenceType.id == (persist.data as DescriptionTemplateReferenceTypeFieldPersist).referenceTypeId);
			(convertedField.data as DescriptionTemplateReferenceTypeData).referenceType = {
					id: (persist.data as DescriptionTemplateReferenceTypeFieldPersist).referenceTypeId,
					name: selectedReferenceType?.name,
				};
		} else {
			convertedField.data = persist.data;
		}
		return convertedField;
	}

}
