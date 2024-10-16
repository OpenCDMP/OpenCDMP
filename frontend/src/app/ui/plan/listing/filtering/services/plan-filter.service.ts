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

@Injectable()
export class PlanFilterService {

  
	public static initializePlanDescriptionTemplateLookup(): PlanDescriptionTemplateLookup {
		const lookup = new PlanDescriptionTemplateLookup();
		lookup.metadata = { countAll: true };
		lookup.isActive = [IsActive.Active];
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
		lookup.isActive = [IsActive.Active];
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
		lookup.isActive = [IsActive.Active];
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
}