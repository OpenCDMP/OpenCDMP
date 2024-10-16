import { Guid } from "@common/types/guid";

//preprocessing
export interface PreprocessingDescriptionModel {
	id: string;
	label: string;
}

// common config
export interface DescriptionCommonModelConfig {
	id: string;
	sectionId: Guid;
    templateId: Guid;
}