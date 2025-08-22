import { Injectable } from '@angular/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ViewPreferencesConfiguration, TenantConfiguration, ViewPreference } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class ViewPreferencesEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.viewPreferences),

			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.planPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.planPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.planPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.isActive)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.planPreferences), nameof<ViewPreference>(x => x.ordinal)].join('.'),

			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.descriptionPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.descriptionPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.descriptionPreferences), nameof<ViewPreference>(x => x.referenceType), nameof<ReferenceType>(x => x.isActive)].join('.'),
			[nameof<TenantConfiguration>(x => x.viewPreferences), nameof<ViewPreferencesConfiguration>(x => x.descriptionPreferences), nameof<ViewPreference>(x => x.ordinal)].join('.'),

			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...ViewPreferencesEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getType(TenantConfigurationType.ViewPreferences, fields).pipe(takeUntil(this._destroyed));
	}
}
