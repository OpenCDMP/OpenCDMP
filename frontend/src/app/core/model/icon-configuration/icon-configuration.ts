import { Guid } from "@common/types/guid";
import { StorageFile } from "../storage-file/storage-file";

export interface IconConfiguration {
    matIconName: string;
    storageFile: StorageFile;
}

export interface IconConfigurationPersist {
    matIconName: string;
    storageFileId: Guid;
}