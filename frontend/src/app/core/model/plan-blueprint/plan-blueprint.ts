import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { SystemFieldType } from "@app/core/common/enum/system-field-type";
import { BaseFieldData, FieldDefaultValue, Multiplicity, FieldRule, BaseSection } from "@app/core/model/description-template/description-template";
import { FieldValidationType } from "@app/core/common/enum/field-validation-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { ReferenceType } from "../reference-type/reference-type";
import { PrefillingSource } from "../prefilling-source/prefilling-source";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";
import { PluginConfiguration, PluginConfigurationPersist } from "../plugin-configuration/plugin-configuration";
import { DescriptionTemplate } from "../description-template/description-template";
import { PlanBlueprintType } from "../plan-blueprint-type/plan-blueprint-type";
import { IconConfiguration, IconConfigurationPersist } from "../icon-configuration/icon-configuration";
import { UserPlanBlueprintRole } from "@app/core/common/enum/user-plan-blueprint-role";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { User } from "../user/user";
import { BaseFieldDataPersist, DefaultValuePersist, MultiplicityPersist, RulePersist, SectionPersist } from "../description-template/description-template-persist";


export interface PlanBlueprint extends BaseEntity {
	label?: string;
	description?: string;
	code?: string;
	type?: PlanBlueprintType;
	definition: PlanBlueprintDefinition;
	status?: PlanBlueprintStatus;
	version?: number;
	versionStatus?: PlanBlueprintVersionStatus;
	groupId?: Guid;
	ordinal?: number;
	usageCount?: number;
	users?: UserPlanBlueprint[];
	authorizationFlags?: AppPermission[];
}

export interface UserPlanBlueprint extends BaseEntity {
	planBlueprint?: PlanBlueprint;
	role?: UserPlanBlueprintRole;
	user?: User;
}

export interface PlanBlueprintDefinition {
	sections?: RootSection[];
	pluginConfigurations?: PluginConfiguration[];
	planCreationConsent?: PlanCreationConsent;
	iconConfiguration?: IconConfiguration;
}

export interface RootSection extends BaseSection{
    hasTemplates: boolean;
    canEditDescriptionTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSection[];
	prefillingSourcesEnabled: boolean;
	prefillingSources: PrefillingSource[];
}

/** @deprecated Use RootSection or BaseSection */
export interface PlanBlueprintDefinitionSection {
	id: Guid;
	label: string;
	description: string;
	extendedDescription?: string;
	ordinal: number;
	fieldSets?: BlueprintFieldSet[];
	sections?: PlanBlueprintDefinitionSection[];
	hasTemplates: boolean;
    canEditDescriptionTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSection[];
	prefillingSourcesEnabled: boolean;
	prefillingSources: PrefillingSource[];
}

export interface DescriptionTemplatesInSection {
	descriptionTemplate: DescriptionTemplate;
	minMultiplicity: number;
	maxMultiplicity: number;
}

export interface BlueprintFieldSet {
	id: Guid;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: Multiplicity;
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: (SystemFieldInSection | BlueprintTemplateFieldInSection)[];
}

export interface FieldInSection {
	id: Guid;
	ordinal: number;
	semantics: string[];
	category: PlanBlueprintFieldCategory;
	label: string;
	placeholder: string;
	description: string;
	required: boolean;
}

export interface SystemFieldInSection extends FieldInSection {
	systemFieldType: SystemFieldType;
}

export interface BlueprintTemplateFieldInSection extends FieldInSection {
	visibilityRules?: FieldRule[];
	validations?: FieldValidationType[];
	includeInExport?: boolean;
	defaultValue?: FieldDefaultValue;
	data?: BaseFieldData;
}

/** @deprecated Use BlueprintTemplateFieldInSection instead */
export interface ExtraFieldInSection extends FieldInSection {
	dataType: number;
}

/** @deprecated Use BlueprintTemplateFieldInSection with REFERENCE_TYPES data instead */
export interface ReferenceTypeFieldInSection extends FieldInSection {
	referenceType: ReferenceType;
	multipleSelect: boolean;
}

