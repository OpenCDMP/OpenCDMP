import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface Language extends BaseEntity{
	code: string;
    payload: string;
    ordinal: number;
}

// Persist

export interface LanguagePersist extends BaseEntityPersist{
	code: string;
    payload: string;
    ordinal: number;
}