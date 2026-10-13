import { Injectable, computed, signal } from '@angular/core';
import { Guid } from '@common/types/guid';
import { PlanUpdateRequestApplyDecision } from '@app/core/common/enum/plan-update-request-apply-decision.enum';
import { DescriptionChangeKind } from '@app/core/common/enum/description-change-kind.enum';
import { PreviewKind } from '@app/core/common/enum/preview-kind.enum';
import {
	DescriptionChange,
	FieldChange,
	PlanUpdatePreview,
} from '@app/core/model/plan-update-request/plan-update-preview';
import { PlanUpdateRequestApply } from '@app/core/model/plan-update-request/plan-update-request';
import { PlanDescriptionTemplate } from '@app/core/model/plan/plan';

export interface NewDescriptionSelection {
	/** The chosen PlanDescriptionTemplate row (encodes both template + section). */
	planDescriptionTemplate: PlanDescriptionTemplate | null;
}

export type ReviewDecision = 'pending' | 'accepted' | 'rejected';

/**
 * Display metadata about a field whose value is in a preview diff. Supplied by the
 * field-editor component that owns the field (plan-level or description-level) when it
 * mounts, since the backend's preview doesn't carry user-facing labels or section
 * ordinals.
 */
export interface FieldDisplayMeta {
	/** Display ordinal e.g. "1.2" for plan-level, "2.1.3" for description-level. */
	ordinal: string | null;
	/** The field's display label. */
	label: string | null;
	/** Optional context label — e.g. the description's label for description-level fields. */
	containerLabel?: string | null;
}

@Injectable()
export class PlanReviewService {
	readonly preview = signal<PlanUpdatePreview | null>(null);
	readonly decisions = signal<Map<string, ReviewDecision>>(new Map());

	/** Per-changeId selection for ADD-kind description changes (template + section chosen by user). */
	readonly newDescriptionSelections = signal<Map<string, NewDescriptionSelection>>(new Map());

	/**
	 * Per-changeId capture of the form's pre-accept value. Set when a badge transitions
	 * pending -> accepted; consumed when it transitions accepted -> pending or accepted -> rejected
	 * to restore the form to its pre-accept state. Keyed by changeId string.
	 */
	private readonly capturedValues = new Map<string, unknown>();

	captureValue(changeId: Guid, value: unknown): void {
		this.capturedValues.set(changeId.toString(), value);
	}

	consumeCapturedValue(changeId: Guid): { hasValue: boolean; value: unknown } {
		const key = changeId.toString();
		if (!this.capturedValues.has(key)) return { hasValue: false, value: undefined };
		const value = this.capturedValues.get(key);
		this.capturedValues.delete(key);
		return { hasValue: true, value };
	}

	/**
	 * Display metadata per changeId, registered by field-editor components as they mount.
	 * The backend's FieldChange.fieldLabel is currently not populated, so the frontend
	 * supplies labels + ordinals from its blueprint / template definitions. The overview
	 * panel reads this map to show "1.2 — Title" instead of an opaque UUID.
	 */
	private readonly displayMetaSignal = signal<Map<string, FieldDisplayMeta>>(new Map());

	readonly displayMeta = computed(() => this.displayMetaSignal());

	registerDisplayMeta(changeId: Guid, meta: FieldDisplayMeta): void {
		if (!meta?.label && !meta?.ordinal) return;
		const key = changeId.toString();
		const current = this.displayMetaSignal();
		const existing = current.get(key);
		if (existing && existing.label === meta.label && existing.ordinal === meta.ordinal && existing.containerLabel === meta.containerLabel) return;
		const next = new Map(current);
		next.set(key, meta);
		this.displayMetaSignal.set(next);
	}

	displayMetaFor(changeId: Guid): FieldDisplayMeta | null {
		return this.displayMetaSignal().get(changeId.toString()) ?? null;
	}

	readonly active = computed(() => this.preview() != null);

