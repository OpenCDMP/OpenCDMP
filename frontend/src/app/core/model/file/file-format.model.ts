import { FileTransformerEntityType } from "@app/core/common/enum/file-transformer-entity-type";

export interface RepositoryFileFormat {
	format: string;
	hasLogo: boolean;
	icon: string;
	repositoryId: string;
	entityTypes: FileTransformerEntityType[];
}
