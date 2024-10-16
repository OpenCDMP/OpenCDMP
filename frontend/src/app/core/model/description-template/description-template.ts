import { DescriptionTemplateFieldDataExternalDatasetType } from "@app/core/common/enum/description-template-field-data-external-dataset-type";
import { DescriptionTemplateFieldType } from "@app/core/common/enum/description-template-field-type";
import { DescriptionTemplateFieldValidationType } from "@app/core/common/enum/description-template-field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { BaseEntity } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { DescriptionTemplateType } from "../description-template-type/description-template-type";
import { ReferenceType } from "../reference-type/reference-type";
import { User } from "../user/user";
import { Reference } from "../reference/reference";
import { DescriptionTemplateVersionStatus } from "@app/core/common/enum/description-template-version-status";
import { AppPermission } from "@app/core/common/enum/permission.enum";


export interface DescriptionTemplate extends BaseEntity {
	label?: string;
	code?: string;
	description?: string;
	groupId?: Guid;
	version?: string;
	language?: string;
	type?: DescriptionTemplateType;
	status?: DescriptionTemplateStatus;
	definition?: DescriptionTemplateDefinition;
	users?: UserDescriptionTemplate[];
	versionStatus?: DescriptionTemplateVersionStatus;
	authorizationFlags?: AppPermission[];
}

export interface UserDescriptionTemplate extends BaseEntity {
	descriptionTemplate?: DescriptionTemplate;
	role?: UserDescriptionTemplateRole;
	user?: User;
}

export interface DescriptionTemplateDefinition {
	pages?: DescriptionTemplatePage[];
}


export interface DescriptionTemplatePage {
	id?: string;
	ordinal?: number;
	title?: string;
	sections?: DescriptionTemplateSection[];
}

export interface DescriptionTemplateSection {
	id?: string;
	ordinal?: number;
	title?: string;
	description?: string;
	sections?: DescriptionTemplateSection[];
	fieldSets?: DescriptionTemplateFieldSet[];
}

export interface DescriptionTemplateFieldSet {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: DescriptionTemplateMultiplicity
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: DescriptionTemplateField[];
}

export interface DescriptionTemplateField {
	id: string;
	ordinal: number;
	semantics?: string[];
	defaultValue?: DescriptionTemplateDefaultValue;
	visibilityRules?: DescriptionTemplateRule[];
	validations?: DescriptionTemplateFieldValidationType[];
	includeInExport: boolean;
	data?: DescriptionTemplateBaseFieldData;
}

export interface DescriptionTemplateDefaultValue {
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
}


export interface DescriptionTemplateRule {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
}

export interface DescriptionTemplateMultiplicity {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;
}

export interface DescriptionTemplateBaseFieldData {
	label: string;
	fieldType: DescriptionTemplateFieldType;
	multipleSelect?: boolean;
}


//
// Field Types
//
export interface DescriptionTemplateLabelData extends DescriptionTemplateBaseFieldData {
}

export interface DescriptionTemplateLabelAndMultiplicityData extends DescriptionTemplateBaseFieldData {
	multipleSelect: boolean;
}

export interface DescriptionTemplateExternalDatasetData extends DescriptionTemplateLabelAndMultiplicityData {
	type?: DescriptionTemplateFieldDataExternalDatasetType;
}

export interface DescriptionTemplateReferenceTypeData extends DescriptionTemplateLabelAndMultiplicityData {
	referenceType?: ReferenceType;
}

export interface DescriptionTemplateRadioBoxData extends DescriptionTemplateBaseFieldData {
	options: DescriptionTemplateRadioBoxOption[];
}

export interface DescriptionTemplateSelectData extends DescriptionTemplateLabelAndMultiplicityData {
	options: DescriptionTemplateSelectOption[];
}

export interface DescriptionTemplateUploadData extends DescriptionTemplateBaseFieldData {
	types: DescriptionTemplateUploadOption[];
	maxFileSizeInMB: number;
}


//
// Others
//

export interface DescriptionTemplateSelectOption {
	label: string;
	value: string;
}

export interface DescriptionTemplateRadioBoxOption {
	label: string;
	value: string;
}

export interface DescriptionTemplateUploadOption {
	label: string;
	value: string;
}
