import { ReferenceFieldDataType } from "@app/core/common/enum/reference-field-data-type";
import { ReferenceSourceType } from "@app/core/common/enum/reference-source-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { ReferenceType } from "../reference-type/reference-type";
import { Guid } from "@common/types/guid";

export interface Reference extends BaseEntity {
	label: string;
	type: ReferenceType;
	description: string;
	definition: Definition;
	reference: string;
	abbreviation: string;
	source: string;
	sourceType: ReferenceSourceType;
}

export interface BaseReference extends BaseEntity{
	label: string;
	type: ReferenceType;
	description?: string;
	reference?: string;
}


export interface Definition {
	fields: Field[];
}

export interface Field {
	code: string;
	dataType: ReferenceFieldDataType;
	value: string;
}

// old fetcher
export interface FetcherReference {
	id: string;
	name: string;
	pid: string;
	pidTypeField: string;
	uri: string;
	description: string;
	source: string;
	count: string;
	path: string;
	host: string;
	types: string;
	firstName: string;
	lastName: string;
	tag: string;
}


// Persist

export interface ReferencePersist extends BaseEntityPersist {
	label: string;
	typeId: Guid;
	description: string;
	definition: DefinitionPersist;
	reference: string;
	abbreviation: string;
	source: string;
	sourceType: ReferenceSourceType;
}

export interface DefinitionPersist {
	fields: FieldPersist[];
}

export interface FieldPersist {
	code: string;
	dataType: ReferenceFieldDataType;
	value: string;
}