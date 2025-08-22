import { DescriptionTemplateFieldDataExternalDatasetType } from "@app/core/common/enum/description-template-field-data-external-dataset-type";
import { DescriptionTemplateFieldType } from "@app/core/common/enum/description-template-field-type";
import { DescriptionTemplateFieldValidationType } from "@app/core/common/enum/description-template-field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { PluginConfigurationPersist } from "../plugin-configuration/plugin-configuration";


export interface DescriptionTemplatePersist extends BaseEntityPersist {
	label: string;
	code: string;
	description: string;
	language: string;
	type: Guid;
	status: DescriptionTemplateStatus;
	definition: DescriptionTemplateDefinitionPersist;
	users: UserDescriptionTemplatePersist[];
}

export interface NewVersionDescriptionTemplatePersist extends BaseEntityPersist {
	label: string;
	code: string;
	description: string;
	language: string;
	type: Guid;
	status: DescriptionTemplateStatus;
	definition: DescriptionTemplateDefinitionPersist;
	users: UserDescriptionTemplatePersist[];
}

export interface UserDescriptionTemplatePersist {
	userId?: Guid;
	role?: UserDescriptionTemplateRole;
}

export interface DescriptionTemplateDefinitionPersist {
	pages?: DescriptionTemplatePagePersist[];
	pluginConfigurations?: PluginConfigurationPersist[];
}


export interface DescriptionTemplatePagePersist {
	id: string;
	ordinal: number;
	title: string;
	sections: DescriptionTemplateSectionPersist[];
}

export interface DescriptionTemplateSectionPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;

	sections?: DescriptionTemplateSectionPersist[];
	fieldSets: DescriptionTemplateFieldSetPersist[];
}

export interface DescriptionTemplateFieldSetPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: DescriptionTemplateMultiplicityPersist;
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: DescriptionTemplateFieldPersist[];
}

export interface DescriptionTemplateFieldPersist {
	id: string;
	ordinal: number;
	semantics: string[];
	defaultValue: DescriptionTemplateDefaultValuePersist;
	visibilityRules: DescriptionTemplateRulePersist[];
	validations: DescriptionTemplateFieldValidationType[];
	includeInExport: boolean;
	data: DescriptionTemplateBaseFieldDataPersist;
}

export interface DescriptionTemplateDefaultValuePersist {
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
}

export interface DescriptionTemplateRulePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
}

export interface DescriptionTemplateMultiplicityPersist {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;
}

export interface DescriptionTemplateBaseFieldDataPersist {
	label: string;
	fieldType: DescriptionTemplateFieldType;
}

//
// Field Types
//

export interface DescriptionTemplateReferenceTypeFieldPersist extends DescriptionTemplateLabelAndMultiplicityDataPersist {
	referenceTypeId: Guid;
}

export interface DescriptionTemplateExternalDatasetDataPersist extends DescriptionTemplateLabelAndMultiplicityDataPersist {
	type: DescriptionTemplateFieldDataExternalDatasetType;
}

export interface DescriptionTemplateLabelAndMultiplicityDataPersist extends DescriptionTemplateBaseFieldDataPersist {
	multipleSelect: boolean;
}

export interface DescriptionTemplateLabelDataPersist extends DescriptionTemplateBaseFieldDataPersist {
}

export interface DescriptionTemplateRadioBoxDataPersist extends DescriptionTemplateBaseFieldDataPersist {
	options: DescriptionTemplateRadioBoxOptionPersist[];
}

export interface DescriptionTemplateSelectDataPersist extends DescriptionTemplateLabelAndMultiplicityDataPersist {
	options: DescriptionTemplateSelectOptionPersist[];
}

export interface DescriptionTemplateUploadDataPersist extends DescriptionTemplateBaseFieldDataPersist {
	types: DescriptionTemplateUploadOptionPersist[];
	maxFileSizeInMB: number;
}

//
// Others
//
export interface DescriptionTemplateSelectOptionPersist {
	label: string;
	value: string;
}

export interface DescriptionTemplateRadioBoxOptionPersist {
	label: string;
	value: string;
}

export interface DescriptionTemplateUploadOptionPersist {
	label: string;
	value: string;
}
