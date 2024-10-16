import { Injectable } from '@angular/core';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { TenantConfigurationType } from '@notification-service/core/enum/tenant-configuration-type';
import { NotifierListTenantConfiguration, TenantConfiguration } from '@notification-service/core/model/tenant-configuration';
import { TenantConfigurationService } from '@notification-service/services/http/tenant-configuration.service';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class NotifierListEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.notifierList),

			[nameof<TenantConfiguration>(x => x.notifierList), nameof<NotifierListTenantConfiguration>(x => x.notifiers)].join('.'),


			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...NotifierListEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.NotifierList, fields).pipe(takeUntil(this._destroyed));
	}
}
