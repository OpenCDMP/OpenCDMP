import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection, ExtraFieldInSection, FieldInSection, ReferenceTypeFieldInSection, SystemFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { map, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class PlanBlueprintEditorResolver extends BaseEditorResolver {

	constructor(private planBlueprintService: PlanBlueprintService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<PlanBlueprint>(x => x.id),
			nameof<PlanBlueprint>(x => x.label),
			nameof<PlanBlueprint>(x => x.code),
			nameof<PlanBlueprint>(x => x.status),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.description)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.category)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.placeholder)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.description)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.required)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.semantics)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.ordinal)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<SystemFieldInSection>(x => x.systemFieldType)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ExtraFieldInSection>(x => x.dataType)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.code)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.multipleSelect)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.minMultiplicity)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.code)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSourcesEnabled)].join('.'),

			nameof<PlanBlueprint>(x => x.createdAt),
			nameof<PlanBlueprint>(x => x.hash),
			nameof<PlanBlueprint>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanBlueprintEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const cloneid = route.paramMap.get('cloneid');
		const newversion = route.paramMap.get("newversionid");
		if (id != null) {
			return this.planBlueprintService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		} else if (cloneid != null) {
			return this.planBlueprintService.clone(Guid.parse(cloneid), fields)
				.pipe(
					tap(x => this.breadcrumbService.addIdResolvedValue(cloneid, x.label)), 
					takeUntil(this._destroyed),
					map((blueprint: PlanBlueprint) => {
                        return {
                            ...blueprint,
                            id: null,
                            hash: null,
                            code: null,
                            isActive: IsActive.Active,
                            belongsToCurrentTenant: true
                        }
				}));
		} else if (newversion != null) {
			return this.planBlueprintService.getSingle(Guid.parse(newversion), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(newversion, x.label)), takeUntil(this._destroyed));
		}
	}
}
