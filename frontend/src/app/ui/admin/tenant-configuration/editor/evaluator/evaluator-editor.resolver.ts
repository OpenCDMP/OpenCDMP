import { Injectable } from "@angular/core";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { EvaluatorSource, EvaluatorTenantConfiguration, TenantConfiguration } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { TenantConfigurationService } from "@app/core/services/tenant-configuration/tenant-configuration.service";
import { BreadcrumbService } from "@app/ui/misc/breadcrumb/breadcrumb.service";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { nameof } from 'ts-simple-nameof';
import { takeUntil } from 'rxjs/operators';


@Injectable()
export class EvaluatorEditorResolver extends BaseEditorResolver{

    constructor(private tenantConfigurationService: TenantConfigurationService, private breadcrumbService: BreadcrumbService){
        super();
    }

    public static lookupFields(): string[] {
        return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),

			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.clientId)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.clientSecret)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.issuerUrl)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.evaluatorId)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.scope)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.sources), nameof<EvaluatorSource>(x => x.url)].join('.'),
			[nameof<TenantConfiguration>(x => x.evaluatorPlugins), nameof<EvaluatorTenantConfiguration>(x => x.disableSystemSources)].join('.'),


			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		] 
    }

    resolve() {

		const fields = [
			...EvaluatorEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getType(TenantConfigurationType.EvaluatorPlugins, fields).pipe(takeUntil(this._destroyed));
	}
}