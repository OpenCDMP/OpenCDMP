import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { BaseEntity, BaseEntityPersist } from '@common/base/base-entity.model';
import { Guid } from '@common/types/guid';
import { DescriptionTemplate } from '../description-template/description-template';
import { BaseDescription, Description, DescriptionPersist, PropertyDefinitionPersist } from '../description/description';
import { PlanBlueprint } from '../plan-blueprint/plan-blueprint';
import { EntityDoi } from '../entity-doi/entity-doi';
import { PlanUpdateRequestApply } from '../plan-update-request/plan-update-request';
import { ReferencePersist } from '../reference/reference';
import { User } from "../user/user";
import { PlanReference } from './plan-reference';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatus } from '../plan-status/plan-status';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';
import { PropertyDefinition } from '../description/description';
export interface BasePlan extends BaseEntity {
    label?: string;
	version?: number;
	groupId?: string;
	description?: string;
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
	language?: string;
	publicAfter?: Date;
	planUsers?: PlanUser[];
	descriptions?: Description[];
	planDescriptionTemplates?: PlanDescriptionTemplate[];
	otherPlanVersions?: Plan[];
	availableStatuses?: PlanStatus[];
    authorizationInformation?: PlanAuthorizationInformation;
	statusAuthorizationFlags?: PlanStatusPermission[];
}

export interface PlanAuthorizationInformation {
    globalAuthorizationFlags: AppPermission[];
    authorizationInformationPerSections: PlanAuthorizationInformationPerSection[];
}
export interface PlanAuthorizationInformationPerSection {
    sectionId: string;
    authorizationFlags: AppPermission[]
}
export interface PlanProperties extends PropertyDefinition{
    //TODO: remove planBlueprintValues
	// planBlueprintValues: PlanBlueprintValue[];
	contacts: PlanContact[];
	consent: PlanPropertiesConsent;
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

export interface PlanPropertiesConsent {
	consentBy: User[];
	consentAt: Date;
}

export interface BasePlanUser extends BaseEntity {
    user: User;
    role: PlanUserRole;
}

export interface PlanUser extends BasePlanUser {
	plan: Plan;
	sectionId: string;
	ordinal: number;
}


export interface PlanDescriptionTemplate extends BaseEntity {
	plan?: Plan;
	currentDescriptionTemplate?: DescriptionTemplate;
	descriptionTemplates?: DescriptionTemplate[];
	descriptionTemplateGroupId?: Guid;
	sectionId?: string;
}

//
// Persist
//
export interface PlanPersist extends BaseEntityPersist {
	label: string;
	statusId: Guid;
	properties: PlanPropertiesPersist;
	description: string;
	language: string;
	blueprint: Guid;
	accessType: PlanAccessType;
	descriptionTemplates: PlanDescriptionTemplatePersist[];
	users: PlanUserPersist[];
	planUpdateRequestApply?: PlanUpdateRequestApply;
}

export interface PlanPropertiesPersist extends PropertyDefinitionPersist{
	contacts: PlanContactPersist[];
	includeConsent: Boolean;
}

// FE-4: payload for the atomic plan/persist-with-descriptions endpoint (plan + its edited descriptions
// saved in one backend transaction).
export interface PlanWithDescriptionsPersist {
	plan: PlanPersist;
	descriptions: DescriptionPersist[];
}


export interface PlanContactPersist {
	firstName: string;
	lastName: string;
	email: string;
}

export interface PlanDescriptionTemplatePersist {
	descriptionTemplateGroupId: Guid;
	sectionId: string;
}

export interface ClonePlanPersist {
	id: Guid;
	label: string;
	description: string;
	descriptions: Guid[];
}

export interface NewVersionPlanPersist {
	id: Guid;
	label: string;
	description: string;
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