import { AnnotationProtectionType } from "@app/core/common/enum/annotation-protection-type.enum";
import { User } from "@app/core/model/user/user";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { AnnotationStatus } from "./annotation-status.model";
import { AnnotationPermission } from "../enum/permission.enum";
import { AnnotationEntityType } from "@app/core/common/enum/annotation-entity-type";

export interface Annotation extends BaseEntity {
	entityId: Guid;
	entityType: AnnotationEntityType;
	anchor?: string;
	payload: string;
	timeStamp: Date;
	author: User;
	threadId: Guid;
	parent: Annotation;
	protectionType: AnnotationProtectionType;
	annotationStatuses: AnnotationStatus[];
	authorizationFlags?: AnnotationPermission[];
}

export interface AnnotationPersist extends BaseEntityPersist {
	entityId: Guid;
	entityType: string;
	anchor: string;
	payload: string;
	threadId?: Guid;
	parentId?: Guid;
	protectionType: AnnotationProtectionType;
}