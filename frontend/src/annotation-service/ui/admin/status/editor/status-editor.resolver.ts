import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Status } from '@annotation-service/core/model/status.model';
import { StatusService } from '@annotation-service/services/http/status.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class StatusEditorResolver extends BaseEditorResolver {

	constructor(private statusService: StatusService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Status>(x => x.id),
			nameof<Status>(x => x.label),
			nameof<Status>(x => x.internalStatus),
			nameof<Status>(x => x.createdAt),
			nameof<Status>(x => x.updatedAt),
			nameof<Status>(x => x.hash),
			nameof<Status>(x => x.isActive),
			nameof<Status>(x => x.belongsToCurrentTenant)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...StatusEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.statusService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		}
	}
}
