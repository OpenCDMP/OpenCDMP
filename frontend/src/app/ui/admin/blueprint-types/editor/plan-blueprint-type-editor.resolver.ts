import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class PlanBlueprintTypeEditorResolver extends BaseEditorResolver {

	constructor(private planBlueprintTypeService: PlanBlueprintTypeService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<PlanBlueprintType>(x => x.id),
			nameof<PlanBlueprintType>(x => x.name),
			nameof<PlanBlueprintType>(x => x.code),
			nameof<PlanBlueprintType>(x => x.status),
			nameof<PlanBlueprintType>(x => x.createdAt),
			nameof<PlanBlueprintType>(x => x.hash),
			nameof<PlanBlueprintType>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanBlueprintTypeEditorResolver.lookupFields()
		];
		return this.planBlueprintTypeService.getSingle(Guid.parse(route.paramMap.get('id')), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.name)), takeUntil(this._destroyed));
	}
}
