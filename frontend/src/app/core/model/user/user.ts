import { RoleOrganizationType } from "@app/core/common/enum/role-organization-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { Reference, ReferencePersist } from "../reference/reference";
import { ContactInfoType } from "@notification-service/core/enum/contact-info-type.enum";

export interface User extends BaseEntity {
	name: string;
	additionalInfo: UserAdditionalInfo;
	contacts: UserContactInfo[];
	globalRoles: UserRole[];
	tenantRoles: UserRole[];
	credentials: UserCredential[];
}

export interface UserPersist extends BaseEntityPersist {
	name: String;
	additionalInfo: UserAdditionalInfoPersist;
}

export interface UserAdditionalInfoPersist {
	avatarUrl: String;
	timezone: String;
	culture: String;
	language: String;
	roleOrganization: String;
	organization: ReferencePersist;
}

export interface UserAdditionalInfo {
	avatarUrl: String;
	timezone: String;
	culture: String;
	language: String;
	roleOrganization: RoleOrganizationType;
	organization: Reference;
}

export interface UserContactInfo {
	id: Guid;
	value: String;
	type: ContactInfoType;
	ordinal: number;
	user: User;
	createdAt: Date;
}

export interface UserRole {
	id: Guid;
	role: String;
	user: User;
	createdAt: Date;
}

export interface UserRolePatchPersist {
	id: Guid;
	roles: String[];
	hasTenantAdminMode: boolean;
	hash: string;
}

export interface UserCredential {
	id: Guid;
	externalId: String;
	user: User;
	createdAt: Date;
	data: UserCredentialData
}

export interface UserCredentialData {
	externalProviderNames: String[];
	email: String;
}

export interface PlanAssociatedUser {
	id: Guid;
	name: string;
	email: string;
}

export interface UserMergeRequestPersist {
	email: string;
}

export interface RemoveCredentialRequestPersist {
	credentialId: Guid;
}

export interface UserTenantUsersInviteRequest {
	users: UserInviteToTenantRequest[];
}

export interface UserInviteToTenantRequest {
	email: string;
	roles: string[];
}
