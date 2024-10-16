import { DescriptionTemplateTypeStatus } from "@app/core/common/enum/description-template-type-status";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface DescriptionTemplateType extends BaseEntity {
	name: string;
	code: string;
	status: DescriptionTemplateTypeStatus;
}

export interface DescriptionTemplateTypePersist extends BaseEntityPersist {
	name: string;
	code: string;
	status: DescriptionTemplateTypeStatus;
}