import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { User } from "../user/user";

export interface Tag extends BaseEntity {
	label?: string;
	createdBy?: User;
}

export interface TagPersist extends BaseEntityPersist {
	label?: string;
}