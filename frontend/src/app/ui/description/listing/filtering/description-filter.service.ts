import { Injectable } from "@angular/core";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { Description, DescriptionReference, DescriptionTag } from "@app/core/model/description/description";
import { Plan, PlanUser } from "@app/core/model/plan/plan";
import { Reference } from "@app/core/model/reference/reference";
import { Tag } from "@app/core/model/tag/tag";
import { Tenant } from "@app/core/model/tenant/tenant";
import { DescriptionTemplateLookup } from "@app/core/query/description-template.lookup";
import { PlanUserLookup } from "@app/core/query/plan-user.lookup";
import { PlanLookup } from "@app/core/query/plan.lookup";
import { DescriptionReferenceLookup } from "@app/core/query/reference.lookup";
import { DescriptionTagLookup } from "@app/core/query/tag.lookup";
import { TenantLookup } from "@app/core/query/tenant.lookup";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { nameof } from "ts-simple-nameof";

import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { IsActive as AppIsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplate as DescriptionTemplateModel } from '@app/core/model/description-template/description-template';
import { Guid } from '@common/types/guid';
import { DescriptionListingFilters } from './description-filter.component';
import { FilterLookupContext } from '@app/ui/plan/listing/filtering/services/plan-filter.service';

@Injectable()
export class DescriptionFilterService {
 
