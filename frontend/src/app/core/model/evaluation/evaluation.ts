import { EntityType } from "@app/core/common/enum/entity-type";
import { BaseEntity } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";
import { EvaluationStatus } from "./evaluation-status";
import { EvaluationData } from "./evaluation-data";

export interface Evaluation extends BaseEntity{
    entityType: EntityType;
    entityId: Guid;
    evaluatedAt: Date;
    status: EvaluationStatus;
    createdById: Guid;
    data: EvaluationData;
}