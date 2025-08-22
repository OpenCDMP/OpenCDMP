import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { BaseEntity, BaseEntityPersist } from '@common/base/base-entity.model';
import { Guid } from '@common/types/guid';
import { DescriptionTemplate } from '../description-template/description-template';
import { BaseDescription, Description, PublicDescription } from '../description/description';
import { PlanBlueprint, PublicPlanBlueprint } from '../plan-blueprint/plan-blueprint';
import { EntityDoi } from '../entity-doi/entity-doi';
import { ReferencePersist } from '../reference/reference';
import { User } from "../user/user";
import { PlanReference } from './plan-reference';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { EntityType } from '@app/core/common/enum/entity-type';
import { PlanStatus } from '../plan-status/plan-status';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';

export interface BasePlan extends BaseEntity {
    label?: string;
	version?: number;
	groupId?: String;
	description?: String;
	publishedAt?: Date;
	finalizedAt?: Date;
	accessType?: PlanAccessType;
	planReferences?: PlanReference[];
	entityDois?: EntityDoi[];
	tenantId?: Guid;
	status?: PlanStatus;
	descriptions?: BaseDescription[];
}
export interface Plan extends BasePlan {
	versionStatus?: PlanVersionStatus;
	properties?: PlanProperties;
	creator?: User;
	blueprint?: PlanBlueprint;
	language?: String;
	publicAfter?: Date;
	planUsers?: PlanUser[];
	descriptions?: Description[];
	planDescriptionTemplates?: PlanDescriptionTemplate[];
	otherPlanVersions?: Plan[];
	availableStatuses?: PlanStatus[];
	authorizationFlags?: AppPermission[];
	statusAuthorizationFlags?: PlanStatusPermission[];
}

export interface PublicPlan extends BasePlan {
	blueprint?: PublicPlanBlueprint;
	language?: String;
	properties?: PublicPlanProperties;
	planUsers: PublicPlanUser[];
	descriptions: PublicDescription[];
	otherPlanVersions?: PublicPlan[];
}

export interface PublicPlanProperties {
	planBlueprintValues: PublicPlanBlueprintValue[];
	contacts: PublicPlanContact[];
}

export interface PublicPlanBlueprintValue {
	fieldId: Guid;
	fieldValue: string;
	dateValue: Date;
	numberValue: number;
}

export interface PublicPlanContact {
	firstName: string;
	lastName: string;
}

export interface PlanProperties {
	planBlueprintValues: PlanBlueprintValue[];
	contacts: PlanContact[];
}

export interface PlanBlueprintValue {
	fieldId: Guid;
	fieldValue: string;
	dateValue: Date;
	numberValue: number;
}

export interface PlanContact {
	firstName: string;
	lastName: string;
	email: string;
}

export interface BasePlanUser extends BaseEntity {
    user: User;
    role: PlanUserRole;
}

export interface PlanUser extends BasePlanUser {
	plan: Plan;
	sectionId: Guid;
	ordinal: number;
}

export interface PublicPlanUser extends BasePlanUser{
	plan: PublicPlan;
}


export interface PlanDescriptionTemplate extends BaseEntity {
	plan?: Plan;
	currentDescriptionTemplate?: DescriptionTemplate;
	descriptionTemplates?: DescriptionTemplate[];
	descriptionTemplateGroupId?: Guid;
	sectionId?: Guid;
}

//
// Persist
//
export interface PlanPersist extends BaseEntityPersist {
	label: string;
	statusId: Guid;
	properties: PlanPropertiesPersist;
	description: String;
	language: String;
	blueprint: Guid;
	accessType: PlanAccessType;
	descriptionTemplates: PlanDescriptionTemplatePersist[];
	users: PlanUserPersist[];
}

export interface PlanPropertiesPersist {
	planBlueprintValues: Map<Guid, PlanBlueprintValuePersist>;
	contacts: PlanContactPersist[];
}

export interface PlanBlueprintValuePersist {
	fieldId: Guid;
	fieldValue: string;
	dateValue: Date;
	numberValue: number;
	references: PlanReferencePersist[];
	reference: PlanReferencePersist;
}

export interface PlanContactPersist {
	firstName: string;
	lastName: string;
	email: string;
}

export interface PlanReferencePersist {
	reference?: ReferencePersist;
	data?: PlanReferenceDataPersist;
}

export interface PlanReferenceDataPersist {
	blueprintFieldId: Guid;
}

export interface PlanDescriptionTemplatePersist {
	descriptionTemplateGroupId: Guid;
	sectionId: Guid;
}

export interface ClonePlanPersist {
	id: Guid;
	label: string;
	description: String;
	descriptions: Guid[];
}

export interface NewVersionPlanPersist {
	id: Guid;
	label: string;
	description: String;
	blueprintId: Guid;
	descriptions: NewVersionPlanDescriptionPersist[];
	hash?: string;
}

export interface NewVersionPlanDescriptionPersist {
	descriptionId: Guid;
	blueprintSectionId: Guid;
}

export interface PlanUserPersist {
	user: Guid;
	role: PlanUserRole;
	email: string;
	sectionId: string;
	ordinal: number;
}

export interface PlanUserRemovePersist {
	id: Guid;
	planId: Guid;
	role: PlanUserRole;
}

export interface PlanUserInvitePersist {
	users: PlanUserPersist[];
}

//
// Invitation
//

export interface PlanInvitationResult {
	planId: Guid;
	isAlreadyAccepted: boolean;
}