import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import { PlanStatus, PlanStatusDefinition, PlanStatusDefinitionAuthorization, PlanStatusDefinitionAuthorizationItem } from "@app/core/model/plan-status/plan-status";
import { PlanStatusService } from "@app/core/services/plan/plan-status.service";
import { BreadcrumbService } from "@app/ui/misc/breadcrumb/breadcrumb.service";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { Guid } from "@common/types/guid";
import { takeUntil, tap } from "rxjs";
import { nameof } from "ts-simple-nameof";

@Injectable()
export class PlanStatusEditorResolver extends BaseEditorResolver{

    constructor(private planStatusService: PlanStatusService, private breadcrumbService: BreadcrumbService) {
		super();
	}

    public static lookupFields(): string[] {
		return [
            nameof<PlanStatus>(x => x.id),
            nameof<PlanStatus>(x => x.name),
            nameof<PlanStatus>(x => x.description),
            nameof<PlanStatus>(x => x.internalStatus),
            nameof<PlanStatus>(x => x.definition),
            [nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization)].join('.'),
            [nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.roles)].join('.'),
            [nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.planRoles)].join('.'),
            [nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.allowAnonymous)].join('.'),
            [nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.allowAuthenticated)].join('.'),

            nameof<PlanStatus>(x => x.updatedAt),
            nameof<PlanStatus>(x => x.createdAt),
            nameof<PlanStatus>(x => x.hash),
            nameof<PlanStatus>(x => x.isActive),
            nameof<PlanStatus>(x => x.belongsToCurrentTenant),
        ]
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const fields = [
			...PlanStatusEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		if (id != null) {
			return this.planStatusService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.name)), takeUntil(this._destroyed));
		}
    }

}