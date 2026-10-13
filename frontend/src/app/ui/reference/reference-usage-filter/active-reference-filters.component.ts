import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';

/**
 * The reference filters a listing currently applies, as removable chips above its results
 * ("Filtered by  Grant: X ×  Clear all"). Renders nothing while no reference filter is set.
 */
@Component({
	selector: 'app-active-reference-filters',
	templateUrl: './active-reference-filters.component.html',
	styleUrl: './active-reference-filters.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [MatButtonModule, MatIconModule, TranslateModule]
})
export class ActiveReferenceFiltersComponent {
	private readonly referenceService = inject(ReferenceService);
	private readonly destroyRef = inject(DestroyRef);

	readonly referenceIds = input<Guid[]>([]);
	readonly removed = output<Guid>();
	readonly cleared = output<void>();

	private readonly references = signal<Reference[]>([]);
	protected readonly chips = computed(() => {
		const byId = new Map(this.references().map(x => [x.id?.toString(), x]));
		return (this.referenceIds() ?? []).filter(x => x != null).map(id => {
			const reference = byId.get(id.toString());
			return { id, label: reference?.label ?? '…', typeName: reference?.type?.name ?? null };
		});
	});

	constructor() {
		effect(() => {
			const ids = (this.referenceIds() ?? []).filter(x => x != null).map(x => x.toString());
			untracked(() => this.loadMissing(ids));
		});
	}

	private loadMissing(ids: string[]): void {
		const known = new Set(this.references().map(x => x.id?.toString()));
		const missing = ids.filter(x => !known.has(x));
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
			const present = new Set(this.references().map(x => x.id?.toString()));
			const added = (result?.items ?? []).filter(x => !present.has(x.id?.toString()));
			if (added.length) this.references.update(x => [...x, ...added]);
		});
	}
}
