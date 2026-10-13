import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { DescriptionStatusLookup } from '@app/core/query/description-status.lookup';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { PlanStatusLookup } from '@app/core/query/plan-status.lookup';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { Guid } from '@common/types/guid';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';
import { BreakdownOption, ReportFilters } from '../report-state';

type EditorKind = 'descriptionStatus' | 'planStatus' | 'access' | 'blueprint' | 'created' | 'planReference' | 'descriptionReference';

interface Editor {
	kind: EditorKind;
	referenceType?: ReferenceType;
}

/** One chip in the bar: a filter the report currently applies, in words. */
interface FilterChip {
	key: string;
	label: string;
	value: string;
	editor: Editor;
}

/**
 * The filters and breakdowns of a report as a row of chips above the result.
 *
 * <p>Each filter is named for what it is — Funder, Plan status, Created — and opens a small
 * editor in place, so a report is refined the way a search is, one chip at a time, instead
 * of by filling in a form beside it. References are offered per type, straight from the
 * tenant's reference types: the menu says Funder or Licence, never "reference type".</p>
 */
@Component({
	selector: 'app-report-filter-bar',
	standalone: true,
	imports: [
		ReactiveFormsModule, AutoCompleteModule,
		MatButtonModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule, MatSelectModule, MatTooltipModule,
		TranslateModule
	],
	templateUrl: './report-filter-bar.component.html',
	styleUrl: './report-filter-bar.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportFilterBarComponent implements OnInit {

	private readonly descriptionStatusService = inject(DescriptionStatusService);
	private readonly planStatusService = inject(PlanStatusService);
	private readonly planBlueprintService = inject(PlanBlueprintService);
	private readonly referenceService = inject(ReferenceService);
	private readonly referenceTypeService = inject(ReferenceTypeService);
	private readonly enumUtils = inject(EnumUtils);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	readonly subject = input.required<ReportSubjectType>();
	readonly filters = input.required<ReportFilters>();
	readonly breakdown = input<string[]>([]);
	readonly breakdownOptions = input<BreakdownOption[]>([]);
	readonly maxBreakdowns = input<number>(3);
	/** True when the blueprint is the report's own source and must not be offered as a filter. */
	readonly blueprintIsSource = input<boolean>(false);

	readonly filtersChange = output<ReportFilters>();
	readonly breakdownChange = output<string[]>();

	protected readonly accessTypeOptions = [PlanAccessType.Public, PlanAccessType.Restricted];
	protected readonly descriptionStatuses = signal<DescriptionStatus[]>([]);
	protected readonly planStatuses = signal<PlanStatus[]>([]);
	protected readonly referenceTypes = signal<ReferenceType[]>([]);
	protected readonly referenceCache = signal<Map<string, Reference>>(new Map());
	protected readonly blueprintLabel = signal<string>(null);

	protected readonly editor = signal<Editor>(null);
	protected readonly referenceControl = new FormControl<Guid[]>([]);
	protected readonly blueprintControl = new FormControl<Guid>(null);
	protected readonly referenceConfiguration = signal<MultipleAutoCompleteConfiguration>(null);
	protected readonly blueprintConfiguration = this.planBlueprintService.planBlueprintGroupSingleAutocompleteConfiguration;

	protected readonly isAssetSubject = computed(() => this.subject() === ReportSubjectType.Description);

	protected readonly availableBreakdowns = computed(() =>
		this.breakdownOptions().filter(x => !this.breakdown().includes(x.token)));

	protected readonly breakdownChips = computed(() =>
		this.breakdown().map(token => this.breakdownOptions().find(x => x.token === token) ?? { token, label: token }));

	protected readonly chips = computed<FilterChip[]>(() => {
		const filters = this.filters();
		const chips: FilterChip[] = [];

		if (filters.descriptionStatusIds.length > 0) {
			chips.push(this.chip('descriptionStatus', 'REPORTS.FILTERS.ASSET-STATUS',
				this.namesOf(filters.descriptionStatusIds, this.descriptionStatuses()), { kind: 'descriptionStatus' }));
		}
		if (filters.planStatusIds.length > 0) {
			chips.push(this.chip('planStatus', 'REPORTS.FILTERS.PLAN-STATUS',
				this.namesOf(filters.planStatusIds, this.planStatuses()), { kind: 'planStatus' }));
		}
		if (filters.accessTypes.length > 0) {
			chips.push(this.chip('access', 'REPORTS.FILTERS.PLAN-ACCESS',
				filters.accessTypes.map(x => this.enumUtils.toPlanAccessTypeString(x)).join(', '), { kind: 'access' }));
		}
		if (filters.planBlueprintGroupId) {
			chips.push(this.chip('blueprint', 'REPORTS.FILTERS.PLAN-BLUEPRINT', this.blueprintLabel() ?? '…', { kind: 'blueprint' }));
		}
		if (filters.createdAfter || filters.createdBefore) {
			const datePipe = new DatePipe(this.language.currentLang || 'en');
			const parts: string[] = [];
			if (filters.createdAfter) parts.push(this.language.instant('REPORTS.FILTERS.AFTER', { date: datePipe.transform(filters.createdAfter, 'mediumDate') }));
			if (filters.createdBefore) parts.push(this.language.instant('REPORTS.FILTERS.BEFORE', { date: datePipe.transform(filters.createdBefore, 'mediumDate') }));
			chips.push(this.chip('created', 'REPORTS.FILTERS.CREATED', parts.join(' · '), { kind: 'created' }));
		}

		// References are shown per type — "Funder: FCT", "Licence: CC-BY" — because that is
		// how the user chose them and the only way the chip reads as a sentence.
		chips.push(...this.referenceChips('planReference', filters.planReferenceIds, 'REPORTS.FILTERS.OF-PLAN'));
		chips.push(...this.referenceChips('descriptionReference', filters.descriptionReferenceIds, 'REPORTS.FILTERS.OF-ASSET'));
		return chips;
	});

	constructor() {
		// Labels for ids that arrived from the URL or from a previous session.
		effect(() => {
			const filters = this.filters();
			untracked(() => {
				this.resolveReferences([...filters.planReferenceIds, ...filters.descriptionReferenceIds]);
				this.resolveBlueprint(filters.planBlueprintGroupId);
			});
		});
	}

	ngOnInit(): void {
		const descriptionStatusLookup = new DescriptionStatusLookup();
		descriptionStatusLookup.isActive = [IsActive.Active];
		descriptionStatusLookup.project = { fields: [nameof<DescriptionStatus>(x => x.id), nameof<DescriptionStatus>(x => x.name)] };
		this.descriptionStatusService.query(descriptionStatusLookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.descriptionStatuses.set(result.items),
			error: () => this.descriptionStatuses.set([])
		});

		const planStatusLookup = new PlanStatusLookup();
		planStatusLookup.isActive = [IsActive.Active];
		planStatusLookup.project = { fields: [nameof<PlanStatus>(x => x.id), nameof<PlanStatus>(x => x.name)] };
		this.planStatusService.query(planStatusLookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.planStatuses.set(result.items),
			error: () => this.planStatuses.set([])
		});

		const typeLookup = new ReferenceTypeLookup();
		typeLookup.isActive = [IsActive.Active];
		typeLookup.project = { fields: [nameof<ReferenceType>(x => x.id), nameof<ReferenceType>(x => x.name), nameof<ReferenceType>(x => x.code)] };
		typeLookup.order = { items: [nameof<ReferenceType>(x => x.name)] };
		this.referenceTypeService.query(typeLookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.referenceTypes.set(result.items),
			error: () => this.referenceTypes.set([])
		});

		this.referenceControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(ids => this.onReferencesPicked(ids ?? []));
		this.blueprintControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(groupId => {
			if ((groupId ?? null) === (this.filters().planBlueprintGroupId ?? null)) return;
			this.patch({ planBlueprintGroupId: groupId ?? null });
		});
	}

	//region editors

	protected open(kind: EditorKind, referenceType?: ReferenceType): void {
		const editor: Editor = { kind, referenceType };
		this.editor.set(editor);

		if (kind === 'planReference' || kind === 'descriptionReference') {
			const current = this.referenceIdsOfType(kind, referenceType.id);
			this.referenceConfiguration.set(this.referenceService.getMultipleAutoCompleteQueryConfiguration([referenceType.id]));
			this.referenceControl.setValue(current, { emitEvent: false });
		}
		if (kind === 'blueprint') {
			this.blueprintControl.setValue(this.filters().planBlueprintGroupId, { emitEvent: false });
		}
	}

	protected close(): void {
		this.editor.set(null);
		this.referenceConfiguration.set(null);
	}

	protected openChip(chip: FilterChip): void {
		this.open(chip.editor.kind, chip.editor.referenceType);
	}

	protected removeChip(chip: FilterChip): void {
		switch (chip.editor.kind) {
			case 'descriptionStatus': this.patch({ descriptionStatusIds: [] }); break;
			case 'planStatus': this.patch({ planStatusIds: [] }); break;
			case 'access': this.patch({ accessTypes: [] }); break;
			case 'blueprint': this.patch({ planBlueprintGroupId: null }); break;
			case 'created': this.patch({ createdAfter: null, createdBefore: null }); break;
			case 'planReference':
				this.patch({ planReferenceIds: this.filters().planReferenceIds.filter(x => !this.isOfType(x, chip.editor.referenceType.id)) });
				break;
			case 'descriptionReference':
				this.patch({ descriptionReferenceIds: this.filters().descriptionReferenceIds.filter(x => !this.isOfType(x, chip.editor.referenceType.id)) });
				break;
		}
		if (this.editor()?.kind === chip.editor.kind && this.editor()?.referenceType?.id === chip.editor.referenceType?.id) this.close();
	}

	protected setDescriptionStatuses(ids: Guid[]): void { this.patch({ descriptionStatusIds: ids ?? [] }); }

	protected setPlanStatuses(ids: Guid[]): void { this.patch({ planStatusIds: ids ?? [] }); }

	protected setAccessTypes(values: PlanAccessType[]): void { this.patch({ accessTypes: values ?? [] }); }

	protected setCreatedAfter(date: Date): void { this.patch({ createdAfter: date ?? null }); }

	protected setCreatedBefore(date: Date): void { this.patch({ createdBefore: date ?? null }); }

	protected accessTypeLabel(value: PlanAccessType): string {
		return this.enumUtils.toPlanAccessTypeString(value);
	}

	private onReferencesPicked(ids: Guid[]): void {
		const editor = this.editor();
		if (!editor || !editor.referenceType) return;
		const typeId = editor.referenceType.id;

		// The picker only knows about its own type; the other types' choices stay as they are.
		const key = editor.kind === 'planReference' ? 'planReferenceIds' : 'descriptionReferenceIds';
		const others = this.filters()[key].filter(x => !this.isOfType(x, typeId) && !ids.some(id => id?.toString() === x?.toString()));
		const next = [...others, ...ids];

		// Anything new is remembered as belonging to this type before its label arrives, so
		// a chip for it is grouped correctly even while the lookup is in flight.
		const cache = new Map(this.referenceCache());
		for (const id of ids) {
			if (!cache.has(id.toString())) cache.set(id.toString(), { id, label: '…', type: editor.referenceType } as Reference);
		}
		this.referenceCache.set(cache);
		this.patch({ [key]: next } as Partial<ReportFilters>);
	}

	//endregion

	//region breakdowns

	protected addBreakdown(token: string): void {
		if (this.breakdown().includes(token) || this.breakdown().length >= this.maxBreakdowns()) return;
		this.breakdownChange.emit([...this.breakdown(), token]);
	}

	protected removeBreakdown(token: string): void {
		this.breakdownChange.emit(this.breakdown().filter(x => x !== token));
	}

	//endregion

	private patch(changes: Partial<ReportFilters>): void {
		this.filtersChange.emit({ ...this.filters(), ...changes });
	}

	private chip(key: string, labelKey: string, value: string, editor: Editor): FilterChip {
		return { key, label: this.language.instant(labelKey), value, editor };
	}

	private referenceChips(kind: 'planReference' | 'descriptionReference', ids: Guid[], scopeKey: string): FilterChip[] {
		const byType = new Map<string, { type: ReferenceType, labels: string[] }>();
		for (const id of ids) {
			const reference = this.referenceCache().get(id?.toString());
			const type = reference?.type;
			const typeKey = type?.id?.toString() ?? 'unknown';
			const entry = byType.get(typeKey) ?? { type: type ?? { id: null, name: this.language.instant('REPORTS.FILTERS.REFERENCE') } as ReferenceType, labels: [] };
			entry.labels.push(reference?.label ?? '…');
			byType.set(typeKey, entry);
		}

		const chips: FilterChip[] = [];
		for (const [typeKey, entry] of byType.entries()) {
			const scope = this.isAssetSubject() ? ' ' + this.language.instant(scopeKey) : '';
			chips.push({
				key: kind + ':' + typeKey,
				label: entry.type.name + scope,
				value: entry.labels.join(', '),
				editor: { kind, referenceType: entry.type }
			});
		}
		return chips;
	}

	private namesOf(ids: Guid[], items: { id?: Guid, name?: string }[]): string {
		return ids.map(id => items.find(x => x.id?.toString() === id?.toString())?.name ?? '…').join(', ');
	}

	private referenceIdsOfType(kind: EditorKind, typeId: Guid): Guid[] {
		const ids = kind === 'planReference' ? this.filters().planReferenceIds : this.filters().descriptionReferenceIds;
		return ids.filter(x => this.isOfType(x, typeId));
	}

	private isOfType(id: Guid, typeId: Guid): boolean {
		return this.referenceCache().get(id?.toString())?.type?.id?.toString() === typeId?.toString();
	}

	private resolveReferences(ids: Guid[]): void {
		const missing = ids.filter(id => {
			const cached = this.referenceCache().get(id?.toString());
			return !cached || cached.label === '…';
		});
		if (missing.length === 0) return;

		const lookup = new ReferenceLookup();
		lookup.ids = missing;
		lookup.isActive = [IsActive.Active, IsActive.Inactive];
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.label),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.')
			]
		};
		this.referenceService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				const cache = new Map(this.referenceCache());
				for (const reference of result.items ?? []) cache.set(reference.id.toString(), reference);
				this.referenceCache.set(cache);
			},
			error: () => { /* the chip keeps its placeholder */ }
		});
	}

	private resolveBlueprint(groupId: Guid): void {
		if (!groupId) {
			this.blueprintLabel.set(null);
			return;
		}
		const lookup = new PlanBlueprintLookup();
		lookup.groupIds = [groupId];
		lookup.isActive = [IsActive.Active, IsActive.Inactive];
		lookup.page = { size: 1, offset: 0 };
		lookup.order = { items: ['-' + nameof<PlanBlueprint>(x => x.version)] };
		lookup.project = { fields: [nameof<PlanBlueprint>(x => x.id), nameof<PlanBlueprint>(x => x.label)] };
		this.planBlueprintService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.blueprintLabel.set(result.items?.[0]?.label ?? null),
			error: () => this.blueprintLabel.set(null)
		});
	}
}
