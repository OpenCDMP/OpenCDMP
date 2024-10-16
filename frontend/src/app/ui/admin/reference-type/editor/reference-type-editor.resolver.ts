import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthenticationConfiguration, ExternalFetcherBaseSourceConfiguration, QueryCaseConfig, QueryConfig, ResultFieldsMappingConfiguration, ResultsConfiguration, Static, StaticOption } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class ReferenceTypeEditorResolver extends BaseEditorResolver {

	constructor(private referenceTypeService: ReferenceTypeService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<ReferenceType>(x => x.id),
			nameof<ReferenceType>(x => x.name),
			nameof<ReferenceType>(x => x.code),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.description)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.dataType)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.type)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.key)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.ordinal)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.referenceTypeDependencies),nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.referenceTypeDependencies),nameof<ReferenceType>(x => x.name)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.url)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.resultsArrayPath)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.results), nameof<ResultsConfiguration>(x => x.fieldsMapping), nameof<ResultFieldsMappingConfiguration>(x => x.responsePath)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.paginationPath)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.contentType)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.firstPage)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.httpMethod)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.requestBody)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.filterType)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.enabled)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.authUrl)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.authMethod)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.authTokenPath)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.authRequestBody)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.auth),nameof<AuthenticationConfiguration>(x => x.type)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.name)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.defaultValue)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.likePattern)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.separator)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.value)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.referenceType)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.referenceType),nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.referenceType),nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.queries),nameof<QueryConfig>(x => x.cases),nameof<QueryCaseConfig>(x => x.referenceTypeSourceKey)].join('.'),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.items),nameof<Static>(x => x.options),nameof<StaticOption>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.items),nameof<Static>(x => x.options),nameof<StaticOption>(x => x.value)].join('.'),

			nameof<ReferenceType>(x => x.createdAt),
			nameof<ReferenceType>(x => x.updatedAt),
			nameof<ReferenceType>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...ReferenceTypeEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		if (id != null) {
			return this.referenceTypeService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.code)), takeUntil(this._destroyed));
		}
	}
}
