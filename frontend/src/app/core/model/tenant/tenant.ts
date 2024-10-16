import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface Tenant extends BaseEntity{
	name?: string;
	code?: string;
	description?: string;
}

//persist

export interface TenantPersist extends BaseEntityPersist{
	name: string;
	code: string;
	description: string;
}