/** @deprecated Use BlueprintTemplateFieldInSection with UPLOAD data instead */
export interface UploadFieldInSection extends FieldInSection {
	types: UploadOption[];
	maxFileSizeInMB: number;
}

/** @deprecated */
export interface UploadOption {
	label: string;
	value: string;
}

export interface PlanCreationConsent {
	enabled: boolean;
	message: string;
	details: string;
}

//
// Persist
//
export interface PlanBlueprintPersist extends BaseEntityPersist {
	label: string;
	description: string;
	code: string;
	definition: PlanBlueprintDefinitionPersist;
	status: PlanBlueprintStatus;
	users: UserPlanBlueprintPersist[];
}

export interface NewVersionPlanBlueprintPersist {
	label: string;
	definition: PlanBlueprintDefinitionPersist;
	status: PlanBlueprintStatus;
	users: UserPlanBlueprintPersist[];
}

export interface UserPlanBlueprintPersist {
	userId?: Guid;
	role?: UserPlanBlueprintRole;
}

export interface PlanBlueprintDefinitionPersist {
	sections?: RootSectionPersist[];
	pluginConfigurations?: PluginConfigurationPersist[];
	planCreationConsent?: PlanCreationConsentPersist;
	iconConfiguration?: IconConfigurationPersist;
}

export interface RootSectionPersist extends SectionPersist{
	hasTemplates: boolean;
    canEditDescriptionTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSectionPersist[];
	prefillingSourcesEnabled: boolean;
	prefillingSourcesIds: Guid[];
}
export interface PlanBlueprintDefinitionSectionPersist {
	id: Guid;
	label: string;
	description: string;
	extendedDescription?: string;
	ordinal: number;
	fieldSets?: BlueprintFieldSetPersist[];
	sections?: PlanBlueprintDefinitionSectionPersist[];
	hasTemplates: boolean;
    canEditDescriptionTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSectionPersist[];
	prefillingSourcesEnabled: boolean;
	prefillingSourcesIds: Guid[];
}

export interface DescriptionTemplatesInSectionPersist {
	descriptionTemplateGroupId: Guid;
	minMultiplicity: number;
	maxMultiplicity: number;
}

export interface BlueprintFieldSetPersist {
	id: Guid;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: MultiplicityPersist;
	hasMultiplicity: boolean;
	hasCommentField: boolean;
	fields: FieldInSectionPersist[];
}

export interface FieldInSectionPersist {
	id: Guid;
	category: PlanBlueprintFieldCategory;
	label: string;
	placeholder: string;
	description: string;
	semantics: string[];
	required: boolean;
	ordinal: number;
}

export interface SystemFieldInSectionPersist extends FieldInSectionPersist {
	systemFieldType: SystemFieldType;
}

export interface BlueprintTemplateFieldInSectionPersist extends FieldInSectionPersist {
	visibilityRules?: RulePersist[];
	validations?: FieldValidationType[];
	includeInExport?: boolean;
	defaultValue?: DefaultValuePersist;
	data?: BaseFieldDataPersist;
}

/** @deprecated Use BlueprintTemplateFieldInSectionPersist instead */
export interface ExtraFieldInSectionPersist extends FieldInSectionPersist {
	dataType: number;
}

/** @deprecated Use BlueprintTemplateFieldInSectionPersist instead */
export interface ReferenceTypeFieldInSectionPersist extends FieldInSectionPersist {
	referenceTypeId: Guid;
	multipleSelect: boolean;
}

/** @deprecated Use BlueprintTemplateFieldInSectionPersist instead */
export interface UploadFieldInSectionPersist extends FieldInSectionPersist {
	types: UploadOptionPersist[];
	maxFileSizeInMB: number;
}

/** @deprecated */
export interface UploadOptionPersist {
	label: string;
	value: string;
}

export interface PlanCreationConsentPersist {
	enabled: boolean;
	message: string;
	details: string;
}
