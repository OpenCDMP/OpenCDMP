import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintExtraFieldDataType } from "@app/core/common/enum/plan-blueprint-field-type";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { PlanBlueprintSystemFieldType } from "@app/core/common/enum/plan-blueprint-system-field-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { ReferenceType } from "../reference-type/reference-type";
import { PrefillingSource } from "../prefilling-source/prefilling-source";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";
import { PluginConfiguration, PluginConfigurationPersist } from "../plugin-configuration/plugin-configuration";
import { DescriptionTemplate } from "../description-template/description-template";


export interface PlanBlueprint extends BaseEntity {
	label?: string;
	description?: string;
	code?: string;
	definition: PlanBlueprintDefinition;
	status?: PlanBlueprintStatus;
	version?: number;
	versionStatus?: PlanBlueprintVersionStatus;
	groupId?: Guid;
	ordinal?: number;
}

export interface PlanBlueprintDefinition {
	sections?: PlanBlueprintDefinitionSection[];
	pluginConfigurations?: PluginConfiguration[];
}


export interface PlanBlueprintDefinitionSection {
	id: Guid;
	label: string;
	description: string;
	ordinal: number;
	fields: (SystemFieldInSection | ExtraFieldInSection | ReferenceTypeFieldInSection | UploadFieldInSection)[];
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

export interface FieldInSection {
	id: Guid;
	category: PlanBlueprintFieldCategory;
	label: string;
	placeholder: string;
	description: string;
	semantics: string[];
	required: boolean;
	ordinal: number;
}

export interface PublicPlanBlueprint extends BaseEntity {
	label?: string;
	description?: string;
	definition: PublicPlanBlueprintDefinition;
}

export interface PublicPlanBlueprintDefinition {
	sections?: PublicPlanBlueprintDefinitionSection[];
}

export interface PublicPlanBlueprintDefinitionSection {
	id: Guid;
	label: string;
	description: string;
	hasTemplates: boolean;
	fields: (SystemFieldInSection | ExtraFieldInSection | ReferenceTypeFieldInSection | UploadFieldInSection)[];

}

export interface SystemFieldInSection extends FieldInSection {
	systemFieldType: PlanBlueprintSystemFieldType;
}

export interface ExtraFieldInSection extends FieldInSection {
	dataType: PlanBlueprintExtraFieldDataType;
}

export interface ReferenceTypeFieldInSection extends FieldInSection {
	referenceType: ReferenceType;
	multipleSelect: boolean;
}

export interface UploadFieldInSection extends FieldInSection {
	types: UploadOption[];
	maxFileSizeInMB: number;
}

export interface UploadOption {
	label: string;
	value: string;
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
}

export interface NewVersionPlanBlueprintPersist {
	label: string;
	definition: PlanBlueprintDefinitionPersist;
	status: PlanBlueprintStatus;
}

export interface PlanBlueprintDefinitionPersist {
	sections?: PlanBlueprintDefinitionSectionPersist[];
	pluginConfigurations?: PluginConfigurationPersist[];
}

export interface PlanBlueprintDefinitionSectionPersist {
	id: Guid;
	label: string;
	description: string;
	ordinal: number;
	fields: FieldInSectionPersist[];
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
	systemFieldType: PlanBlueprintSystemFieldType;
}

export interface ExtraFieldInSectionPersist extends FieldInSectionPersist {
	dataType: PlanBlueprintExtraFieldDataType;
}

export interface ReferenceTypeFieldInSectionPersist extends FieldInSectionPersist {
	referenceTypeId: Guid;
	multipleSelect: boolean;
}

export interface UploadFieldInSectionPersist extends FieldInSectionPersist {
	types: UploadOptionPersist[];
	maxFileSizeInMB: number;
}

export interface UploadOptionPersist {
	label: string;
	value: string;
}
