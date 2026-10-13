import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import { PlanService } from '@app/core/services/plan/plan.service';
import { DescriptionVersionDiff, PlanVersionDiff } from '@app/core/model/plan/plan-version-diff';
import { FieldChange, fieldValueDisplayText, FieldValuePreview } from '@app/core/model/plan-update-request/plan-update-preview';
import { DescriptionChangeKind } from '@app/core/common/enum/description-change-kind.enum';

export interface PlanVersionDiffDialogData {
	planId: Guid;
	otherPlanId: Guid;
}

@Component({
	selector: 'app-plan-version-diff-dialog',
	standalone: true,
	imports: [MatDialogModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslateModule],
	templateUrl: './plan-version-diff-dialog.component.html',
	styleUrl: './plan-version-diff-dialog.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlanVersionDiffDialogComponent {

	private readonly dialogRef = inject(MatDialogRef<PlanVersionDiffDialogComponent>);
	private readonly data = inject<PlanVersionDiffDialogData>(MAT_DIALOG_DATA);
	private readonly planService = inject(PlanService);
	private readonly translate = inject(TranslateService);

	protected readonly descriptionChangeKind = DescriptionChangeKind;

	protected readonly loading = signal(true);
	protected readonly failed = signal(false);
	protected readonly diff = signal<PlanVersionDiff | null>(null);

	constructor() {
		this.planService.versionDiff(this.data.planId, this.data.otherPlanId)
			.pipe(takeUntilDestroyed())
			.subscribe({
				next: (diff) => {
					this.diff.set(diff);
					this.loading.set(false);
				},
				error: () => {
					this.failed.set(true);
					this.loading.set(false);
				}
			});
	}

	protected get hasChanges(): boolean {
		const diff = this.diff();
		return !!diff && ((diff.planFieldChanges?.length ?? 0) > 0 || (diff.descriptionChanges?.length ?? 0) > 0);
	}

	protected fieldLabel(change: FieldChange): string {
		if (change.fieldLabel) return change.fieldLabel;
		if (change.fieldId?.startsWith('system.')) {
			const key = 'PLAN-VERSION-DIFF.SYSTEM-FIELDS.' + change.fieldId.substring('system.'.length).toUpperCase();
			const translated = this.translate.instant(key);
			if (translated !== key) return translated;
		}
		return change.fieldId;
	}

	protected valueText(value: FieldValuePreview | null | undefined): string {
		if (value == null) return this.translate.instant('PLAN-VERSION-DIFF.EMPTY-VALUE');
		return fieldValueDisplayText(value, 300);
	}

	protected descriptionTitle(change: DescriptionVersionDiff): string {
		return change.toLabel ?? change.fromLabel ?? '';
	}

	protected close(): void {
		this.dialogRef.close();
	}
}
