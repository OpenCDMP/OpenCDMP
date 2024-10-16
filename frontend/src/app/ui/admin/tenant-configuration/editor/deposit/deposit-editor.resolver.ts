import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DepositSource, DepositTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class DepositEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),

			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.clientId)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.clientSecret)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.issuerUrl)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.repositoryId)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.scope)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.url)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.pdfTransformerId)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.sources), nameof<DepositSource>(x => x.rdaTransformerId)].join('.'),
			[nameof<TenantConfiguration>(x => x.depositPlugins), nameof<DepositTenantConfiguration>(x => x.disableSystemSources)].join('.'),


			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...DepositEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.DepositPlugins, fields).pipe(takeUntil(this._destroyed));
	}
}