	readonly planFieldChangeByFieldId = computed<Map<string, FieldChange>>(() => {
		const map = new Map<string, FieldChange>();
		const p = this.preview();
		if (!p?.planFieldChanges) return map;
		for (const change of p.planFieldChanges) {
			if (change.fieldId) map.set(change.fieldId, change);
		}
		return map;
	});

	readonly descriptionChangeByDescriptionId = computed<Map<string, DescriptionChange>>(() => {
		const map = new Map<string, DescriptionChange>();
		const p = this.preview();
		if (!p?.descriptionChanges) return map;
		for (const change of p.descriptionChanges) {
			if (change.descriptionId) map.set(change.descriptionId.toString(), change);
		}
		return map;
	});

	readonly descriptionFieldChangeByKey = computed<Map<string, FieldChange>>(() => {
		const map = new Map<string, FieldChange>();
		const p = this.preview();
		if (!p?.descriptionChanges) return map;
		for (const dc of p.descriptionChanges) {
			if (dc.kind !== DescriptionChangeKind.Update || !dc.descriptionId || !dc.fieldChanges) continue;
			for (const fc of dc.fieldChanges) {
				if (!fc.fieldId) continue;
				map.set(this.buildDescriptionFieldKey(dc.descriptionId, fc.fieldId), fc);
			}
		}
		return map;
	});

	/** Set by the review bar component; when false, ADD-kind entries are excluded from counts. */
	readonly hasDescriptionTemplates = signal(false);

	readonly addChangeIds = computed<Set<string>>(() => {
		const p = this.preview();
		const ids = new Set<string>();
		if (!p?.descriptionChanges) return ids;
		for (const dc of p.descriptionChanges) {
			if (dc.kind === DescriptionChangeKind.Add && dc.changeId) ids.add(dc.changeId.toString());
		}
		return ids;
	});

	readonly pendingCount = computed(() => this.countByState('pending'));
	readonly acceptedCount = computed(() => this.countByState('accepted'));
	readonly rejectedCount = computed(() => this.countByState('rejected'));
	readonly totalChangeCount = computed(() => this.decisions().size);

	setNewDescriptionSelection(changeId: Guid, selection: NewDescriptionSelection): void {
		const next = new Map(this.newDescriptionSelections());
		next.set(changeId.toString(), selection);
		this.newDescriptionSelections.set(next);
	}

	newDescriptionSelectionFor(changeId: Guid): NewDescriptionSelection | null {
		return this.newDescriptionSelections().get(changeId.toString()) ?? null;
	}

	readonly addDescriptionChanges = computed<DescriptionChange[]>(() => {
		const p = this.preview();
		if (!p?.descriptionChanges) return [];
		return p.descriptionChanges.filter(dc => dc.kind === DescriptionChangeKind.Add);
	});

	readonly deleteDescriptionChanges = computed<DescriptionChange[]>(() => {
		const p = this.preview();
		if (!p?.descriptionChanges) return [];
		return p.descriptionChanges.filter(dc => dc.kind === DescriptionChangeKind.Delete);
	});

	activate(preview: PlanUpdatePreview): void {
		const decisions = new Map<string, ReviewDecision>();
		this.collectChangeIds(preview).forEach(id => decisions.set(id, 'pending'));
		this.preview.set(preview);
		this.decisions.set(decisions);
		this.newDescriptionSelections.set(new Map());
	}

	clear(): void {
		this.preview.set(null);
		this.decisions.set(new Map());
		this.capturedValues.clear();
		this.displayMetaSignal.set(new Map());
		this.newDescriptionSelections.set(new Map());
	}

	decisionFor(changeId: Guid): ReviewDecision {
		return this.decisions().get(changeId.toString()) ?? 'pending';
	}

	accept(changeId: Guid): void {
		this.setDecision(changeId.toString(), 'accepted');
	}

	reject(changeId: Guid): void {
		this.setDecision(changeId.toString(), 'rejected');
	}

	resetToPending(changeId: Guid): void {
		this.setDecision(changeId.toString(), 'pending');
	}

