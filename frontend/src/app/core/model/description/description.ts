import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { DescriptionTemplate } from "../description-template/description-template";
import { Plan, PlanDescriptionTemplate, PublicPlan } from "../plan/plan";
import { Reference, ReferencePersist } from "../reference/reference";
import { Tag, TagPersist } from "../tag/tag";
import { User } from "../user/user";
import { AppPermission } from "@app/core/common/enum/permission.enum";

export interface Description extends BaseDescription {
	label?: string;
	properties?: DescriptionPropertyDefinition;
	description?: string;
	createdBy?: User;
	finalizedAt?: Date;
	descriptionReferences?: DescriptionReference[];
	descriptionTags?: DescriptionTag[];
	descriptionTemplate?: DescriptionTemplate;
	planDescriptionTemplate?: PlanDescriptionTemplate;
	plan?: Plan;
	authorizationFlags?: AppPermission[];
}


export interface DescriptionPropertyDefinition {
	fieldSets: Map<string, DescriptionPropertyDefinitionFieldSet>;
}

export interface DescriptionPropertyDefinitionFieldSet {
	items?: DescriptionPropertyDefinitionFieldSetItem[];
	comment?: string;
}

export interface DescriptionPropertyDefinitionFieldSetItem {
	fields?: Map<string, DescriptionField>;
	ordinal?: number;
}

export interface DescriptionField {
	textValue: string;
	textListValue: string[];
	dateValue: Date;
	booleanValue: boolean;
	externalIdentifier?: DescriptionExternalIdentifier;
	references: Reference[];
	tags: Tag[];
}

export interface DescriptionExternalIdentifier {
	identifier: string;
	type: string;
}

export interface DescriptionReference extends BaseEntity {
	description?: Description;
	reference?: Reference;
	data?: DescriptionReferenceData;
}

export interface DescriptionReferenceData {
	fieldId?: string;
	ordinal?: number;
}

export interface DescriptionTag extends BaseEntity {
	description?: Description;
	tag?: Tag;
}

//
// Persist
//
export interface DescriptionPersist extends BaseEntityPersist {
	label: string;
	planId: Guid;
	planDescriptionTemplateId: Guid;
	descriptionTemplateId: Guid;
	status: DescriptionStatusEnum;
	description: string;
	properties: DescriptionPropertyDefinitionPersist;
	tags: string[];
}

export interface DescriptionPropertyDefinitionPersist {
	fieldSets: Map<string, DescriptionPropertyDefinitionFieldSetPersist>;
}

export interface DescriptionPropertyDefinitionFieldSetPersist {
	items?: DescriptionPropertyDefinitionFieldSetItemPersist[];
	comment?: string;
}

export interface DescriptionPropertyDefinitionFieldSetItemPersist {
	fields?: Map<string, DescriptionFieldPersist>;
	ordinal?: number;
}

export interface DescriptionFieldPersist {
	textValue: string;
	textListValue: string[];
	dateValue: Date;
	booleanValue: boolean;
	references: ReferencePersist[];
	reference: ReferencePersist;
	externalIdentifier?: DescriptionExternalIdentifierPersist;
	tags: string[];
}

export interface DescriptionExternalIdentifierPersist {
	identifier: string;
	type: string;
}

export interface DescriptionReferencePersist {
	id: Guid;
	reference?: ReferencePersist;
	hash: string;
}

export interface DescriptionStatusPersist {
	id: Guid;
	status: DescriptionStatusEnum;
	hash: string;
}

//
// Public
//

export interface PublicDescription extends BaseDescription {
	label?: string;
	status?: DescriptionStatusEnum;
	description?: string;
	finalizedAt?: Date;
	descriptionTemplate?: PublicDescriptionTemplate;
	planDescriptionTemplate?: PublicPlanDescriptionTemplate;
	plan?: PublicPlan;
}

export interface PublicPlanDescriptionTemplate {
	id: Guid;
	plan: PublicPlan;
}

export interface PublicDescriptionTemplate {
	id: Guid;
	label: string;
	description: string;
}
export interface DescriptionSectionPermissionResolver {
	planId: Guid;
	sectionIds: Guid[];
	permissions: string[];
}

export interface UpdateDescriptionTemplatePersist {
	id: Guid;
	hash?: string;
}

export interface BaseDescription extends BaseEntity {
	tenantId?: Guid;
	status?: DescriptionStatusEnum;
}
