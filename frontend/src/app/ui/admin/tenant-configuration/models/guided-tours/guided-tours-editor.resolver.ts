import { Injectable } from '@angular/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { GuidedToursTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class GuidedToursEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.guidedTours), nameof<GuidedToursTenantConfiguration>(x => x.enabled)].join('.'),
			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive),
			nameof<TenantConfiguration>(x => x.belongsToCurrentTenant),
		];
	}

	/** The minimal projection the tour service needs to decide whether tours are on for this tenant. */
	public static activeLookupFields(): string[] {
		return [
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.guidedTours), nameof<GuidedToursTenantConfiguration>(x => x.enabled)].join('.'),
		];
	}

	resolve() {
		return this.tenantConfigurationService.getType(TenantConfigurationType.GuidedTours, GuidedToursEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed));
	}
}