	acceptAll(): void {
		const addChangeIds = new Set(
			(this.preview()?.descriptionChanges ?? [])
				.filter(dc => dc.kind === DescriptionChangeKind.Add && dc.changeId)
				.map(dc => dc.changeId.toString())
		);
		const next = new Map<string, ReviewDecision>();
		for (const [id, current] of this.decisions()) {
			// ADD entries stay pending if no template is selected yet
			if (addChangeIds.has(id) && !this.newDescriptionSelections().get(id)?.planDescriptionTemplate) {
				next.set(id, current);
			} else {
				next.set(id, 'accepted');
			}
		}
		this.decisions.set(next);
	}

	rejectAll(): void {
		const next = new Map<string, ReviewDecision>();
		for (const id of this.decisions().keys()) next.set(id, 'rejected');
		this.decisions.set(next);
	}

	resetAll(): void {
		const next = new Map<string, ReviewDecision>();
		for (const id of this.decisions().keys()) next.set(id, 'pending');
		this.decisions.set(next);
	}

	buildApply(): PlanUpdateRequestApply | null {
		const preview = this.preview();
		if (!preview) return null;

		const decisions = this.decisions();
		const accepted: Guid[] = [];
		let rejectedAny = false;
		for (const [changeId, state] of decisions) {
			if (state === 'accepted') accepted.push(Guid.parse(changeId));
			else if (state === 'rejected') rejectedAny = true;
		}

		const total = decisions.size;
		let decision: PlanUpdateRequestApplyDecision;
		if (accepted.length === 0) {
			decision = PlanUpdateRequestApplyDecision.Refuse;
		} else if (accepted.length === total && !rejectedAny) {
			decision = PlanUpdateRequestApplyDecision.Accept;
		} else {
			decision = PlanUpdateRequestApplyDecision.PartialAccept;
		}

		// Collect template selections for ADD-kind description changes.
		const newDescriptionTemplateSelections: { [changeId: string]: Guid } = {};
		for (const [changeId, sel] of this.newDescriptionSelections()) {
			const templateId = sel.planDescriptionTemplate?.currentDescriptionTemplate?.id;
			if (templateId) newDescriptionTemplateSelections[changeId] = templateId;
		}

		return {
			planUpdateRequestId: preview.planUpdateRequestId,
			decision,
			acceptedChangeIds: decision === PlanUpdateRequestApplyDecision.PartialAccept ? accepted : undefined,
			newDescriptionTemplateSelections: Object.keys(newDescriptionTemplateSelections).length > 0 ? newDescriptionTemplateSelections : undefined,
			previewPlanVersion: preview.planVersion,
		};
	}

	isUpdateKind(): boolean {
		return this.preview()?.kind === PreviewKind.Update;
	}

	private setDecision(changeId: string, state: ReviewDecision): void {
		const current = this.decisions();
		if (!current.has(changeId)) return;
		const next = new Map(current);
		next.set(changeId, state);
		this.decisions.set(next);
	}

	private countByState(state: ReviewDecision): number {
		const skipAddIds = !this.hasDescriptionTemplates() ? this.addChangeIds() : null;
		let count = 0;
		for (const [id, v] of this.decisions()) {
			if (skipAddIds?.has(id)) continue;
			if (v === state) count++;
		}
		return count;
	}

	private buildDescriptionFieldKey(descriptionId: Guid, fieldId: string): string {
		return `${descriptionId.toString()}:${fieldId}`;
	}

	private collectChangeIds(preview: PlanUpdatePreview): string[] {
		const ids: string[] = [];
		if (preview.planFieldChanges) {
			for (const c of preview.planFieldChanges) if (c.changeId) ids.push(c.changeId.toString());
		}
		if (preview.descriptionChanges) {
			for (const dc of preview.descriptionChanges) {
				if (dc.changeId) ids.push(dc.changeId.toString());
				if (dc.fieldChanges) {
					for (const fc of dc.fieldChanges) if (fc.changeId) ids.push(fc.changeId.toString());
				}
			}
		}
		return ids;
	}
}
