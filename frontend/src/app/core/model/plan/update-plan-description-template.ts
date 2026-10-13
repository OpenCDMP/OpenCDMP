import { Guid } from "@common/types/guid";
import { PlanDescriptionTemplatePersist } from "./plan";

// preprocessing
export interface UpdatePlanDescriptionTemplate  {
    id: Guid;
    descriptionTemplates: PlanDescriptionTemplatePersist[];
    hash: string;
}