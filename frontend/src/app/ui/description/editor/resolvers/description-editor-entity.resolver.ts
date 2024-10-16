import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description, DescriptionExternalIdentifier, DescriptionField, DescriptionPropertyDefinition, DescriptionPropertyDefinitionFieldSet, DescriptionPropertyDefinitionFieldSetItem, DescriptionReference, DescriptionReferenceData, DescriptionTag } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { Tag } from '@app/core/model/tag/tag';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { concatMap, map, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class DescriptionEditorEntityResolver extends BaseEditorResolver {

	constructor(
		private descriptionService: DescriptionService,
		private breadcrumbService: BreadcrumbService,
		private language: TranslateService,
		private planService: PlanService
	) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...DescriptionEditorEntityResolver.descriptionLookupFields(),
			...DescriptionEditorEntityResolver.planLookupFields(nameof<Description>(x => x.plan)),
			...DescriptionEditorEntityResolver.descriptionTemplateLookupFieldsForDescrption(),
		]
	}

	public static cloneLookupFields(): string[] {
		return [
			...DescriptionEditorEntityResolver.descriptionLookupFields(),
			...DescriptionEditorEntityResolver.descriptionTemplateLookupFieldsForDescrption(),
		]
	}

	public static descriptionLookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Description>(x => x.id),
			nameof<Description>(x => x.label),
			nameof<Description>(x => x.status),
			nameof<Description>(x => x.description),
			nameof<Description>(x => x.status),

			[nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.FinalizeDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.AnnotateDescription].join('.'),

			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.comment)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.ordinal)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.textValue)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.textListValue)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.dateValue)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.booleanValue)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.identifier)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.type)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.id)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.label)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.hash)].join('.'),

			[nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.id),].join('.'),
			[nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.tag), nameof<Tag>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.isActive)].join('.'),

			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.fieldId)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.ordinal)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.sourceType)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.isActive)].join('.'),

			nameof<Description>(x => x.createdAt),
			nameof<Description>(x => x.hash),
			nameof<Description>(x => x.isActive)
		]
	}

	public static descriptionTemplateLookupFieldsForDescrption(): string[] {
		return [
			"DescriptionEditorDescriptionTemplateForDescriptionLookupFields"
		]
	}

	public static descriptionTemplateLookupFields(): string[] {
		return [
			"DescriptionEditorDescriptionTemplateLookupFields"
		]
	}

	public static planLookupFields(prefix?: string): string[] {
		return [
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.label)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.status)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.isActive)].join('.'),

			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),

			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.isActive)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSourcesEnabled)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.id)].join('.'),

			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.version)].join('.'),

			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...DescriptionEditorEntityResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const planId = route.paramMap.get('planId');
		const planSectionId = route.paramMap.get('planSectionId');
		const copyPlanId = route.paramMap.get('copyPlanId');
		const fieldsetId = route.paramMap.get('fieldsetId');
		if (id != null && copyPlanId == null && planSectionId == null) {
			if (fieldsetId != null) {
				this.breadcrumbService.addExcludedParam(fieldsetId, true);
				this.breadcrumbService.addExcludedParam('f', true);
				this.breadcrumbService.addExcludedParam('annotation', true);
			}
			return this.descriptionService.getSingle(Guid.parse(id), fields).pipe(tap(d => this.breadcrumbService.addIdResolvedValue(d.id.toString(), d.label)));
		} else if (planId != null && planSectionId != null && copyPlanId == null) {
			return this.planService.getSingle(Guid.parse(planId), DescriptionEditorEntityResolver.planLookupFields())
				.pipe(tap(x => {
					this.breadcrumbService.addExcludedParam(planId, true);
					this.breadcrumbService.addIdResolvedValue(planSectionId, this.language.instant("DESCRIPTION-EDITOR.TITLE-NEW"));
				}), takeUntil(this._destroyed), map(plan => {
					const description: Description = {};
					description.plan = plan;
					description.planDescriptionTemplate = {
						sectionId: Guid.parse(planSectionId)
					}
					return description;
				}));
		} else if (copyPlanId != null && id != null && planSectionId != null) {
			return this.planService.getSingle(Guid.parse(copyPlanId), DescriptionEditorEntityResolver.planLookupFields()).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed), concatMap(plan => {
				return this.descriptionService.getSingle(Guid.parse(id), DescriptionEditorEntityResolver.cloneLookupFields())
				.pipe(tap(x => {
					this.breadcrumbService.addExcludedParam(copyPlanId, true)
					this.breadcrumbService.addExcludedParam(planSectionId, true)
					this.breadcrumbService.addIdResolvedValue(id, x.label)
				}), takeUntil(this._destroyed), map(description => {

					description.id = null;
					description.hash = null;
					description.isActive = IsActive.Active;
                    description.belongsToCurrentTenant = true;
					description.status = DescriptionStatusEnum.Draft;
					description.plan = plan;
					description.planDescriptionTemplate = {
						id: plan.planDescriptionTemplates.filter(x => x.sectionId == Guid.parse(planSectionId) && x.descriptionTemplateGroupId == description.descriptionTemplate.groupId)[0].id,
						sectionId: Guid.parse(planSectionId)
					}
					return description;
				}));
			}));
		}
	}
}
