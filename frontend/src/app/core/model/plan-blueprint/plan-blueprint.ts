import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintExtraFieldDataType } from "@app/core/common/enum/plan-blueprint-field-type";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { PlanBlueprintSystemFieldType } from "@app/core/common/enum/plan-blueprint-system-field-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { ReferenceType } from "../reference-type/reference-type";
import { PrefillingSource } from "../prefilling-source/prefilling-source";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";


export interface PlanBlueprint extends BaseEntity {
	label: string;
	code: string;
	definition: PlanBlueprintDefinition;
	status: PlanBlueprintStatus;
	version: number;
	versionStatus: PlanBlueprintVersionStatus;
	groupId: Guid;
}

export interface PlanBlueprintDefinition {
	sections?: PlanBlueprintDefinitionSection[];
}


export interface PlanBlueprintDefinitionSection {
	id: Guid;
	label: string;
	description: string;
	ordinal: number;
	fields: FieldInSection[];
	hasTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSection[];
	prefillingSourcesEnabled: boolean;
	prefillingSources: PrefillingSource[];
}

export interface DescriptionTemplatesInSection {
	descriptionTemplateGroupId: Guid;
	label: string;
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

//
// Persist
//
export interface PlanBlueprintPersist extends BaseEntityPersist {
	label: string;
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
}

export interface PlanBlueprintDefinitionSectionPersist {
	id: Guid;
	label: string;
	description: string;
	ordinal: number;
	fields: FieldInSectionPersist[];
	hasTemplates: boolean;
	descriptionTemplates?: DescriptionTemplatesInSectionPersist[];
	prefillingSourcesEnabled: boolean;
	prefillingSourcesIds: Guid[];
}

export interface DescriptionTemplatesInSectionPersist {
	descriptionTemplateGroupId: Guid;
	label: string;
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
