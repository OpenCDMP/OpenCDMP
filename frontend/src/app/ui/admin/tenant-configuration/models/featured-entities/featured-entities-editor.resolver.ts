import { Injectable } from '@angular/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { FeaturedEntities, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class FeaturedEntitiesEditorResolver extends BaseEditorResolver {

	constructor(private tenantConfigurationService: TenantConfigurationService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.featuredEntities),

			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.version)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.ordinal)].join('.'),

			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.version)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.ordinal)].join('.'),

			nameof<TenantConfiguration>(x => x.createdAt),
			nameof<TenantConfiguration>(x => x.updatedAt),
			nameof<TenantConfiguration>(x => x.hash),
			nameof<TenantConfiguration>(x => x.isActive)
		]
	}

	resolve() {

		const fields = [
			...FeaturedEntitiesEditorResolver.lookupFields()
		];

		return this.tenantConfigurationService.getType(TenantConfigurationType.FeaturedEntities, fields).pipe(takeUntil(this._destroyed));
	}
}
