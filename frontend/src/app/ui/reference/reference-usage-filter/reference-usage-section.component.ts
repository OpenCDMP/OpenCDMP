import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, model, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ReferenceUsageEntityType } from '@app/core/common/enum/reference-usage-entity-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceUsage } from '@app/core/model/reference/reference-usage';
import { ReferenceUsageLookup } from '@app/core/query/reference-usage.lookup';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { nameof } from 'ts-simple-nameof';

const PAGE_SIZE = 5;
let nextSectionId = 0;

export interface ReferenceToggle {
	reference: Reference;
	checked: boolean;
}

interface UsageRow {
	reference: Reference;
	count: number | null;
}

/**
 * One collapsible filter section: the references of some types that the caller's plans (or
 * descriptions) actually link to, most used first, each with its count and a checkbox.
 *
 * <p>It only lists what exists in the caller's own data, so picking an entry never leads to an
 * empty result. Selected references stay listed even when they fall outside the loaded page.</p>
 */
@Component({
	selector: 'app-reference-usage-section',
	templateUrl: './reference-usage-section.component.html',
	styleUrl: './reference-usage-section.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, TranslateModule]
})
export class ReferenceUsageSectionComponent {
	private readonly referenceService = inject(ReferenceService);
	private readonly filterService = inject(FilterService);
	private readonly destroyRef = inject(DestroyRef);

	readonly title = input.required<string>();
	readonly searchLabel = input.required<string>();
	readonly entityType = input.required<ReferenceUsageEntityType>();
	/** Only references of these types; null for any type. */
	readonly typeIds = input<Guid[]>(null);
	/** Leave out references of these types. */
	readonly excludedTypeIds = input<Guid[]>(null);
	/** Group the list under reference type names (used when the section spans several types). */
	readonly groupByType = input(false);
	/** The selected references that belong to this section. */
	readonly selected = input<Reference[]>([]);
	readonly expanded = model(false);
	readonly toggled = output<ReferenceToggle>();

	protected readonly search = new FormControl<string>('');
	protected readonly sectionId = `reference-usage-section-${nextSectionId++}`;
	protected readonly like = signal('');
	private readonly size = signal(PAGE_SIZE);
	private readonly items = signal<ReferenceUsage[]>([]);
	private readonly total = signal(0);
	protected readonly loading = signal(false);
	private request: Subscription;

	protected readonly selectedIds = computed(() => new Set(this.selected().map(x => x.id?.toString())));
	protected readonly rows = computed<UsageRow[]>(() => {
		const listed = new Set(this.items().map(x => x.reference?.id?.toString()));
		const unlisted = this.like() ? [] : this.selected()
			.filter(x => !listed.has(x.id?.toString()))
			.map(reference => ({ reference, count: null }));
		return [...unlisted, ...this.items().map(x => ({ reference: x.reference, count: x.count }))];
	});
	protected readonly groups = computed(() => {
		if (!this.groupByType()) return [{ name: null as string, rows: this.rows() }];
		const groups = new Map<string, UsageRow[]>();
		for (const row of this.rows()) {
			const name = row.reference?.type?.name ?? '';
			if (!groups.has(name)) groups.set(name, []);
			groups.get(name).push(row);
		}
		return Array.from(groups.entries()).map(([name, rows]) => ({ name, rows }));
	});
	protected readonly remaining = computed(() => Math.max(this.total() - this.items().length, 0));
	protected readonly nextPage = computed(() => Math.min(this.remaining(), PAGE_SIZE));

	constructor() {
		this.search.valueChanges
			.pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
			.subscribe(value => {
				this.size.set(PAGE_SIZE);
				this.like.set(value?.trim() ?? '');
			});

		// Load when the section is open, and again whenever what it asks for changes.
		effect(() => {
			if (!this.expanded()) return;
			const lookup = this.buildLookup(this.like(), this.size(), this.typeIds(), this.excludedTypeIds(), this.entityType());
			untracked(() => this.load(lookup));
		});
	}

	protected toggleExpanded(): void {
		this.expanded.set(!this.expanded());
	}

	protected showMore(): void {
		this.size.update(x => x + PAGE_SIZE);
	}

	protected onChange(reference: Reference, checked: boolean): void {
		this.toggled.emit({ reference, checked });
	}

	private load(lookup: ReferenceUsageLookup): void {
		this.request?.unsubscribe();
		this.loading.set(true);
		this.request = this.referenceService.usage(lookup)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: result => {
					this.items.set(result?.items ?? []);
					this.total.set(result?.count ?? 0);
					this.loading.set(false);
				},
				error: () => {
					this.items.set([]);
					this.total.set(0);
					this.loading.set(false);
				}
			});
	}

	private buildLookup(like: string, size: number, typeIds: Guid[], excludedTypeIds: Guid[], entityType: ReferenceUsageEntityType): ReferenceUsageLookup {
		const lookup = new ReferenceUsageLookup();
		lookup.entityType = entityType;
		lookup.typeIds = typeIds?.length ? typeIds : null;
		lookup.excludedTypeIds = excludedTypeIds?.length ? excludedTypeIds : null;
		lookup.like = like ? this.filterService.transformLike(like) : null;
		lookup.page = { offset: 0, size };
		lookup.project = {
			fields: [
				[nameof<ReferenceUsage>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
				[nameof<ReferenceUsage>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
				[nameof<ReferenceUsage>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<ReferenceUsage>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
				nameof<ReferenceUsage>(x => x.count),
			]
		};
		return lookup;
	}
}
