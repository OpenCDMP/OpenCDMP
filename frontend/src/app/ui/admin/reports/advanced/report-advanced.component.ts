import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReportDateBucket } from '@app/core/common/enum/report-date-bucket.enum';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { ReportOutline, ReportOutlineField, SelectedQuestion } from '@app/core/model/report/report';
import { TranslateModule } from '@ngx-translate/core';
import { ReportQuestionPickerComponent } from '../question-picker/report-question-picker.component';
import { ReportQuestionRef, fieldTypeWord, isControlField, questionLabel } from '../report-state';

/**
 * Everything about a report that only matters once in a while, folded away under one
 * heading: which fields of a question are counted, whether superseded plan versions and
 * deleted items count, how dates are bucketed, and — for asset reports narrowed to one
 * blueprint — which of the plan's own answers can serve as a breakdown.
 *
 * <p>These were first-class controls before; here they are still one click away, but no
 * longer stand between a person and the number they came for.</p>
 */
@Component({
	selector: 'app-report-advanced',
	standalone: true,
	imports: [
		MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatProgressSpinnerModule, MatSelectModule, MatTooltipModule,
		TranslateModule, ReportQuestionPickerComponent
	],
	templateUrl: './report-advanced.component.html',
	styleUrl: './report-advanced.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportAdvancedComponent {

	readonly subject = input.required<ReportSubjectType>();
	readonly questions = input.required<ReportQuestionRef[]>();
	readonly latestVersionOnly = input<boolean>(true);
	readonly includeInactive = input<boolean>(false);
	readonly dateBucket = input<ReportDateBucket>(ReportDateBucket.Year);
	/** Only when a date is being counted or broken down by; otherwise the knob is noise. */
	readonly showDateBucket = input<boolean>(false);
	readonly planOutline = input<ReportOutline>(null);
	readonly loadingPlanOutline = input<boolean>(false);
	readonly planQuestions = input<SelectedQuestion[]>([]);

	readonly questionsChange = output<ReportQuestionRef[]>();
	readonly latestVersionOnlyChange = output<boolean>();
	readonly includeInactiveChange = output<boolean>();
	readonly dateBucketChange = output<ReportDateBucket>();
	readonly planQuestionsChange = output<SelectedQuestion[]>();

	protected readonly dateBuckets = ReportDateBucket;
	protected readonly open = signal<boolean>(false);

	protected readonly isAssetSubject = computed(() => this.subject() === ReportSubjectType.Description);
	protected readonly fieldSetQuestions = computed(() => this.questions().filter(x => x.kind === 'fieldset' && (x.allFields?.length ?? 0) > 0));
	protected readonly semanticQuestions = computed(() => this.questions().filter(x => x.kind === 'semantic'));

	/** A one-line summary for the collapsed state, so the fold says what it hides. */
	protected readonly summary = computed(() => {
		const counted = this.fieldSetQuestions().reduce((sum, q) => sum + (q.fieldIds?.length ?? 0), 0);
		const total = this.fieldSetQuestions().reduce((sum, q) => sum + (q.allFields?.length ?? 0), 0);
		return { counted, total };
	});

	protected toggle(): void {
		this.open.set(!this.open());
	}

	protected label(question: ReportQuestionRef): string {
		return questionLabel(question);
	}

	protected isCounted(question: ReportQuestionRef, field: ReportOutlineField): boolean {
		return (question.fieldIds ?? []).includes(field.id);
	}

	protected isExcludedControl(question: ReportQuestionRef, field: ReportOutlineField): boolean {
		return isControlField(field) && !this.isCounted(question, field);
	}

	protected typeWord(field: ReportOutlineField): string {
		return 'REPORTS.FIELD-TYPES.' + fieldTypeWord(field.fieldType);
	}

	protected toggleField(question: ReportQuestionRef, field: ReportOutlineField, checked: boolean): void {
		const current = question.fieldIds ?? [];
		const next = checked ? [...current, field.id] : current.filter(x => x !== field.id);
		// A question with nothing counted is no question; the last field cannot be unticked.
		if (next.length === 0) return;
		const ordered = (question.allFields ?? []).map(x => x.id).filter(x => next.includes(x));
		this.questionsChange.emit(this.questions().map(x => x === question ? { ...x, fieldIds: ordered } : x));
	}
}
