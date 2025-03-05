import { AppPermission } from "@app/core/common/enum/permission.enum";
import { Guid } from "@common/types/guid";

export interface AnnotationAccount {
	isAuthenticated: boolean;
	userExists: boolean;
	permissions: AppPermission[];
	principal: AnnotationPrincipalInfo;
}

export interface AnnotationPrincipalInfo {
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

