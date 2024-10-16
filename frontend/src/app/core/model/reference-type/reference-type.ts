import { ReferenceFieldDataType } from "@app/core/common/enum/reference-field-data-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { ExternalFetcherBaseSourceConfiguration, ExternalFetcherBaseSourceConfigurationPersist } from "../external-fetcher/external-fetcher";

export interface ReferenceType extends BaseEntity{
	name?: string;
	code?: string;
	definition?: ReferenceTypeDefinition;
}

export interface ReferenceTypeDefinition{
    fields: ReferenceTypeField[];
	sources: ExternalFetcherBaseSourceConfiguration[];
}

export interface ReferenceTypeField {
	code: string;
	label: string;
	description: string;
	dataType: ReferenceFieldDataType;
}



// Persist

export interface ReferenceTypePersist extends BaseEntityPersist{
	name: string;
	code: string;
	definition: ReferenceTypeDefinitionPersist;
}

export interface ReferenceTypeDefinitionPersist{
    fields?: ReferenceTypeFieldPersist[];
	sources: ExternalFetcherBaseSourceConfigurationPersist[];
}

export interface ReferenceTypeFieldPersist {
	code: string;
	label: string;
	description: string;
	dataType: ReferenceFieldDataType;
}
