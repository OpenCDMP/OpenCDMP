import { Injectable } from '@angular/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfiguration, TermsAndConditionsTenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class TermsAndConditionsEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.termsAndConditions), nameof<TermsAndConditionsTenantConfiguration>(x => x.content)].join('.'),
			[nameof<TenantConfiguration>(x => x.termsAndConditions), nameof<TermsAndConditionsTenantConfiguration>(x => x.version)].join('.'),
			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive),
			nameof<TenantConfiguration>(x => x.belongsToCurrentTenant),
		];
	}

	resolve() {
		return this.tenantConfigurationService.getType(TenantConfigurationType.TermsAndConditions, TermsAndConditionsEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed));
	}
}
