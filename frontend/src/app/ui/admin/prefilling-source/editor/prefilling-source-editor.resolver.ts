import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthenticationConfiguration, ExternalFetcherBaseSourceConfiguration, QueryCaseConfig, QueryConfig, ResultFieldsMappingConfiguration, ResultsConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { PrefillingSource, PrefillingSourceDefinition, PrefillingSourceDefinitionField, PrefillingSourceDefinitionFixedValueField } from '@app/core/model/prefilling-source/prefilling-source';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class PrefillingSourceEditorResolver extends BaseEditorResolver {

	constructor(private prefillingSourceService: PrefillingSourceService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<PrefillingSource>(x => x.id),
			nameof<PrefillingSource>(x => x.label),
			nameof<PrefillingSource>(x => x.code),

			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fields), nameof<PrefillingSourceDefinitionField>(x => x.code)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fields), nameof<PrefillingSourceDefinitionField>(x => x.systemFieldTarget)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fields), nameof<PrefillingSourceDefinitionField>(x => x.semanticTarget)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fields), nameof<PrefillingSourceDefinitionField>(x => x.trimRegex)].join('.'),

			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fixedValueFields), nameof<PrefillingSourceDefinitionFixedValueField>(x => x.systemFieldTarget)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fixedValueFields), nameof<PrefillingSourceDefinitionFixedValueField>(x => x.semanticTarget)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fixedValueFields), nameof<PrefillingSourceDefinitionFixedValueField>(x => x.trimRegex)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.fixedValueFields), nameof<PrefillingSourceDefinitionFixedValueField>(x => x.fixedValue)].join('.'),

			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.type)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.key)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.label)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.ordinal)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.url)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.resultsArrayPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.code)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.responsePath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.paginationPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.contentType)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.firstPage)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.httpMethod)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.requestBody)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.filterType)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.enabled)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authUrl)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authMethod)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authTokenPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authRequestBody)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.type)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.name)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.defaultValue)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.likePattern)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.separator)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.searchConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.value)].join('.'),

			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.type)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.key)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.label)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.ordinal)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.url)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.resultsArrayPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.code)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.responsePath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.paginationPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.contentType)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.firstPage)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.httpMethod)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.requestBody)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.filterType)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.enabled)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authUrl)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authMethod)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authTokenPath)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.authRequestBody)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth), nameof<AuthenticationConfiguration>(x => x.type)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.name)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.defaultValue)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.likePattern)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.separator)].join('.'),
			[nameof<PrefillingSource>(x => x.definition), nameof<PrefillingSourceDefinition>(x => x.getConfiguration), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries), nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.value)].join('.'),

			nameof<PrefillingSource>(x => x.createdAt),
			nameof<PrefillingSource>(x => x.updatedAt),
			nameof<PrefillingSource>(x => x.hash),
			nameof<PrefillingSource>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PrefillingSourceEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.prefillingSourceService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		}
	}
}
