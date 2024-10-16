import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { ExternalFetcherBaseSourceConfiguration, ExternalFetcherBaseSourceConfigurationPersist} from "../external-fetcher/external-fetcher";

export interface PrefillingSource extends BaseEntity{
	label: string;
	code: string;
	definition: PrefillingSourceDefinition;
}

export interface PrefillingSourceDefinition{
    fields: PrefillingSourceDefinitionField[];
	fixedValueFields: PrefillingSourceDefinitionFixedValueField[];
	searchConfiguration: ExternalFetcherBaseSourceConfiguration;
	getConfiguration: ExternalFetcherBaseSourceConfiguration;
}

export interface PrefillingSourceDefinitionField {
	code: string;
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;
}

export interface PrefillingSourceDefinitionFixedValueField {
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;
	fixedValue: string;
}

export interface Prefilling {
	id: string,
	label: string,
	key: string,
	tag: string
	data: Map<string, string>
}

// Persist

export interface PrefillingSourcePersist extends BaseEntityPersist{
	label: string;
	code: string;
	definition: PrefillingSourceDefinitionPersist;
}

export interface PrefillingSourceDefinitionPersist{
    fields: PrefillingSourceDefinitionFieldPersist[];
	fixedValueFields: PrefillingSourceDefinitionFixedValueFieldPersist[];
	searchConfiguration: ExternalFetcherBaseSourceConfigurationPersist;
	getConfiguration: ExternalFetcherBaseSourceConfigurationPersist;
}

export interface PrefillingSourceDefinitionFieldPersist {
	code: string;
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;
}

export interface PrefillingSourceDefinitionFixedValueFieldPersist {
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;
	fixedValue: string;
}
