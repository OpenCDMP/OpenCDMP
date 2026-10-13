import { Injectable } from "@angular/core";
import { PlanBlueprint } from "@app/core/model/plan-blueprint/plan-blueprint";
import { Plan, PlanDescriptionTemplate, PlanUser } from "@app/core/model/plan/plan";
import { Reference } from "@app/core/model/reference/reference";
import { Tenant } from "@app/core/model/tenant/tenant";
import { PlanBlueprintLookup } from "@app/core/query/plan-blueprint.lookup";
import { PlanDescriptionTemplateLookup } from "@app/core/query/plan-description-template.lookup";
import { PlanUserLookup } from "@app/core/query/plan-user.lookup";
import { PlanReferenceLookup } from "@app/core/query/reference.lookup";
import { TenantLookup } from "@app/core/query/tenant.lookup";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { nameof } from "ts-simple-nameof";
import { PlanLookup } from "@app/core/query/plan.lookup";
import { ReferencesWithType } from "@app/core/query/description.lookup";
import { Guid } from "@common/types/guid";
import { IsActive as AppIsActive } from "@app/core/common/enum/is-active.enum";
import { PlanListingFilters } from "../plan-filter.component";

/** What turning panel filters into a lookup needs from the page that hosts the panel. */
export interface FilterLookupContext {
	/** Code of the tenant for the "only this tenant" filter; null when unavailable. */
	tenantCode?: string;
	/** The signed-in user, for the "my role" filters. */
	principalId?: Guid;
}

@Injectable()
export class PlanFilterService {

  
	public static initializePlanDescriptionTemplateLookup(): PlanDescriptionTemplateLookup {
		const lookup = new PlanDescriptionTemplateLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			]
		};

		return lookup;
	}

	public static initializePlanBlueprintLookup(): PlanBlueprintLookup {
		const lookup = new PlanBlueprintLookup();
		lookup.metadata = { countAll: true };
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			]
		};

		return lookup;
	}

	public static initializePlanUserLookup(): PlanUserLookup {
		const lookup = new PlanUserLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			]
		};

		return lookup;
	}
	
	public static initializePlanReferenceLookup(): PlanReferenceLookup {
		const lookup = new PlanReferenceLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<Plan>(x => x.planReferences), nameof<Reference>(x => x.id)].join('.'),
			]
		};

		return lookup;
	}
	
	public static initializeTenantLookup(): TenantLookup {
		const lookup = new TenantLookup();
		lookup.metadata = { countAll: true };
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Tenant>(x => x.code),
			]
		};

		return lookup;
	}

	/**
	 * Applies the filter panel's values to a plan lookup (My Plans, and the dashboard's Plans tab).
	 * Only the filter parts are touched; paging, order and projection stay as they are.
	 */
	public static patchLookup(lookup: PlanLookup, filters: PlanListingFilters, context: FilterLookupContext = {}): PlanLookup {
		lookup.statusIds = filters?.statusId != null ? [filters.statusId] : null;
		lookup.isActive = filters?.isActive ? [AppIsActive.Active] : [AppIsActive.Inactive];

		if (filters?.viewOnlyTenant && context.tenantCode) {
			lookup.tenantSubQuery = PlanFilterService.initializeTenantLookup();
			lookup.tenantSubQuery.codes = [context.tenantCode];
		} else lookup.tenantSubQuery = null;

		const descriptionTemplates = filters?.descriptionTemplates ?? null;
		if (descriptionTemplates?.length) {
			lookup.planDescriptionTemplateSubQuery = PlanFilterService.initializePlanDescriptionTemplateLookup();
			lookup.planDescriptionTemplateSubQuery.isActive = filters.isActive == true ? [AppIsActive.Active] : [AppIsActive.Inactive];
			lookup.planDescriptionTemplateSubQuery.descriptionTemplateGroupIds = descriptionTemplates;
		} else lookup.planDescriptionTemplateSubQuery = null;

		const planBlueprints = filters?.planBlueprints ?? null;
		const typeIds = filters?.typeIds ?? null;
		if (planBlueprints?.length || typeIds?.length) {
			lookup.planBlueprintSubQuery = PlanFilterService.initializePlanBlueprintLookup();
			if (planBlueprints?.length) lookup.planBlueprintSubQuery.groupIds = planBlueprints;
			if (typeIds?.length) lookup.planBlueprintSubQuery.typeIds = typeIds;
		} else lookup.planBlueprintSubQuery = null;

		const roles = filters?.roles ?? null;
		if (roles?.length) {
			lookup.planUserSubQuery = PlanFilterService.initializePlanUserLookup();
			lookup.planUserSubQuery.userRoles = roles;
			lookup.planUserSubQuery.isActive = filters.isActive == true ? [AppIsActive.Active] : [AppIsActive.Inactive];
			if (context.principalId) lookup.planUserSubQuery.userIds = [context.principalId];
		} else lookup.planUserSubQuery = null;

		PlanFilterService.setReferenceIds(lookup, PlanFilterService.flattenReferenceIds(filters?.references), filters?.isActive != false);
		return lookup;
	}

	/** Replaces the lookup's reference filter; an empty list removes it. */
	public static setReferenceIds(lookup: PlanLookup, referenceIds: Guid[], active: boolean = true): void {
		if (referenceIds?.length) {
			lookup.planReferenceSubQuery = lookup.planReferenceSubQuery ?? PlanFilterService.initializePlanReferenceLookup();
			lookup.planReferenceSubQuery.isActive = active ? [AppIsActive.Active] : [AppIsActive.Inactive];
			lookup.planReferenceSubQuery.referenceIds = referenceIds;
		} else lookup.planReferenceSubQuery = null;
	}

	/** The panel's values as they are stored in a lookup, for opening the panel again. */
	public static buildFilters(lookup: PlanLookup, references: ReferencesWithType[]): PlanListingFilters {
		return {
			statusId: lookup?.statusIds?.[0] ?? null,
			isActive: (lookup?.isActive?.[0] ?? AppIsActive.Active) == AppIsActive.Active,
			descriptionTemplates: lookup?.planDescriptionTemplateSubQuery?.descriptionTemplateGroupIds ?? [],
			planBlueprints: lookup?.planBlueprintSubQuery?.groupIds ?? [],
			typeIds: lookup?.planBlueprintSubQuery?.typeIds ?? [],
			roles: lookup?.planUserSubQuery?.userRoles ?? null,
			references: references ?? [],
			viewOnlyTenant: lookup?.tenantSubQuery?.codes?.length > 0
		};
	}

	public static countFilters(lookup: PlanLookup): number {
		let count = 0;
		if (lookup?.statusIds) count += lookup.statusIds.length;
		if (lookup?.isActive?.[0] == AppIsActive.Inactive) count += lookup.isActive.length;
		if (lookup?.tenantSubQuery) count += 1;
		if (lookup?.planDescriptionTemplateSubQuery) count += lookup.planDescriptionTemplateSubQuery.descriptionTemplateGroupIds?.length ?? 0;
		if (lookup?.planBlueprintSubQuery) count += (lookup.planBlueprintSubQuery.groupIds?.length ?? 0) + (lookup.planBlueprintSubQuery.typeIds?.length ?? 0);
		if (lookup?.planUserSubQuery) count += lookup.planUserSubQuery.userRoles?.length ?? 0;
		if (lookup?.planReferenceSubQuery) count += lookup.planReferenceSubQuery.referenceIds?.length ?? 0;
		return count;
	}

	public static flattenReferenceIds(references: ReferencesWithType[]): Guid[] {
		return (references ?? []).flatMap(x => x?.referenceIds ?? []).filter(x => x != null);
	}
}
