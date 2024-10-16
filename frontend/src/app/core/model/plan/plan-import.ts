import { Guid } from "@common/types/guid";
import { DescriptionCommonModelConfig, PreprocessingDescriptionModel } from "../description/description-import";

// preprocessing
export interface PreprocessingPlanModel {
	label: string;
	blueprintId: Guid;
	preprocessingDescriptionModels: PreprocessingDescriptionModel[];
}

// common config
export interface PlanCommonModelConfig {
	fileId: Guid;
	label: string;
	blueprintId: Guid;
    repositoryId: string;
	descriptions: DescriptionCommonModelConfig[];
}