import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceUsageEntityType } from '@app/core/common/enum/reference-usage-entity-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { Guid } from '@common/types/guid';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ReferenceTypeNamePipe } from '@app/core/pipes/reference-type-name.pipe';
import { nameof } from 'ts-simple-nameof';
import { ReferenceToggle, ReferenceUsageSectionComponent } from './reference-usage-section.component';

/**
 * Reference filters as ordinary filter sections: one per featured reference type (the tenant's
 * view preferences), then "More reference types" searching every other type at once.
 *
 * <p>Every section lists only references used by the caller's plans (or descriptions), with
 * counts. The component owns no filter state of its own: it reads the selected reference ids
 * and reports the new selection, grouped by type.</p>
 */
@Component({
	selector: 'app-reference-usage-filter',
	templateUrl: './reference-usage-filter.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [ReferenceUsageSectionComponent, TranslateModule, ReferenceTypeNamePipe]
})
export class ReferenceUsageFilterComponent {
	private readonly referenceService = inject(ReferenceService);
	private readonly referenceTypeService = inject(ReferenceTypeService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	readonly entityType = input.required<ReferenceUsageEntityType>();
	/** Reference types shown as their own sections, in order. */
	readonly featuredTypes = input<ReferenceType[]>([]);
	readonly selectedIds = input<Guid[]>([]);
	/** A reference type whose section starts open (added as a section if it is not featured). */
	readonly expandTypeId = input<Guid>(null);
	readonly selectionChange = output<ReferencesWithType[]>();

	private readonly selection = signal<Reference[]>([]);
	private readonly extraType = signal<ReferenceType>(null);
	protected readonly expanded = signal<Record<string, boolean>>({});

	protected readonly sections = computed<ReferenceType[]>(() => {
		const featured = (this.featuredTypes() ?? []).filter(x => x?.id);
		const extra = this.extraType();
		const all = extra && !featured.some(x => x.id?.toString() === extra.id?.toString()) ? [extra, ...featured] : featured;
		const seen = new Set<string>();
		return all.filter(x => {
			const key = x.id.toString();
			if (seen.has(key)) return false;
			seen.add(key);
			return true;
		});
	});
	protected readonly sectionTypeIds = computed(() => this.sections().map(x => x.id));
	private readonly sectionKeys = computed(() => new Set(this.sections().map(x => x.id.toString())));
	protected readonly selectedByType = computed(() => {
		const map = new Map<string, Reference[]>();
		for (const reference of this.selection()) {
			const key = reference.type?.id?.toString() ?? '';
			if (!map.has(key)) map.set(key, []);
			map.get(key).push(reference);
		}
		return map;
	});
	protected readonly moreSelected = computed(() => this.selection().filter(x => !this.sectionKeys().has(x.type?.id?.toString())));
	protected readonly moreSearchLabel = computed(() => this.language.instant(
		this.entityType() === ReferenceUsageEntityType.Description ? 'REFERENCE-USAGE-FILTER.SEARCH-ALL-DESCRIPTIONS' : 'REFERENCE-USAGE-FILTER.SEARCH-ALL-PLANS'
	));

	constructor() {
		// Keep the selection in step with the ids handed in, loading label and type of any new ones.
		effect(() => {
			const ids = (this.selectedIds() ?? []).filter(x => x != null).map(x => x.toString());
			untracked(() => this.syncSelection(ids));
		});

		effect(() => {
			const typeId = this.expandTypeId();
			if (!typeId) return;
			untracked(() => {
				this.expanded.update(x => ({ ...x, [typeId.toString()]: true }));
				if (!(this.featuredTypes() ?? []).some(x => x?.id?.toString() === typeId.toString())) this.loadExtraType(typeId);
			});
		});
	}

	protected selectedFor(typeId: Guid): Reference[] {
		return this.selectedByType().get(typeId?.toString()) ?? [];
	}

	protected isExpanded(key: string): boolean {
		return !!this.expanded()[key];
	}

	protected setExpanded(key: string, value: boolean): void {
		this.expanded.update(x => ({ ...x, [key]: value }));
	}

	protected searchLabelFor(name: string): string {
		return this.language.instant('REFERENCE-USAGE-FILTER.SEARCH-TYPE', { name: name ?? '' });
	}

	protected onToggle(toggle: ReferenceToggle): void {
		const id = toggle.reference?.id?.toString();
		if (!id) return;
		const current = this.selection().filter(x => x.id?.toString() !== id);
		this.selection.set(toggle.checked ? [...current, toggle.reference] : current);
		this.selectionChange.emit(this.grouped());
	}

	private grouped(): ReferencesWithType[] {
		return Array.from(this.selectedByType().entries()).map(([typeId, references]) => ({
			referenceTypeId: typeId ? (references[0].type.id) : null,
			referenceIds: references.map(x => x.id)
		}));
	}

	private syncSelection(ids: string[]): void {
		const wanted = new Set(ids);
		const kept = this.selection().filter(x => wanted.has(x.id?.toString()));
		const known = new Set(kept.map(x => x.id.toString()));
		const missing = ids.filter(x => !known.has(x));
		if (kept.length !== this.selection().length) this.selection.set(kept);
		if (!missing.length) return;

		const lookup = new ReferenceLookup();
		lookup.ids = missing.map(x => Guid.parse(x));
		lookup.page = { offset: 0, size: missing.length };
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.label),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
			]
		};
		// The backend refuses paging without an order.
		lookup.order = { items: [nameof<Reference>(x => x.label)] };
		this.referenceService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
			const stillWanted = new Set((this.selectedIds() ?? []).map(x => x?.toString()));
			const present = new Set(this.selection().map(x => x.id.toString()));
			const added = (result?.items ?? []).filter(x => stillWanted.has(x.id?.toString()) && !present.has(x.id?.toString()));
			if (added.length) this.selection.update(x => [...x, ...added]);
		});
	}

	private loadExtraType(typeId: Guid): void {
		const lookup = new ReferenceTypeLookup();
		lookup.ids = [typeId];
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 1 };
		lookup.project = { fields: [nameof<ReferenceType>(x => x.id), nameof<ReferenceType>(x => x.name), nameof<ReferenceType>(x => x.code)] };
		lookup.order = { items: [nameof<ReferenceType>(x => x.name)] };
		this.referenceTypeService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
			const type = result?.items?.[0];
			if (type) this.extraType.set(type);
		});
	}
}
