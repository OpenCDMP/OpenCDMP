import { FieldDataExternalDatasetType } from "@app/core/common/enum/field-data-external-dataset-type";
import { FieldType } from "@app/core/common/enum/field-type";
import { FieldValidationType } from "@app/core/common/enum/field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { PluginConfigurationPersist } from "../plugin-configuration/plugin-configuration";
import { IconConfigurationPersist } from "../icon-configuration/icon-configuration";
import { SystemFieldType } from "@app/core/common/enum/system-field-type";


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
	sections?: SectionPersist[];
	pluginConfigurations?: PluginConfigurationPersist[];
	iconConfiguration?: IconConfigurationPersist;
}


// export interface DescriptionTemplatePagePersist {
// 	id: string;
// 	ordinal: number;
// 	title: string;
// 	sections: DescriptionTemplateSectionPersist[];
// }

export interface SectionPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;

	sections?: SectionPersist[];
	fieldSets: FieldSetPersist[];
	semantics?: string[];
}

export interface FieldSetPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: MultiplicityPersist;
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: BaseFieldPersist[];
	semantics?: string[];
}

export interface BaseFieldPersist {
	id: string;
	ordinal: number;
	semantics: string[];
	defaultValue: DefaultValuePersist;
	visibilityRules: RulePersist[];
	validations: FieldValidationType[];
	includeInExport: boolean;
	data: BaseFieldDataPersist;
}

export interface DefaultValuePersist {
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
}

export interface RulePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
}

export interface MultiplicityPersist {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;
	transposedTableView: boolean;
}

export interface BaseFieldDataPersist {
	label: string;
	fieldType: FieldType;
}

//
// Field Types
//



export interface FieldLabelAndMultiplicityDataPersist extends BaseFieldDataPersist {
    multipleSelect: boolean;
}
export interface FieldExternalDatasetDataPersist extends FieldLabelAndMultiplicityDataPersist {
    type: FieldDataExternalDatasetType;
}
export interface FieldReferenceTypePersist extends FieldLabelAndMultiplicityDataPersist {
	referenceTypeId: Guid;
}

export interface FieldLabelDataPersist extends BaseFieldDataPersist {
}

export interface FieldRadioBoxDataPersist extends BaseFieldDataPersist {
	options: FieldRadioBoxOptionPersist[];
}

export interface FieldSelectDataPersist extends FieldLabelAndMultiplicityDataPersist {
	options: FieldSelectOptionPersist[];
}

export interface FieldUploadDataPersist extends BaseFieldDataPersist {
	types: FieldUploadOptionPersist[];
	maxFileSizeInMB: number;
}
export interface FieldNumberDataPersist extends BaseFieldDataPersist {
}
export interface FieldSystemDataPersist extends BaseFieldDataPersist {
    systemFieldType: SystemFieldType;
}
//
// Others
//
export interface FieldSelectOptionPersist {
	label: string;
	value: string;
	semantics?: SemanticValuePersist[];
	exportsNothing?: boolean;
}

export interface FieldRadioBoxOptionPersist {
	label: string;
	value: string;
	semantics?: SemanticValuePersist[];
	exportsNothing?: boolean;
}

export interface SemanticValuePersist {
	key: string;
	value: string;
}

export interface FieldUploadOptionPersist {
	label: string;
	value: string;
}
