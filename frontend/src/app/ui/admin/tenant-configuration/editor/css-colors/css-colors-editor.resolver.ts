import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { CssColorsTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class CssColorsEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.cssColors),

			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.primaryColor)].join('.'),
			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.cssOverride)].join('.'),


			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...CssColorsEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getType(TenantConfigurationType.CssColors, fields).pipe(takeUntil(this._destroyed));
	}
}
