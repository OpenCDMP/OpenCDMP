import { Component, ElementRef, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { RankResultModel, EvaluationResultModel, EvaluationResultMetricModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { SuccessStatus } from '@app/core/model/evaluator/evaluator-success-status.model';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { RankType } from '@app/core/model/evaluator/rank-type';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { Guid } from '@common/types/guid';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { NgTemplateOutlet, LowerCasePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-evaluate-dialog',
    templateUrl: './evaluate-dialog.component.html',
    styleUrl: './evaluate-dialog.component.scss',
    // Drives the fixed sizing below, so switching tabs cannot resize the dialog.
    changeDetection: ChangeDetectionStrategy.Eager,
    host: { '[class.tabbed]': 'hasResultTabs' },
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, NgTemplateOutlet, LowerCasePipe, TranslatePipe]
})
export class EvaluateDialogComponent {
    // Injecting the dialog data into the component
    constructor(
      public dialogRef: MatDialogRef<EvaluateDialogComponent>,
      public enumUtils: EnumUtils,
      private routerUtils: RouterUtilsService,
      private elementRef: ElementRef<HTMLElement>,
      @Inject(MAT_DIALOG_DATA) public data: { rankData: RankResultModel[], rankConfig: RankConfig, evaluatorId: string, planId?: Guid },
    ) { }

    get isValueRangeRankType(): boolean{
      return this.data?.rankConfig?.rankType === RankType.ValueRange;
    }

    get isSelectionRankType(): boolean{
      return this.data?.rankConfig?.rankType === RankType.Selection;
    }

    get rankResults(): RankResultModel[] {
      return this.data?.rankData ?? [];
    }

    selectionRankResultFor(rankResult: RankResultModel): SuccessStatus{
      return this.data?.rankConfig?.selectionConfiguration?.valueSetList?.find(x => x.key == rankResult?.rank)?.successStatus || null;
    }

    get evaluatorId() {
        return this.data?.evaluatorId;
    }

    SuccessStatus = SuccessStatus;

    // ---- Result tabs ----------------------------------------------------
    // A rank result per evaluated entity. An evaluator that assesses each asset of a plan
    // returns one per asset. More than one is shown as a pill tablist instead of stacked
    // blocks, so a long benchmark report does not bury the results that follow it.
    private activeIndex = 0;

    get selectedIndex(): number {
        const count = this.rankResults.length;
        if (count === 0) { return 0; }
        return Math.min(Math.max(this.activeIndex, 0), count - 1);
    }

    get selectedRankResult(): RankResultModel {
        return this.rankResults[this.selectedIndex] ?? null;
    }

    get hasResultTabs(): boolean {
        return this.rankResults.length > 1;
    }

    select(index: number): void {
        const count = this.rankResults.length;
        if (count === 0) { return; }
        const next = Math.min(Math.max(index, 0), count - 1);
        if (next === this.activeIndex) { return; }
        this.activeIndex = next;
        // A new report starts at its own beginning, not at the previous scroll depth.
        this.elementRef.nativeElement.querySelector('[mat-dialog-content]')?.scrollTo({ top: 0 });
    }

    // Roving-tabindex keyboard support expected of a tablist.
    onTabsKeydown(event: KeyboardEvent, index: number): void {
        const count = this.rankResults.length;
        let target: number;
        switch (event.key) {
            case 'ArrowRight':
            case 'ArrowDown':
                target = (index + 1) % count;
                break;
            case 'ArrowLeft':
            case 'ArrowUp':
                target = (index - 1 + count) % count;
                break;
            case 'Home':
                target = 0;
                break;
            case 'End':
                target = count - 1;
                break;
            default:
                return;
        }
        event.preventDefault();
        this.select(target);
        this.elementRef.nativeElement
            .querySelectorAll<HTMLElement>('.rank-tab')
            ?.item(this.selectedIndex)?.focus();
    }

    // ---- What each result is about --------------------------------------
    // The evaluator reports which part of the plan a result covers and the backend resolves
    // the name, so the tab can say "Austrian Science Fund" rather than "Result 2". Absent for
    // whole-plan evaluators, and for evaluations recorded before evaluators could report it,
    // which is why every caller falls back to the ordinal.

    targetLabelFor(rankResult: RankResultModel): string {
        const label = rankResult?.target?.label;
        return label?.length > 0 ? label : null;
    }

    isAssetTarget(rankResult: RankResultModel): boolean {
        return rankResult?.target?.entityType === PluginEntityType.Description;
    }

    /**
     * Link to the asset a result covers, so a name that does not identify it on its own (an
     * asset labelled with its own DOI, say) is still traceable. Null when the dialog was
     * opened without plan context, or in a public view where this route does not apply.
     */
    targetUrlFor(rankResult: RankResultModel): string {
        const entityId = rankResult?.target?.entityId;
        if (!this.data?.planId || !entityId || !this.isAssetTarget(rankResult)) { return null; }
        return this.routerUtils.generateUrl(['/plans/overview/' + this.data.planId + '/assets/' + entityId]);
    }

    /**
     * The details line is the identifier the evaluator actually assessed. It is worth showing
     * unless it merely repeats the name above it, which happens when an asset is labelled
     * with its own DOI.
     */
    showDetailsFor(rankResult: RankResultModel): boolean {
        const details = rankResult?.details;
        if (!(details?.length > 0)) { return false; }
        return details.trim() !== this.targetLabelFor(rankResult)?.trim();
    }

    // ---- Not-evaluated handling ----------------------------------------
    // An asset with no persistent identifier cannot be assessed. Reporting that as a failure
    // told users their asset failed an assessment that never ran, so it carries its own
    // status and shows the evaluator's reason instead of a benchmark report.

    isNotEvaluated(rankResult: RankResultModel): boolean {
        return this.selectionRankResultFor(rankResult) === SuccessStatus.NotEvaluated;
    }

    // Card-level pass/fail derived from its metrics (failed if any metric failed).
    // Returns null when the benchmark has no metrics that carry a rank, so no chip is shown.
    resultPassed(result: EvaluationResultModel): boolean | null {
        const ranked = (result?.metrics ?? []).filter(m => this.metricPassed(m) !== null);
        if (!ranked.length) { return null; }
        return !ranked.some(m => this.metricPassed(m) === false);
    }

    // A metric rank of -1 means the evaluator ran the metric but it yields no verdict, so it
    // counts as neither a pass nor a failure.
    metricPassed(metric: EvaluationResultMetricModel): boolean | null {
        if (metric == null || metric.rank < 0) { return null; }
        return metric.rank === 1;
    }

    passedCountFor(rankResult: RankResultModel): number {
        return (rankResult?.results ?? []).filter(r => this.resultPassed(r) === true).length;
    }

    failedCountFor(rankResult: RankResultModel): number {
        return (rankResult?.results ?? []).filter(r => this.resultPassed(r) === false).length;
    }

    close(){
        this.dialogRef.close();
    }
}
