import { ExternalFetcherApiHTTPMethodType } from "@app/core/common/enum/external-fetcher-api-http-method-type";
import { ReferenceType } from "../reference-type/reference-type";
import { ExternalFetcherSourceType } from "@app/core/common/enum/external-fetcher-source-type";
import { Guid } from "@common/types/guid";
import { ExternalFetcherApiHeaderType } from "@app/core/common/enum/ExternalFetcherApiHeader.enum";

export interface ExternalFetcherBaseSourceConfiguration extends ExternalFetcherApiSourceConfiguration, ExternalFetcherStaticOptionSourceConfiguration {
	type: ExternalFetcherSourceType;
	key: string;
	label: string;
	ordinal: number;
	referenceTypeDependencies?: ReferenceType[];
}

export interface ExternalFetcherApiSourceConfiguration {
	url: string;
	results: ResultsConfiguration;
	paginationPath: string;
	contentType: string;
	firstPage: string;
	httpMethod: ExternalFetcherApiHTTPMethodType;
	requestBody?: string;
	filterType?: string;
	auth: AuthenticationConfiguration;
	queries?: QueryConfig[];
	headers?: HeaderConfig[];
}

export interface ResultsConfiguration {
	resultsArrayPath: string;
	fieldsMapping: ResultFieldsMappingConfiguration[];
}


export interface ResultFieldsMappingConfiguration {
	code: string;
	responsePath: string;
}

export interface AuthenticationConfiguration {
	enabled: boolean;
	authUrl: string;
	authMethod: ExternalFetcherApiHTTPMethodType;
	authTokenPath: string;
	authRequestBody: string;
	type: string;
}

export interface HeaderConfig {
	key: ExternalFetcherApiHeaderType;
	value: string;
}

export interface QueryConfig {
	name: string;
	defaultValue: string;
	cases: QueryCaseConfig[];
}

export interface QueryCaseConfig {
	likePattern: string,
	separator: string;
	value: string;
	referenceType?: ReferenceType;
	referenceTypeSourceKey: string
}

export interface ExternalFetcherStaticOptionSourceConfiguration {
	items: Static[];
}

export interface Static {
	options: StaticOption[];
}

export interface StaticOption {
	code: string;
	value: string;
}

//
// Persist
//

export interface ExternalFetcherBaseSourceConfigurationPersist extends ExternalFetcherApiSourceConfigurationPersist, ExternalFetcherStaticOptionSourceConfigurationPersist {
	type: ExternalFetcherSourceType;
	key: string;
	label: string;
	ordinal: number;
	referenceTypeDependencyIds?: Guid[];
}

export interface ExternalFetcherApiSourceConfigurationPersist {
	url: string;
	results: ResultsConfigurationPersist;
	paginationPath: string;
	contentType: string;
	firstPage: string;
	httpMethod: ExternalFetcherApiHTTPMethodType;
	requestBody?: string;
	filterType?: string;
	auth: AuthenticationConfigurationPersist;
	queries?: QueryConfigPersist[];
	headers?: HeadersConfigPersist[];
}

export interface ResultsConfigurationPersist {
	resultsArrayPath: string;
	fieldsMapping: ResultFieldsMappingConfigurationPersist[];
}


export interface ResultFieldsMappingConfigurationPersist {
	code: string;
	responsePath: string;
}

export interface AuthenticationConfigurationPersist {
	enabled: boolean;
	authUrl: string;
	authMethod: ExternalFetcherApiHTTPMethodType;
	authTokenPath: string;
	authRequestBody: string;
	type: string;
}

export interface HeadersConfigPersist {
	key: ExternalFetcherApiHeaderType;
	value: string;
}

export interface QueryConfigPersist {
	name: string;
	defaultValue: string;
	cases: QueryCaseConfigPersist[];
}

export interface QueryCaseConfigPersist {
	likePattern: string,
	separator: string;
	value: string;
	referenceTypeId: Guid;
	referenceTypeSourceKey: string
}

export interface ExternalFetcherStaticOptionSourceConfigurationPersist {
	items: StaticPersist[];
}

export interface StaticPersist {
	options: StaticOptionPersist[];
}

export interface StaticOptionPersist {
	code: string;
	value: string;
}