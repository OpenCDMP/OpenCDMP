import { RoleOrganizationType } from "@app/core/common/enum/role-organization-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { Reference, ReferencePersist } from "../reference/reference";
import { ContactInfoType } from "@notification-service/core/enum/contact-info-type.enum";
import { PluginConfigurationUser, PluginConfigurationUserPersist } from "../plugin-configuration/plugin-configuration";

export interface User extends BaseEntity {
	name: string;
	additionalInfo: UserAdditionalInfo;
	contacts: UserContactInfo[];
	globalRoles: UserRole[];
	tenantRoles: UserRole[];
	credentials: UserCredential[];
}

export interface UserPersist extends BaseEntityPersist {
	name: string;
	additionalInfo: UserAdditionalInfoPersist;
}

export interface UserAdditionalInfoPersist {
	avatarUrl: string;
	timezone: string;
	culture: string;
	language: string;
	roleOrganization: string;
	organization: ReferencePersist;
	pluginConfigurations?: PluginConfigurationUserPersist[];
}

export interface UserAdditionalInfo {
	avatarUrl: string;
	timezone: string;
	culture: string;
	language: string;
	roleOrganization: RoleOrganizationType;
	organization: Reference;
	pluginConfigurations?: PluginConfigurationUser[];
}

export interface UserContactInfo {
	id: Guid;
	value: string;
	type: ContactInfoType;
	ordinal: number;
	user: User;
	createdAt: Date;
}

export interface UserRole {
	id: Guid;
	role: string;
	user: User;
	createdAt: Date;
}

export interface UserRolePatchPersist {
	id: Guid;
	roles: string[];
	hasTenantAdminMode: boolean;
	hash: string;
}

export interface UserCredential {
	id: Guid;
	externalId: string;
	user: User;
	createdAt: Date;
	data: UserCredentialData
}

export interface UserCredentialData {
	externalProviderNames: string[];
	email: string;
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
