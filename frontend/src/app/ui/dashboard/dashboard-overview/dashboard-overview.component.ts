import { Component, DestroyRef, computed, effect, inject, input, signal, untracked, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceUsageEntityType } from '@app/core/common/enum/reference-usage-entity-type';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceType, ReferenceTypeDefinition } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceUsage } from '@app/core/model/reference/reference-usage';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { ReferenceUsageLookup } from '@app/core/query/reference-usage.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { MergedDashboardStatistics } from '@app/core/services/statistics/ui-statistics.service';
import { BaseComponent } from '@common/base/base.component';
import { catchError, forkJoin, map, of } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { MatCard, MatCardContent } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { ReferenceTypeNamePipe } from '@app/core/pipes/reference-type-name.pipe';

interface ReferenceRow {
    id: string;
    type: ReferenceType;
    count: number;
    hasLogo: boolean;
}

@Component({
    selector: 'app-dashboard-overview',
    templateUrl: './dashboard-overview.component.html',
    styleUrl: './dashboard-overview.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCard, MatCardContent, RouterLink, MatIcon, TranslatePipe, ReferenceTypeNamePipe]
})
export class DashboardOverviewComponent extends BaseComponent{
    dashboardStatistics = input<MergedDashboardStatistics>();
    /** The tenant's featured plan reference types (view preferences); one overview row each. */
    featuredReferenceTypes = input<ReferenceType[]>([]);

    private readonly referenceService = inject(ReferenceService);
    private readonly referenceTypeService = inject(ReferenceTypeService);
    private readonly configurationService = inject(ConfigurationService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly referenceCounts = signal<Record<string, number>>({});
    private readonly referenceLogos = signal<Record<string, boolean>>({});
    /** Logos that failed to load fall back to the default icon. */
    private readonly brokenLogos = signal<Record<string, boolean>>({});

    /**
     * One row per featured reference type. Signed in: the tenant's view preferences, counted over the
     * plans the user can see. Signed out: the default tenant's, counted over public plans (the backend
     * statistics). Either way, a type counts distinct references, not plans.
     */
    referenceRows = computed<ReferenceRow[]>(() => {
        const broken = this.brokenLogos();
        const rows: ReferenceRow[] = this.authService.currentAccountIsAuthenticated()
            ? (this.featuredReferenceTypes() ?? []).filter(x => x?.id).map(type => ({
                id: type.id.toString(),
                type,
                count: this.referenceCounts()[type.id.toString()],
                hasLogo: !!this.referenceLogos()[type.id.toString()],
            }))
            : (this.dashboardStatistics()?.publicReferenceTypeStatistics ?? []).filter(x => x?.referenceType?.id).map(x => ({
                id: x.referenceType.id.toString(),
                type: x.referenceType,
                count: x.count,
                hasLogo: !!x.hasLogo,
            }));
        return rows.map(x => ({ ...x, hasLogo: x.hasLogo && !broken[x.id] }));
    });

    constructor(
        protected authService: AuthService,
        protected routerUtils: RouterUtilsService
    ){
        super();
        effect(() => {
            const types = (this.featuredReferenceTypes() ?? []).filter(x => x?.id);
            untracked(() => this.loadFeatured(types));
        });
    }

    logoUrl(referenceTypeId: string): string {
        return `${this.configurationService.server}reference-type/public/${referenceTypeId}/logo`;
    }

    onLogoError(referenceTypeId: string): void {
        this.brokenLogos.update(x => ({ ...x, [referenceTypeId]: true }));
    }

    private loadFeatured(types: ReferenceType[]): void {
        if (!types.length || !this.authService.currentAccountIsAuthenticated()) return;

        forkJoin(types.map(type => this.referenceService.usage(this.buildUsageLookup(type)).pipe(
            map(result => [type.id.toString(), result?.count ?? 0] as const),
            catchError(() => of([type.id.toString(), 0] as const))
        )))
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(entries => this.referenceCounts.set(Object.fromEntries(entries)));

        this.referenceTypeService.query(this.buildLogoLookup(types))
            .pipe(takeUntilDestroyed(this.destroyRef), catchError(() => of(null)))
            .subscribe(result => this.referenceLogos.set(Object.fromEntries((result?.items ?? []).map(type => [
                type.id.toString(),
                (type.definition?.sources ?? []).some(source => !!source?.storageFile?.id)
            ]))));
    }

    private buildUsageLookup(type: ReferenceType): ReferenceUsageLookup {
        const lookup = new ReferenceUsageLookup();
        lookup.entityType = ReferenceUsageEntityType.Plan;
        lookup.typeIds = [type.id];
        lookup.page = { offset: 0, size: 1 };
        lookup.project = { fields: [[nameof<ReferenceUsage>(x => x.reference), nameof<Reference>(x => x.id)].join('.')] };
        return lookup;
    }

    private buildLogoLookup(types: ReferenceType[]): ReferenceTypeLookup {
        const lookup = new ReferenceTypeLookup();
        lookup.ids = types.map(x => x.id);
        lookup.isActive = [IsActive.Active];
        lookup.page = { offset: 0, size: types.length };
        lookup.order = { items: [nameof<ReferenceType>(x => x.name)] };
        lookup.project = {
            fields: [
                nameof<ReferenceType>(x => x.id),
                [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
            ]
        };
        return lookup;
    }
}
