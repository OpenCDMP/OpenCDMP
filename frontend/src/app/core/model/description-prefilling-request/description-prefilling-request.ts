import { Guid } from "@common/types/guid";

export interface PrefillingSearchRequest {
	like: string;
	prefillingSourceId: Guid;
}

export interface DescriptionPrefillingRequest {
	prefillingSourceId: Guid;
	descriptionTemplateId: Guid;
	data: DescriptionPrefillingRequestData;
	project: string[];
}

export interface DescriptionPrefillingRequestData {
    data: Map<string, string>;
	id: string;
}
