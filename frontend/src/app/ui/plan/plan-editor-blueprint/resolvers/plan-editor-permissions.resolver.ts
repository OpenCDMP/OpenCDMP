import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionSectionPermissionResolver } from '@app/core/model/description/description';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { mergeMap, takeUntil, tap } from 'rxjs/operators';
import { Plan } from '@app/core/model/plan/plan';
import { nameof } from 'ts-simple-nameof';
import { PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';

@Injectable()
export class PlanEditorPermissionsResolver extends BaseEditorResolver {

	constructor(private planService: PlanService, 
		private descriptionService: DescriptionService,
	) {
		super();
	}

	public static permissionLookupFields(): string[] {
		return [
			nameof<Plan>(x => x.id),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanEditorPermissionsResolver.permissionLookupFields()
		];
		const id = route.paramMap.get('id');
		if (id != null) {
			return this.planService
				.getSingle(Guid.parse(id), fields)
				.pipe(mergeMap( data => {
					let descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
						planId: data.id,
						sectionIds: data?.blueprint?.definition?.sections?.map(x => x.id),
						permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.AnnotatePlan]
					};
					return this.descriptionService.getDescriptionSectionPermissions(descriptionSectionPermissionResolverModel).pipe(takeUntil(this._destroyed));
				}
			));
		}
	}
}
