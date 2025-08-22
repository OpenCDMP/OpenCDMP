import { Injectable } from '@angular/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { PluginConfiguration, PluginConfigurationField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PluginTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil} from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class PluginConfigurationEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.cssColors),

			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.name)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.extension)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.fullName)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.mimeType)].join('.'),

			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.code)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.textValue)].join('.'),

			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginCode)].join('.'),
			[nameof<TenantConfiguration>(x => x.pluginConfiguration), nameof<PluginTenantConfiguration>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginType)].join('.'),

			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...PluginConfigurationEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getType(TenantConfigurationType.PluginConfiguration, fields).pipe(takeUntil(this._destroyed));
	}
}
