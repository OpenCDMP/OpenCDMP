import { SupportiveMaterialFieldType } from "@app/core/common/enum/supportive-material-field-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface SupportiveMaterial extends BaseEntity {
	type: SupportiveMaterialFieldType;
	languageCode: string;
	payload: string;
}


export interface SupportiveMaterialPersist extends BaseEntityPersist {
	type: SupportiveMaterialFieldType;
	languageCode: string;
	payload: string;
}