import { DecimalPipe, formatNumber } from '@angular/common';
import { ChangeDetectionStrategy, Component, LOCALE_ID, computed, inject, input, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { Report, ReportCrossTabRow, ReportDistributionEntry, ReportQuestionResult } from '@app/core/model/report/report';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

/** The columns of one question's pivot: its most common answers, then the state columns. */
interface Pivot {
	question: ReportQuestionResult;
	valueColumns: string[];
	rows: PivotRow[];
	total: PivotRow;
	maxCell: number;
}

interface PivotRow {
	dimensionValues: string[];
	subjects: number;
	answered: number;
	notAnswered: number;
	cells: number[];
}

const PIVOT_COLUMNS = 5;
/** Rows shown before a long tail is folded behind "show all". */
const VISIBLE_ROWS = 12;

/**
 * Renders a finished report so that the answer comes first: a sentence a person can paste
 * into an email, then the distribution beside the answer states, then — when a breakdown
 * was asked for — a pivot table with the breakdown as rows and the most common answers as
 * columns, which is the shape that goes into a slide.
 *
 * <p>Bars are drawn in CSS rather than by a chart library: the numbers are the deliverable,
 * and a bar beside each row reads the same on screen, in print and in a screenshot.</p>
 */
@Component({
	selector: 'app-report-results',
	standalone: true,
	imports: [DecimalPipe, MatIconModule, MatTooltipModule, TranslateModule],
	templateUrl: './report-results.component.html',
	styleUrl: './report-results.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportResultsComponent {

	private readonly language = inject(TranslateService);
	private readonly locale = inject(LOCALE_ID);

	readonly report = input<Report>(null);

	protected readonly questions = computed(() => this.report()?.questions ?? []);
	protected readonly dimensions = computed(() => this.report()?.dimensions ?? []);
	protected readonly crossTab = computed(() => this.report()?.crossTab ?? []);
	protected readonly isPlanSubject = computed(() => this.report()?.subject === ReportSubjectType.Plan);

	protected readonly hasNotMigrated = computed(() =>
		this.questions().some(x => (x.states?.notMigrated ?? 0) > 0));

	protected readonly notMigratedCount = computed(() =>
		Math.max(...this.questions().map(x => x.states?.notMigrated ?? 0), 0));

	/**
	 * The report in one sentence: the scope, then how many answered and what they most often
	 * said. With several questions each section carries its own sentence and the scope is in
	 * the header and the scope notes, so there is no lead; the scope clause alone is not a
	 * sentence in every language.
	 */
	protected readonly lead = computed<string>(() => {
		const report = this.report();
		if (!report || report.questions?.length !== 1) return '';
		const plans = this.language.instant(this.plural('REPORTS.LEAD.PLANS', report.distinctPlans), { count: this.format(report.distinctPlans) });
		const count = this.format(report.subjects);
		const key = report.subject === ReportSubjectType.Plan
			? (report.sourceSpansAllGroups ? 'REPORTS.LEAD.PLANS-ALL' : 'REPORTS.LEAD.PLANS-ON')
			: (report.sourceSpansAllGroups ? 'REPORTS.LEAD.ASSETS-ALL' : 'REPORTS.LEAD.ASSETS-ON');
		const scope = this.language.instant(this.plural(key, report.subjects), { count, source: report.sourceLabel, plans });

		return scope + ' ' + this.answerSentence(report.questions[0]);
	});

	protected readonly pivots = computed<Pivot[]>(() => {
		if (this.dimensions().length === 0 || this.crossTab().length === 0) return [];
		return this.questions().map(question => this.pivotOf(question));
	});

	/** {@code alone} for a question's own section, where the sentence does not continue the lead. */
	protected answerSentence(question: ReportQuestionResult, alone = false): string {
		const answered = question.states?.answered ?? 0;
		if (answered === 0) return this.language.instant(alone ? 'REPORTS.LEAD.NONE-ANSWERED-ALONE' : 'REPORTS.LEAD.NONE-ANSWERED');

		let sentence = this.language.instant(alone ? 'REPORTS.LEAD.ANSWERED-ALONE' : 'REPORTS.LEAD.ANSWERED', { count: this.format(answered) });
		const top = (question.distribution ?? [])[0];
		const second = (question.distribution ?? [])[1];
		if (top) sentence += ' ' + this.language.instant('REPORTS.LEAD.TOP', { value: top.value, count: this.format(top.count) });
		if (second) {
			// Some languages open this clause with a comma, which must hug the previous word.
			const clause = this.language.instant('REPORTS.LEAD.SECOND', { value: second.value, count: this.format(second.count) });
			sentence += (/^[,;]/.test(clause) ? '' : ' ') + clause;
		} else if (top) {
			sentence += '.';
		}
		return sentence.trim();
	}

	/** The -ONE variant of a key for a count of one, as elsewhere in the app. */
	protected plural(key: string, count: number): string {
		return count === 1 ? key + '-ONE' : key;
	}

	/** Counts inside translated sentences, grouped the way the tables' number pipe groups them. */
	private format(value: number): string {
		return formatNumber(value ?? 0, this.locale);
	}

	/** Questions whose full distribution the user asked to see. */
	protected readonly expanded = signal<Set<string>>(new Set());

	protected visibleRows(question: ReportQuestionResult): ReportDistributionEntry[] {
		const all = question.distribution ?? [];
		return this.expanded().has(question.key) || all.length <= VISIBLE_ROWS + 2 ? all : all.slice(0, VISIBLE_ROWS);
	}

	protected hiddenRows(question: ReportQuestionResult): number {
		return (question.distribution?.length ?? 0) - this.visibleRows(question).length;
	}

	protected toggleExpanded(question: ReportQuestionResult): void {
		const next = new Set(this.expanded());
		if (next.has(question.key)) next.delete(question.key);
		else next.add(question.key);
		this.expanded.set(next);
	}

	/** Widest count in a question's distribution, so every bar shares one scale. */
	protected maxCount(question: ReportQuestionResult): number {
		return Math.max(...question.distribution.map(x => x.count), 1);
	}

	protected barWidth(entry: ReportDistributionEntry, question: ReportQuestionResult): number {
		return Math.round((entry.count / this.maxCount(question)) * 100);
	}

	/** Cell shading as a share of the primary colour, so the heaviest cell reads first. */
	protected heat(value: number, max: number): number {
		if (!value || !max) return 0;
		return Math.round(Math.min(55, 8 + (value / max) * 47));
	}

	private pivotOf(question: ReportQuestionResult): Pivot {
		const valueColumns = (question.distribution ?? []).slice(0, PIVOT_COLUMNS).map(x => x.value);
		const rows = this.crossTab().map(row => this.pivotRow(row, question.key, valueColumns));
		const total: PivotRow = {
			dimensionValues: [],
			subjects: this.report().subjects,
			answered: question.states?.answered ?? 0,
			notAnswered: question.states?.notAnswered ?? 0,
			cells: valueColumns.map(value => question.distribution.find(x => x.value === value)?.count ?? 0)
		};
		const maxCell = Math.max(...rows.flatMap(x => x.cells), 1);
		return { question, valueColumns, rows, total, maxCell };
	}

	private pivotRow(row: ReportCrossTabRow, key: string, valueColumns: string[]): PivotRow {
		const cell = row.questions.find(x => x.key === key);
		return {
			dimensionValues: row.dimensionValues,
			subjects: row.subjects,
			answered: cell?.states?.answered ?? 0,
			notAnswered: cell?.states?.notAnswered ?? 0,
			cells: valueColumns.map(value => cell?.distribution?.find(x => x.value === value)?.count ?? 0)
		};
	}
}
