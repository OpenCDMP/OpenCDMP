import { BaseEntity } from "@common/base/base-entity.model";
import { User } from "../user/user";

export interface StorageFile extends BaseEntity {
	fileRef: string;
    name: string;
    fullName: string;
    extension: string;
    mimeType: string;
    purgeAt: Date;
    purgedAt: Date;
    owner: User
}