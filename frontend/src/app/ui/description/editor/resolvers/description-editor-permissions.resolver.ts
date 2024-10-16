import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Description, DescriptionSectionPermissionResolver } from '@app/core/model/description/description';
import { DescriptionService } from '@app/core/services/description/description.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { mergeMap, takeUntil, tap } from 'rxjs/operators';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { nameof } from 'ts-simple-nameof';
import { Plan, PlanDescriptionTemplate } from '@app/core/model/plan/plan';

@Injectable()
export class DescriptionEditorPermissionsResolver extends BaseEditorResolver {

	constructor(
		private descriptionService: DescriptionService,
	) {
		super();
	}

	public static permissionLookupFields(): string[] {
		return [
			nameof<Description>(x => x.id),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...DescriptionEditorPermissionsResolver.permissionLookupFields()
		];
		const id = route.paramMap.get('id');
		const planId = route.paramMap.get('planId');
		const planSectionId = route.paramMap.get('planSectionId');
		const copyPlanId = route.paramMap.get('copyPlanId');
		// const cloneid = route.paramMap.get('cloneid');
		if (id != null && copyPlanId == null && planSectionId == null) {
			return this.descriptionService.getSingle(Guid.parse(id), fields)				
			.pipe(mergeMap(description => {
					const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
						planId: description.plan.id,
						sectionIds: [description.planDescriptionTemplate.sectionId],
						permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription]
					}
					return this.descriptionService.getDescriptionSectionPermissions(descriptionSectionPermissionResolverModel).pipe(takeUntil(this._destroyed));
				}));

		} else if (planId != null && planSectionId != null && copyPlanId == null) {

			const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
				planId: Guid.parse(planId),
				sectionIds: [Guid.parse(planSectionId)],
				permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription]
			}
			return this.descriptionService.getDescriptionSectionPermissions(descriptionSectionPermissionResolverModel).pipe(takeUntil(this._destroyed));
		} else if (copyPlanId != null && id != null && planSectionId != null) {
			const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
				planId: Guid.parse(copyPlanId),
				sectionIds: [Guid.parse(planSectionId)],
				permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription]
			}
			return this.descriptionService.getDescriptionSectionPermissions(descriptionSectionPermissionResolverModel).pipe(takeUntil(this._destroyed));
		}
	}
}
