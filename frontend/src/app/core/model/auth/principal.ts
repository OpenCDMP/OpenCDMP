import { AppPermission } from "@app/core/common/enum/permission.enum";
import { Guid } from "@common/types/guid";
import { Tenant } from "../tenant/tenant";

export interface AppAccount {
	isAuthenticated: boolean;
	userExists: boolean;
	permissions: AppPermission[];
	principal: AppPrincipalInfo;
	profile: UserProfileInfo;
	selectedTenant: Tenant;
}

export interface AppPrincipalInfo {
	subject: Guid;
	name: string;
	scope: string[];
	client: string;
	notBefore: Date;
	authenticatedAt: Date;
	expiresAt: Date;
	userId: Guid;
	more: Record<string, string[]>
}

export interface UserProfileInfo {
	culture: string;
	language: string;
	timezone: string;
	avatarUrl: string;
	email: string;
}
