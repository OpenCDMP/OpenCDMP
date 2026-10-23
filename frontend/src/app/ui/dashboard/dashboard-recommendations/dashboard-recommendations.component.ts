import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { FeaturedEntities, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslatePipe } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';

interface Recommendation {
	isBlueprint: boolean;
	id: Guid;
	groupId: Guid;
	label: string;
	typeName: string;
	description: string;
	version: number | string;
	usageCount: number;
}

@Component({
	selector: 'app-dashboard-recommendations',
	templateUrl: './dashboard-recommendations.component.html',
	styleUrl: './dashboard-recommendations.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [MatButton, MatIconButton, MatIcon, RouterLink, TranslatePipe]
})
export class DashboardRecommendationsComponent {
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly principalService = inject(PrincipalService);
	private readonly authService = inject(AuthService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	protected readonly routerUtils = inject(RouterUtilsService);

	private readonly pageSize = 3;
	protected readonly items = signal<Recommendation[]>([]);
	protected readonly tenantName = signal<string>(null);
	protected readonly expanded = signal(true);
	protected readonly page = signal(0);
	protected readonly pageCount = computed(() => Math.ceil(this.items().length / this.pageSize));
	protected readonly rangeStart = computed(() => this.page() * this.pageSize + 1);
	protected readonly rangeEnd = computed(() => Math.min((this.page() + 1) * this.pageSize, this.items().length));
	protected readonly pageItems = computed(() => this.items().slice(this.rangeStart() - 1, this.rangeEnd()));

	constructor() {
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.FeaturedEntities, this.lookupFields())
			.pipe(takeUntilDestroyed())
			.subscribe({
				next: (data) => this.items.set([
					...(data?.featuredEntities?.planBlueprints ?? []).map((x) => this.toRecommendation(x, true)),
					...(data?.featuredEntities?.descriptionTemplates ?? []).map((x) => this.toRecommendation(x, false))
				]),
				error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
			});

		if ((this.authService.selectedTenant() ?? 'default') !== 'default') {
			this.principalService.myTenantsTranslated()
				.pipe(takeUntilDestroyed())
				.subscribe((tenants) => this.tenantName.set(tenants.find((x) => x.code === this.authService.selectedTenant())?.name ?? null));
		}
	}

	toggle(): void {
		this.expanded.update((x) => !x);
	}

	previous(): void {
		this.page.update((x) => Math.max(x - 1, 0));
	}

	next(): void {
		this.page.update((x) => Math.min(x + 1, this.pageCount() - 1));
	}

	private toRecommendation(item: PlanBlueprint | DescriptionTemplate, isBlueprint: boolean): Recommendation {
		return {
			isBlueprint,
			id: item.id,
			groupId: item.groupId,
			label: item.label,
			typeName: item.type?.name,
			description: item.description,
			version: item.version,
			usageCount: item.usageCount ?? 0
		};
	}

	private lookupFields(): string[] {
		return [
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.description)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.version)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.ordinal)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.usageCount)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.name)].join('.'),

			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.description)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.version)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.ordinal)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.usageCount)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
		];
	}
}
