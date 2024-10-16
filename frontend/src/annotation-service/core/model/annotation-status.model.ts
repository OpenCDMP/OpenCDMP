import { BaseEntity } from "@common/base/base-entity.model";
import { Annotation } from "./annotation.model";
import { Status } from "./status.model";
import { Guid } from "@common/types/guid";

export interface AnnotationStatus extends BaseEntity {
	annotation: Annotation;
	status: Status;
}

// persist

export interface AnnotationStatusPersist extends BaseEntity {
	annotationId: Guid;
	statusId: Guid;
}