  public static initializeReferenceLookup(): DescriptionReferenceLookup {
		const lookup = new DescriptionReferenceLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			]
		};

		return lookup;
	}
	
	public static initializeTagLookup(): DescriptionTagLookup {
		const lookup = new DescriptionTagLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<DescriptionTag>(x => x.tag), nameof<Tag>(x => x.id)].join('.'),
			]
		};

		return lookup;
	}
	
	public static initializePlanLookup(): PlanLookup {
		const lookup = new PlanLookup();
		lookup.metadata = { countAll: true };
		lookup.project = {
			fields: [
				[nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
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

	public static initializeDescriptionTemplateLookup(): DescriptionTemplateLookup {
		const lookup = new DescriptionTemplateLookup();
		lookup.metadata = { countAll: true };
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
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
	 * Applies the filter panel's values to a description lookup (My Assets, and the dashboard's Assets tab).
	 * Only the filter parts are touched; paging, order and projection stay as they are.
	 */
	public static patchLookup(lookup: DescriptionLookup, filters: DescriptionListingFilters, context: FilterLookupContext = {}): DescriptionLookup {
		lookup.statusIds = filters?.statusId != null ? [filters.statusId] : null;
		lookup.isActive = filters?.isActive ? [AppIsActive.Active] : [AppIsActive.Inactive];

		if (filters?.viewOnlyTenant && context.tenantCode) {
			lookup.tenantSubQuery = DescriptionFilterService.initializeTenantLookup();
			lookup.tenantSubQuery.codes = [context.tenantCode];
		} else lookup.tenantSubQuery = null;

		if (filters?.descriptionTemplates?.length || filters?.descriptionTemplateTypes?.length) {
			lookup.descriptionTemplateSubQuery = DescriptionFilterService.initializeDescriptionTemplateLookup();
			lookup.descriptionTemplateSubQuery.project = {
				fields: [
					nameof<DescriptionTemplateModel>(x => x.id),
					nameof<DescriptionTemplateModel>(x => x.type),
					[nameof<DescriptionTemplateModel>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.')]
			};
			lookup.descriptionTemplateSubQuery.groupIds = filters?.descriptionTemplates?.length ? filters.descriptionTemplates : null;
			lookup.descriptionTemplateSubQuery.typeIds = filters?.descriptionTemplateTypes?.length ? filters.descriptionTemplateTypes : null;
		} else lookup.descriptionTemplateSubQuery = null;

		if (filters?.roles?.length || filters?.associatedPlanIds?.length) {
			lookup.planSubQuery = DescriptionFilterService.initializePlanLookup();
			lookup.planSubQuery.isActive = filters.isActive == true ? [AppIsActive.Active] : [AppIsActive.Active, AppIsActive.Inactive];
			if (filters?.associatedPlanIds?.length) lookup.planSubQuery.ids = filters.associatedPlanIds;
			if (filters?.roles?.length) {
				lookup.planSubQuery.planUserSubQuery = DescriptionFilterService.initializePlanUserLookup();
				lookup.planSubQuery.planUserSubQuery.isActive = filters.isActive == true ? [AppIsActive.Active] : [AppIsActive.Active, AppIsActive.Inactive];
				if (context.principalId) lookup.planSubQuery.planUserSubQuery.userIds = [context.principalId];
				lookup.planSubQuery.planUserSubQuery.userRoles = filters.roles;
			}
		} else lookup.planSubQuery = null;

		if (filters?.tags?.length) {
			lookup.descriptionTagSubQuery = DescriptionFilterService.initializeTagLookup();
			lookup.descriptionTagSubQuery.isActive = filters.isActive == true ? [AppIsActive.Active] : [AppIsActive.Inactive];
			lookup.descriptionTagSubQuery.tagIds = filters.tags;
		} else lookup.descriptionTagSubQuery = null;

		DescriptionFilterService.setReferenceIds(lookup, (filters?.references ?? []).flatMap(x => x?.referenceIds ?? []).filter(x => x != null), filters?.isActive != false);
		return lookup;
	}

	/** Replaces the lookup's reference filter; an empty list removes it. */
	public static setReferenceIds(lookup: DescriptionLookup, referenceIds: Guid[], active: boolean = true): void {
		if (referenceIds?.length) {
			lookup.descriptionReferenceSubQuery = lookup.descriptionReferenceSubQuery ?? DescriptionFilterService.initializeReferenceLookup();
			lookup.descriptionReferenceSubQuery.isActive = active ? [AppIsActive.Active] : [AppIsActive.Inactive];
			lookup.descriptionReferenceSubQuery.referenceIds = referenceIds;
		} else lookup.descriptionReferenceSubQuery = null;
	}

	/** The panel's values as they are stored in a lookup, for opening the panel again. */
	public static buildFilters(lookup: DescriptionLookup, references: ReferencesWithType[]): DescriptionListingFilters {
		return {
			statusId: lookup?.statusIds?.[0] ?? null,
			isActive: (lookup?.isActive?.[0] ?? AppIsActive.Active) == AppIsActive.Active,
			viewOnlyTenant: lookup?.tenantSubQuery?.codes?.length > 0,
			roles: lookup?.planSubQuery?.planUserSubQuery?.userRoles ?? null,
			descriptionTemplates: lookup?.descriptionTemplateSubQuery?.groupIds ?? [],
			descriptionTemplateTypes: lookup?.descriptionTemplateSubQuery?.typeIds ?? [],
			associatedPlanIds: lookup?.planSubQuery?.ids ?? [],
			tags: lookup?.descriptionTagSubQuery?.tagIds ?? [],
			references: references ?? []
		};
	}

	public static countFilters(lookup: DescriptionLookup): number {
		let count = 0;
		if (lookup?.statusIds) count += lookup.statusIds.length;
		if (lookup?.isActive?.[0] == AppIsActive.Inactive) count += lookup.isActive.length;
		if (lookup?.tenantSubQuery) count += 1;
		if (lookup?.descriptionTemplateSubQuery) count += (lookup.descriptionTemplateSubQuery.groupIds?.length ?? 0) + (lookup.descriptionTemplateSubQuery.typeIds?.length ?? 0);
		if (lookup?.descriptionTagSubQuery) count += lookup.descriptionTagSubQuery.tagIds?.length ?? 0;
		if (lookup?.planSubQuery) count += (lookup.planSubQuery.ids?.length ?? 0) + (lookup.planSubQuery.planUserSubQuery?.userRoles?.length ?? 0);
		if (lookup?.descriptionReferenceSubQuery) count += lookup.descriptionReferenceSubQuery.referenceIds?.length ?? 0;
		return count;
	}
}
