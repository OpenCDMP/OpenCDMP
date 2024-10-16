import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { FileTransformerSource, FileTransformerTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class FileTransformerEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),

			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.clientId)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.clientSecret)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.issuerUrl)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.transformerId)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.scope)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.sources), nameof<FileTransformerSource>(x => x.url)].join('.'),
			[nameof<TenantConfiguration>(x => x.fileTransformerPlugins), nameof<FileTransformerTenantConfiguration>(x => x.disableSystemSources)].join('.'),


			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...FileTransformerEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.FileTransformerPlugins, fields).pipe(takeUntil(this._destroyed));
	}
}
