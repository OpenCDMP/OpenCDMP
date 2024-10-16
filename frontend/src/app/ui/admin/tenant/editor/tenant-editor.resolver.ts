import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Tenant } from '@app/core/model/tenant/tenant';
import { TenantService } from '@app/core/services/tenant/tenant.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class TenantEditorResolver extends BaseEditorResolver {

	constructor(private tenantService: TenantService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Tenant>(x => x.id),
			nameof<Tenant>(x => x.name),
			nameof<Tenant>(x => x.code),
			nameof<Tenant>(x => x.description),


			nameof<Tenant>(x => x.createdAt),
			nameof<Tenant>(x => x.updatedAt),
			nameof<Tenant>(x => x.hash),
			nameof<Tenant>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...TenantEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.tenantService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.code)), takeUntil(this._destroyed));
		}
	}
}
