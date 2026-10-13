import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { DescriptionTemplate } from "../description-template/description-template";
import { Plan, PlanDescriptionTemplate } from "../plan/plan";
import { Reference, ReferencePersist } from "../reference/reference";
import { Tag } from "../tag/tag";
import { User } from "../user/user";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { DescriptionStatus } from "../description-status/description-status";
import { DescriptionStatusPermission } from "@app/core/common/enum/description-status-permission.enum";
import { PlanUpdateRequestApply } from "../plan-update-request/plan-update-request";

export interface Description extends BaseDescription {
	label?: string;
	status?: DescriptionStatus;
	properties?: PropertyDefinition;
	description?: string;
	createdBy?: User;
	finalizedAt?: Date;
	descriptionReferences?: DescriptionReference[];
	descriptionTags?: DescriptionTag[];
	descriptionTemplate?: DescriptionTemplate;
	planDescriptionTemplate?: PlanDescriptionTemplate;
	plan?: Plan;
	availableStatuses?: DescriptionStatus[];
	authorizationFlags?: AppPermission[];
	statusAuthorizationFlags?: DescriptionStatusPermission[];
}


export interface PropertyDefinition {
	fieldSets: Map<string, PropertyDefinitionFieldSet>;
}

export interface PropertyDefinitionFieldSet {
	items?: PropertyDefinitionFieldSetItem[];
	comment?: string;
}

export interface PropertyDefinitionFieldSetItem {
	fields?: Map<string, PropertyDefinitionField>;
	ordinal?: number;
}

export interface PropertyDefinitionField {
	textValue: string;
	textListValue: string[];
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
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
export interface DescriptionMultiplePersist {
	descriptions: DescriptionPersist[];
	planUpdateRequestApply?: PlanUpdateRequestApply;
}

export interface DescriptionPersist extends BaseEntityPersist {
	label: string;
	planId: Guid;
	planDescriptionTemplateId: Guid;
	descriptionTemplateId: Guid;
	statusId: Guid;
	description: string;
	properties: PropertyDefinitionPersist;
	tags: string[];
}

export interface PropertyDefinitionPersist {
	fieldSets: Map<string, PropertyDefinitionFieldSetPersist>;
}

export interface PropertyDefinitionFieldSetPersist {
	items?: PropertyDefinitionFieldSetItemPersist[];
	comment?: string;
}

export interface PropertyDefinitionFieldSetItemPersist {
	fields?: Map<string, FieldPersist>;
	ordinal?: number;
}

export interface FieldPersist {
	textValue?: string;
	textListValue?: string[];
	dateValue?: Date;
	booleanValue?: boolean;
	numberValue?: number;
	references?: ReferencePersist[];
	reference?: ReferencePersist;
	externalIdentifier?: DescriptionExternalIdentifierPersist;
	tags?: string[];
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
	statusId?: Guid;
	hash: string;
}

export interface DescriptionSectionPermissionResolver {
	planId: Guid;
	sectionIds: string[];
	permissions: string[];
}

export interface UpdateDescriptionTemplatePersist {
	id: Guid;
	hash?: string;
}

export interface BaseDescription extends BaseEntity {
	tenantId?: Guid;
}
