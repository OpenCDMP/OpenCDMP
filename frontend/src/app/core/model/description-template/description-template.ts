import { FieldDataExternalDatasetType } from "@app/core/common/enum/field-data-external-dataset-type";
import { FieldType } from "@app/core/common/enum/field-type";
import { FieldValidationType } from "@app/core/common/enum/field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { BaseEntity } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { DescriptionTemplateType } from "../description-template-type/description-template-type";
import { ReferenceType } from "../reference-type/reference-type";
import { User } from "../user/user";
import { DescriptionTemplateVersionStatus } from "@app/core/common/enum/description-template-version-status";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { PluginConfiguration } from "../plugin-configuration/plugin-configuration";
import { IconConfiguration } from "../icon-configuration/icon-configuration";
import { SystemFieldType } from "@app/core/common/enum/system-field-type";


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
	ordinal?: number;
	usageCount?: number;
}

export interface UserDescriptionTemplate extends BaseEntity {
	descriptionTemplate?: DescriptionTemplate;
	role?: UserDescriptionTemplateRole;
	user?: User;
}

export interface DescriptionTemplateDefinition {
	sections?: BaseSection[];
	pluginConfigurations?: PluginConfiguration[];
	iconConfiguration?: IconConfiguration;
}


export interface BaseSection {
	id?: string;
	ordinal?: number;
	title?: string;
	description?: string;
	sections?: BaseSection[];
	fieldSets?: BaseFieldSet[];
	/** The object keys the section describes, e.g. `rda.dataset.distribution`. */
	semantics?: string[];
}

export interface BaseFieldSet {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: Multiplicity
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: BaseField[];
	/** The object keys the question describes, e.g. `rda.dataset.distribution.host`. */
	semantics?: string[];
}

export interface BaseField<T extends BaseFieldData = BaseFieldData> {
	id: string;
	ordinal: number;
	semantics?: string[];
	defaultValue?: FieldDefaultValue;
	visibilityRules?: FieldRule[];
	validations?: FieldValidationType[];
	includeInExport: boolean;
	data?: T;
}

export interface FieldDefaultValue {
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
}


export interface FieldRule {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
}

export interface Multiplicity {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;
	transposedTableView: boolean;
}

export interface BaseFieldData {
	label: string;
	fieldType: FieldType;
	multipleSelect?: boolean;
}


//
// Field Types
//

export interface FieldLabelData extends BaseFieldData {
}

export interface FieldLabelAndMultiplicityData extends BaseFieldData {
	multipleSelect: boolean;
}

export interface FieldExternalDatasetData extends FieldLabelAndMultiplicityData {
	type?: FieldDataExternalDatasetType;
}

export interface FieldReferenceTypeData extends FieldLabelAndMultiplicityData {
	referenceType?: ReferenceType;
}

export interface FieldRadioBoxData extends BaseFieldData {
	options: FieldRadioBoxOption[];
}

export interface FieldSelectData extends FieldLabelAndMultiplicityData {
	options: FieldSelectOption[];
}

export interface FieldUploadData extends BaseFieldData {
	types: FieldUploadOption[];
	maxFileSizeInMB: number;
}

export interface FieldNumberData extends BaseFieldData {
}
export interface FieldSystemData extends BaseFieldData {
    systemFieldType: SystemFieldType;
}
//
// Others
//

export interface FieldSelectOption {
	label: string;
	value: string;
	/** The key values choosing this option stands for, exported as if an input with that key were answered. */
	semantics?: SemanticValue[];
	/** Choosing this option means nothing is exported for the question's object. */
	exportsNothing?: boolean;
}

export interface FieldRadioBoxOption {
	label: string;
	value: string;
	/** The key values choosing this option stands for, exported as if an input with that key were answered. */
	semantics?: SemanticValue[];
	/** Choosing this option means nothing is exported for the question's object. */
	exportsNothing?: boolean;
}

/** A semantic key and the value an answer option stands for. */
export interface SemanticValue {
	key: string;
	value: string;
}

export interface FieldUploadOption {
	label: string;
	value: string;
}
