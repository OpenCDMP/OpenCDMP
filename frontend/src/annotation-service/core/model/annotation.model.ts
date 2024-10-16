import { AnnotationProtectionType } from "@app/core/common/enum/annotation-protection-type.enum";
import { User } from "@app/core/model/user/user";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { AnnotationStatus } from "./annotation-status.model";

export interface Annotation extends BaseEntity {
	entityId: Guid;
	entityType: string;
	anchor?: string;
	payload: string;
	timeStamp: Date;
	author: User;
	threadId: Guid;
	parent: Annotation;
	protectionType: AnnotationProtectionType;
	annotationStatuses: AnnotationStatus[];
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