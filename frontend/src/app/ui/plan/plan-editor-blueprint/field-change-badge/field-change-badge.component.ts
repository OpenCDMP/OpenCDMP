import { ChangeDetectionStrategy, Component, Input, computed, effect, signal } from '@angular/core';
import { FieldChange, FieldValuePreview, fieldValueDisplayText } from '@app/core/model/plan-update-request/plan-update-preview';
import { PlanReviewService, ReviewDecision } from '../plan-review.service';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

export type FormValueReader = () => unknown;
export type FormValueWriter = (value: unknown) => void;

@Component({
    selector: 'app-field-change-badge',
    templateUrl: './field-change-badge.component.html',
    styleUrls: ['./field-change-badge.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [MatIcon, TranslatePipe],
})
export class FieldChangeBadgeComponent {
	private readonly _change = signal<FieldChange | null>(null);

	@Input()
	set fieldChange(value: FieldChange | null | undefined) {
		this._change.set(value ?? null);
	}
	get fieldChange(): FieldChange | null {
		return this._change();
	}

	/**
	 * Reads the current form-control value at this badge's anchor. Used to capture the
	 * pre-accept state so an undo can restore it. Should return the unwrapped value (the
	 * thing patchValue accepts, e.g. a string for a text control, a Date for a date control,
	 * an array for a FormArray-backed list).
	 */
	@Input() readFormValue: FormValueReader | null = null;

	/**
	 * Writes a value into the form control at this badge's anchor. Used to apply the incoming
	 * value when the user accepts, and to restore the captured value when the user undoes.
	 */
	@Input() writeFormValue: FormValueWriter | null = null;

	readonly state = computed<ReviewDecision | null>(() => {
		const change = this._change();
		if (!change?.changeId) return null;
		return this.planReviewService.decisionFor(change.changeId);
	});

	private previousState: ReviewDecision | null = null;

	constructor(public planReviewService: PlanReviewService) {
		// React to ANY decision change for our changeId, regardless of whether it was triggered
		// from this badge's buttons, from another badge, or from the action bar's accept-all /
		// reject-all. Each transition either patches the form (pending -> accepted) or restores
		// it (accepted -> pending|rejected).
		effect(() => {
			const change = this._change();
			const newState = this.state();
			if (!change?.changeId || newState == null) {
				this.previousState = newState;
				return;
			}
			const prev = this.previousState;
			this.previousState = newState;
			if (prev === newState) return;
			this.handleTransition(prev, newState);
		}, { allowSignalWrites: true });
	}

	accept(): void {
		const change = this._change();
		if (change?.changeId) this.planReviewService.accept(change.changeId);
	}

	reject(): void {
		const change = this._change();
		if (change?.changeId) this.planReviewService.reject(change.changeId);
	}

	reset(): void {
		const change = this._change();
		if (change?.changeId) this.planReviewService.resetToPending(change.changeId);
	}

	displayValue(value: FieldValuePreview | null | undefined): string {
		return fieldValueDisplayText(value);
	}

	private handleTransition(prev: ReviewDecision | null, next: ReviewDecision): void {
		const change = this._change();
		if (!change?.changeId) return;

		const transitionedIntoAccepted = next === 'accepted' && prev !== 'accepted';
		const transitionedOutOfAccepted = prev === 'accepted' && next !== 'accepted';

		if (transitionedIntoAccepted) {
			if (this.readFormValue && this.writeFormValue) {
				const current = this.readFormValue();
				this.planReviewService.captureValue(change.changeId, current);
				this.writeFormValue(change.incomingValue);
			}
			return;
		}

		if (transitionedOutOfAccepted) {
			if (this.writeFormValue) {
				const captured = this.planReviewService.consumeCapturedValue(change.changeId);
				if (captured.hasValue) {
					this.writeFormValue(captured.value);
				}
			}
		}
	}
}
