import { ChangeDetectionStrategy, Component, computed, effect, EventEmitter, inject, Input, Injector, OnChanges, Output, signal, SimpleChanges } from '@angular/core';
import { Guid } from '@common/types/guid';
import { DescriptionChangeKind } from '@app/core/common/enum/description-change-kind.enum';
import { DescriptionChange, FieldChange, FieldValuePreview, fieldValueDisplayText } from '@app/core/model/plan-update-request/plan-update-preview';
import { PlanReviewService, NewDescriptionSelection, ReviewDecision } from '../plan-review.service';
import { PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatSelect, MatOption } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';

export interface AcceptedNewDescription {
	changeId: Guid;
	datasetId: string;
	planDescriptionTemplate: PlanDescriptionTemplate;
	label: string;
}

export interface RevertedNewDescription {
	changeId: Guid;
	descriptionId: Guid;
}

export interface AcceptedDeleteDescription {
	changeId: Guid;
	descriptionId: Guid;
}

export interface OverviewRow {
	changeId: Guid;
	ordinal: string | null;
	label: string;
	currentValue: FieldValuePreview | undefined;
	incomingValue: FieldValuePreview | undefined;
	state: ReviewDecision;
	scope: 'plan' | 'description';
	containerLabel?: string | null;
	anchorElementId?: string;
}

@Component({
    selector: 'app-plan-review-action-bar',
    templateUrl: './plan-review-action-bar.component.html',
    styleUrls: ['./plan-review-action-bar.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        MatTooltip,
        MatIcon,
        MatButton,
        NgTemplateOutlet,
        NgClass,
        MatFormField,
        MatLabel,
        MatSelect,
        FormsModule,
        MatOption,
        TranslatePipe,
    ],
})
export class PlanReviewActionBarComponent implements OnChanges {
	readonly expanded = signal(false);

	/** Existing PlanDescriptionTemplate rows from the plan — passed from the plan editor. */
	@Input() planDescriptionTemplates: PlanDescriptionTemplate[] = [];
	/** Blueprint sections with `hasTemplates` — used to resolve section labels. */
	@Input() blueprintSections: PlanBlueprintDefinitionSection[] = [];

	/** Emits when user clicks "Add to plan" for a new ADD-kind description. */
	@Output() acceptedNewDescription = new EventEmitter<AcceptedNewDescription>();
	/** Emits when user clicks "Revert" to remove an already-added new description from the form. */
	@Output() revertedNewDescription = new EventEmitter<RevertedNewDescription>();
	/** Emits when user confirms deletion of an existing description (DELETE-kind accepted). */
	@Output() acceptedDeleteDescription = new EventEmitter<AcceptedDeleteDescription>();
	/** Emits when user undoes a confirmed deletion (DELETE-kind reset to pending). */
	@Output() revertedDeleteDescription = new EventEmitter<AcceptedDeleteDescription>();

	/** Maps ADD changeId (string) → temp descriptionId assigned when added to the form. */
	readonly addedToFormMap = signal<Map<string, Guid>>(new Map());
	/** Tracks DELETE-kind changeIds that have been accepted (confirmed for deletion). */
	private readonly acceptedDeleteIds = signal<Set<string>>(new Set());

	private readonly injector = inject(Injector);

