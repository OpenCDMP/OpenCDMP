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

@Injectable()
export class DescriptionFilterService {
 
  public static initializeReferenceLookup(): DescriptionReferenceLookup {
		const lookup = new DescriptionReferenceLookup();
		lookup.metadata = { countAll: true };
		lookup.isActive = [IsActive.Active];
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
		lookup.isActive = [IsActive.Active];
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
		lookup.isActive = [IsActive.Active];
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
		lookup.isActive = [IsActive.Active];
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
}