import { AppPermission } from "@app/core/common/enum/permission.enum";
import { Guid } from "@common/types/guid";

export interface NotificationAccount {
	isAuthenticated: boolean;
	userExists: boolean;
	permissions: AppPermission[];
	principal: NotificationPrincipalInfo;
}

export interface NotificationPrincipalInfo {
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

