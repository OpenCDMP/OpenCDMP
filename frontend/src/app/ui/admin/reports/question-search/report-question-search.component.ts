import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { ReportQuestionHit, ReportQuestionSearch, ReportSemanticHit } from '@app/core/model/report/report';
import { ReportService } from '@app/core/services/report/report.service';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, filter, switchMap, tap } from 'rxjs/operators';
import { fieldTypeWord } from '../report-state';

/** Question hits and semantic hits requested per search; the dropdown shows fewer semantics. */
const RESULTS_PER_GROUP = 20;
const SEMANTIC_HITS = 6;

/** What a search hit resolves to when chosen. */
export type ReportQuestionSearchPick =
	| { kind: 'question', hit: ReportQuestionHit }
	| { kind: 'semantic', hit: ReportSemanticHit };

/**
 * The front door of a report: one box that finds a question by its number, a word of its
 * title or a field label, across every asset template and plan blueprint at once.
 *
 * <p>Semantics matching the term are offered first — a tag several templates share is the
 * only way to ask "where will the data be deposited" of every asset, whatever template it
 * was written on — then the questions of each template and blueprint.</p>
 */
@Component({
	selector: 'app-report-question-search',
	standalone: true,
	imports: [ReactiveFormsModule, MatAutocompleteModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, TranslateModule],
	templateUrl: './report-question-search.component.html',
	styleUrl: './report-question-search.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportQuestionSearchComponent implements OnInit {

	private readonly reportService = inject(ReportService);
	private readonly destroyRef = inject(DestroyRef);

	readonly installationWide = input<boolean>(false);
	readonly picked = output<ReportQuestionSearchPick>();

	protected readonly subjectTypes = ReportSubjectType;
	protected readonly term = new FormControl<string>('');
	protected readonly searching = signal<boolean>(false);
	protected readonly result = signal<ReportQuestionSearch>(null);
	protected readonly searched = signal<boolean>(false);

	protected readonly semanticHits = computed(() => (this.result()?.semantics ?? []).slice(0, SEMANTIC_HITS));
	protected readonly assetHits = computed(() => (this.result()?.questions ?? []).filter(x => x.subject === ReportSubjectType.Description));
	protected readonly planHits = computed(() => (this.result()?.questions ?? []).filter(x => x.subject === ReportSubjectType.Plan));
	protected readonly nothingFound = computed(() =>
		this.searched() && !this.searching() && this.semanticHits().length === 0 && this.assetHits().length === 0 && this.planHits().length === 0);

	ngOnInit(): void {
		this.term.valueChanges.pipe(
			debounceTime(300),
			filter(value => typeof value === 'string'),
			distinctUntilChanged(),
			tap(value => {
				if (!value?.trim()) {
					this.result.set(null);
					this.searched.set(false);
				}
			}),
			filter(value => !!value?.trim()),
			tap(() => this.searching.set(true)),
			switchMap(value => this.reportService.searchQuestions({ like: value.trim(), limit: RESULTS_PER_GROUP, installationWide: this.installationWide() }).pipe(
				catchError(() => of({ questions: [], semantics: [] } as ReportQuestionSearch)))),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(result => {
			this.result.set(result);
			this.searching.set(false);
			this.searched.set(true);
		});
	}

	protected onSelected(event: MatAutocompleteSelectedEvent): void {
		const pick = event.option.value as ReportQuestionSearchPick;
		if (!pick) return;
		this.term.setValue('', { emitEvent: false });
		this.result.set(null);
		this.searched.set(false);
		this.picked.emit(pick);
	}

	/** The box always shows what was typed, never the chosen option. */
	protected readonly displayNothing = (): string => '';

	protected questionPick(hit: ReportQuestionHit): ReportQuestionSearchPick {
		return { kind: 'question', hit };
	}

	protected semanticPick(hit: ReportSemanticHit): ReportQuestionSearchPick {
		return { kind: 'semantic', hit };
	}

	protected typeWords(hit: { fields?: { fieldType: string }[], fieldTypes?: string[] }): string[] {
		const types = hit.fieldTypes ?? (hit.fields ?? []).map(x => x.fieldType);
		return Array.from(new Set(types.filter(x => !!x).map(x => 'REPORTS.FIELD-TYPES.' + fieldTypeWord(x))));
	}
}
