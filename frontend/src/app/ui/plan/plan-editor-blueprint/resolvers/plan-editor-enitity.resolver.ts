import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection, ExtraFieldInSection, FieldInSection, ReferenceTypeFieldInSection, SystemFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanBlueprintValue, PlanContact, PlanDescriptionTemplate, PlanProperties, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference, PlanReferenceData } from '@app/core/model/plan/plan-reference';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceType, ReferenceTypeDefinition } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';

@Injectable()
export class PlanEditorEntityResolver extends BaseEditorResolver {

	constructor(private descriptionService: PlanService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Plan>(x => x.id),
			nameof<Plan>(x => x.label),
			nameof<Plan>(x => x.status),
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.description),
			nameof<Plan>(x => x.language),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.isActive),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.publicAfter),
			nameof<Plan>(x => x.creator),
			nameof<Plan>(x => x.hash),

			[nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),

			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.planBlueprintValues), nameof<PlanBlueprintValue>(x => x.fieldId)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.planBlueprintValues), nameof<PlanBlueprintValue>(x => x.fieldValue)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.planBlueprintValues), nameof<PlanBlueprintValue>(x => x.dateValue)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.planBlueprintValues), nameof<PlanBlueprintValue>(x => x.numberValue)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts), nameof<PlanContact>(x => x.firstName)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts), nameof<PlanContact>(x => x.lastName)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts), nameof<PlanContact>(x => x.email)].join('.'),


			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.data), nameof<PlanReferenceData>(x => x.blueprintFieldId)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.sourceType)].join('.'),


			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.isActive)].join('.'),

			...PlanEditorEntityResolver.blueprintLookupFields(nameof<Plan>(x => x.blueprint)),

		]
	}

	public static blueprintLookupFields(prefix?: string): string[] {
		return [
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.description)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.minMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.category)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.label)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.placeholder)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.description)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.required)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.ordinal)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ExtraFieldInSection>(x => x.dataType)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<SystemFieldInSection>(x => x.systemFieldType)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.code)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.multipleSelect)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x=> x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x=> x.referenceTypeDependencies) , nameof<ReferenceType>(x => x.id)].join('.'),
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanEditorEntityResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const fieldId = route.paramMap.get('fieldId');
		if (id != null) {
			return this.descriptionService.getSingle(Guid.parse(id), fields).pipe(tap(x => {
				if (fieldId) {
					this.breadcrumbService.addExcludedParam(fieldId, true);
					this.breadcrumbService.addExcludedParam('f', true);
					this.breadcrumbService.addExcludedParam('annotation', true);
				}
				this.breadcrumbService.addIdResolvedValue(id, x.label)
			}), takeUntil(this._destroyed));
		}
	}
}