	constructor(public planReviewService: PlanReviewService) {
		effect(() => {
			const decisions = this.planReviewService.decisions();
			const addChanges = this.planReviewService.addDescriptionChanges();
			const addedMap = this.addedToFormMap();
			for (const dc of addChanges) {
				if (!dc.changeId) continue;
				const changeIdStr = dc.changeId.toString();
				const decision = decisions.get(changeIdStr);

				if (decision === 'accepted' && !addedMap.has(changeIdStr)) {
					const sel = this.planReviewService.newDescriptionSelectionFor(dc.changeId);
					if (!sel?.planDescriptionTemplate || !dc.datasetId) continue;
					this.acceptedNewDescription.emit({
						changeId: dc.changeId,
						datasetId: dc.datasetId,
						planDescriptionTemplate: sel.planDescriptionTemplate,
						label: dc.currentLabel ?? sel.planDescriptionTemplate.currentDescriptionTemplate?.label ?? '',
					});
				} else if ((decision === 'rejected' || decision === 'pending') && addedMap.has(changeIdStr)) {
					const descriptionId = addedMap.get(changeIdStr);
					if (!descriptionId) continue;
					this.addedToFormMap.update(m => { const next = new Map(m); next.delete(changeIdStr); return next; });
					this.revertedNewDescription.emit({ changeId: dc.changeId, descriptionId });
				}
			}

			// Handle DELETE-kind transitions
			const deleteChanges = this.planReviewService.deleteDescriptionChanges();
			const acceptedDeletes = this.acceptedDeleteIds();
			for (const dc of deleteChanges) {
				if (!dc.changeId || !dc.descriptionId) continue;
				const changeIdStr = dc.changeId.toString();
				const decision = decisions.get(changeIdStr);

				if (decision === 'accepted' && !acceptedDeletes.has(changeIdStr)) {
					this.acceptedDeleteIds.update(s => new Set([...s, changeIdStr]));
					this.acceptedDeleteDescription.emit({ changeId: dc.changeId, descriptionId: dc.descriptionId });
				} else if (decision !== 'accepted' && acceptedDeletes.has(changeIdStr)) {
					this.acceptedDeleteIds.update(s => { const next = new Set(s); next.delete(changeIdStr); return next; });
					this.revertedDeleteDescription.emit({ changeId: dc.changeId, descriptionId: dc.descriptionId });
				}
			}
		}, { injector: this.injector });
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['planDescriptionTemplates']) {
			this.planReviewService.hasDescriptionTemplates.set((this.planDescriptionTemplates?.length ?? 0) > 0);
		}
	}

	sectionLabelFor(sectionId: Guid | string): string {
		return this.blueprintSections.find(s => s.id?.toString() === sectionId?.toString())?.label ?? '';
	}

	planDescriptionTemplateLabel(pdt: PlanDescriptionTemplate): string {
		const section = this.sectionLabelFor(pdt.sectionId);
		const template = pdt.currentDescriptionTemplate?.label ?? '';
		return section ? `${section} — ${template}` : template;
	}

	onAcceptAll(): void {
		this.planReviewService.acceptAll();
	}

	onRejectAll(): void {
		this.planReviewService.rejectAll();
	}

	onResetAll(): void {
		this.planReviewService.resetAll();
	}

	toggleExpanded(): void {
		this.expanded.update(v => !v);
	}

	readonly overviewRows = computed<OverviewRow[]>(() => {
		const preview = this.planReviewService.preview();
		if (!preview) return [];
		// Subscribe to displayMeta so the rows recompute as soon as a field editor mounts
		// and registers its label + ordinal.
		this.planReviewService.displayMeta();
		const rows: OverviewRow[] = [];

		for (const change of preview.planFieldChanges ?? []) {
			if (!change.changeId) continue;
			const meta = this.planReviewService.displayMetaFor(change.changeId);
			rows.push({
				changeId: change.changeId,
				ordinal: meta?.ordinal ?? null,
				label: meta?.label ?? this.fallbackLabel(change),
				currentValue: change.currentValue,
				incomingValue: change.incomingValue,
				state: this.planReviewService.decisionFor(change.changeId),
				scope: 'plan',
				anchorElementId: change.fieldId ?? undefined,
			});
		}

		for (const dc of preview.descriptionChanges ?? []) {
			if (dc.kind !== DescriptionChangeKind.Update || !dc.fieldChanges) continue;
			for (const fc of dc.fieldChanges) {
				if (!fc.changeId) continue;
				const meta = this.planReviewService.displayMetaFor(fc.changeId);
				rows.push({
					changeId: fc.changeId,
					ordinal: meta?.ordinal ?? null,
					label: meta?.label ?? this.fallbackLabel(fc),
					currentValue: fc.currentValue,
					incomingValue: fc.incomingValue,
					state: this.planReviewService.decisionFor(fc.changeId),
					scope: 'description',
					containerLabel: meta?.containerLabel ?? dc.currentLabel ?? null,
					anchorElementId: dc.descriptionId
						? `${dc.descriptionId.toString()}:${fc.fieldId ?? ''}`
						: undefined,
				});
			}
		}

		return rows;
	});

	readonly pendingRows = computed(() => this.overviewRows().filter(r => r.state === 'pending'));
	readonly acceptedRows = computed(() => this.overviewRows().filter(r => r.state === 'accepted'));
	readonly rejectedRows = computed(() => this.overviewRows().filter(r => r.state === 'rejected'));

	scrollToRow(row: OverviewRow): void {
		if (!row.anchorElementId) return;
		// Plan-level fields have [id]="field.id" anchors. Description-level rows are not
		// individually addressable today (the form-field component doesn't render a unique id
		// per qualified field), so this is a best-effort scroll.
		const target = document.getElementById(row.anchorElementId);
		if (target) {
			target.scrollIntoView({ behavior: 'smooth', block: 'center' });
		}
	}

	displayValue(value: FieldValuePreview | undefined): string {
		return fieldValueDisplayText(value);
	}

	getNewDescriptionSelection(change: DescriptionChange): NewDescriptionSelection {
		return this.planReviewService.newDescriptionSelectionFor(change.changeId) ?? { planDescriptionTemplate: null };
	}

	setNewDescriptionPlanTemplate(change: DescriptionChange, pdt: PlanDescriptionTemplate | null): void {
		this.planReviewService.setNewDescriptionSelection(change.changeId, { planDescriptionTemplate: pdt });
	}

	newDescriptionState(change: DescriptionChange): ReviewDecision {
		return this.planReviewService.decisionFor(change.changeId);
	}

	onAcceptNewDescription(change: DescriptionChange): void {
		const sel = this.getNewDescriptionSelection(change);
		if (!sel.planDescriptionTemplate || !change.datasetId) return;
		const changeIdStr = change.changeId?.toString();
		if (!changeIdStr || this.addedToFormMap().has(changeIdStr)) return;
		// Mark immediately so the decisions-effect skips this entry (real id set by registerAddedDescription)
		this.addedToFormMap.update(m => { const next = new Map(m); next.set(changeIdStr, Guid.parse(Guid.EMPTY)); return next; });
		this.planReviewService.accept(change.changeId);
		this.acceptedNewDescription.emit({
			changeId: change.changeId,
			datasetId: change.datasetId,
			planDescriptionTemplate: sel.planDescriptionTemplate,
			label: change.currentLabel ?? sel.planDescriptionTemplate.currentDescriptionTemplate?.label ?? '',
		});
	}

	onDiscardNewDescription(change: DescriptionChange): void {
		this.planReviewService.reject(change.changeId);
	}

	/** Called by the plan editor after it created the form entry, so the bar can store the temp id. */
	registerAddedDescription(changeId: Guid, descriptionId: Guid): void {
		const changeIdStr = changeId?.toString();
		if (!changeIdStr) return;
		this.addedToFormMap.update(m => new Map([...m, [changeIdStr, descriptionId]]));
	}

	onRevertNewDescription(change: DescriptionChange): void {
		const changeIdStr = change.changeId?.toString();
		if (!changeIdStr) return;
		const descriptionId = this.addedToFormMap().get(changeIdStr);
		if (!descriptionId) return;
		this.addedToFormMap.update(m => { const next = new Map(m); next.delete(changeIdStr); return next; });
		this.planReviewService.resetToPending(change.changeId);
		this.revertedNewDescription.emit({ changeId: change.changeId, descriptionId });
	}

	onUnDiscardNewDescription(change: DescriptionChange): void {
		this.planReviewService.resetToPending(change.changeId);
	}

	deleteDescriptionState(change: DescriptionChange): ReviewDecision {
		return this.planReviewService.decisionFor(change.changeId);
	}

	onAcceptDeleteDescription(change: DescriptionChange): void {
		this.planReviewService.accept(change.changeId);
	}

	onKeepDescription(change: DescriptionChange): void {
		this.planReviewService.reject(change.changeId);
	}

	onUndoAcceptDeleteDescription(change: DescriptionChange): void {
		this.planReviewService.resetToPending(change.changeId);
	}

	onUndoKeepDescription(change: DescriptionChange): void {
		this.planReviewService.resetToPending(change.changeId);
	}

	private fallbackLabel(change: FieldChange): string {
		return change.fieldId ?? '';
	}

	trackByChangeId = (_: number, row: OverviewRow): string => row.changeId.toString();
	trackByDescriptionChangeId = (_: number, change: DescriptionChange): string => change.changeId?.toString();
}
