import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, ParamMap, Params, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { ReportDateBucket } from '@app/core/common/enum/report-date-bucket.enum';
import { ReportDimensionType } from '@app/core/common/enum/report-dimension-type.enum';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { Report, ReportOutline, ReportOutlineFieldSet, ReportOutlineSection, ReportSemanticHit, ReportSourceHit, SelectedQuestion } from '@app/core/model/report/report';
import { DescriptionLookup } from '@app/core/query/description.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { DescriptionReferenceLookup, PlanReferenceLookup } from '@app/core/query/reference.lookup';
import { ReportDimensionSelection, ReportLookup, ReportQuestionSelection } from '@app/core/query/report.lookup';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ReportService } from '@app/core/services/report/report.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { Guid } from '@common/types/guid';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable, Subject, forkJoin, of } from 'rxjs';
import { catchError, debounceTime, map, switchMap, tap } from 'rxjs/operators';
import { ReportAdvancedComponent } from './advanced/report-advanced.component';
import { ReportFilterBarComponent } from './filter-bar/report-filter-bar.component';
import { ReportQuestionPickerComponent } from './question-picker/report-question-picker.component';
import { ReportQuestionSearchComponent, ReportQuestionSearchPick } from './question-search/report-question-search.component';
import { ReportResultsComponent } from './report-results/report-results.component';
import { BreakdownOption, ReportFilters, ReportQuestionRef, defaultCountedFields, emptyFilters, fromQuestionHit, fromSemanticHit, questionLabel } from './report-state';

/** Cards on the start page: the tags most templates share. */
const STARTING_POINTS = 6;

/**
 * Counts how a question was answered across the tenant — and starts from the question.
 *
 * <p>The page has two states. Before a question is chosen it is a search box over every
 * template and blueprint, with a browse route and a few ready-made starting points. Once
 * one is chosen the report runs at once with sensible defaults and the page becomes the
 * answer: a lead sentence, the distribution beside the answer states, the filters as chips
 * to refine by, and everything rarer folded under Advanced.</p>
 *
 * <p>The whole definition lives in the URL, so the report a funder asks for each quarter is
 * a bookmark rather than a procedure.</p>
 */
@Component({
	selector: 'app-reports',
	standalone: true,
	imports: [
		NgTemplateOutlet, ReactiveFormsModule, AutoCompleteModule,
		MatButtonModule, MatButtonToggleModule, MatCardModule, MatFormFieldModule, MatIconModule, MatProgressBarModule, MatProgressSpinnerModule, MatTooltipModule,
		TranslateModule,
		ReportQuestionSearchComponent, ReportFilterBarComponent, ReportAdvancedComponent, ReportResultsComponent, ReportQuestionPickerComponent
	],
	templateUrl: './reports.component.html',
	styleUrl: './reports.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportsComponent implements OnInit {

	private readonly reportService = inject(ReportService);
	private readonly authService = inject(AuthService);
	private readonly descriptionTemplateService = inject(DescriptionTemplateService);
	private readonly planBlueprintService = inject(PlanBlueprintService);
	private readonly router = inject(Router);
	private readonly route = inject(ActivatedRoute);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	private readonly fileUtils = inject(FileUtils);
	private readonly destroyRef = inject(DestroyRef);

	protected readonly subjectTypes = ReportSubjectType;

	//region report definition

	protected readonly subject = signal<ReportSubjectType>(ReportSubjectType.Description);
	protected readonly questions = signal<ReportQuestionRef[]>([]);
	protected readonly planQuestions = signal<SelectedQuestion[]>([]);
	protected readonly filters = signal<ReportFilters>(emptyFilters());
	protected readonly breakdown = signal<string[]>([]);
	protected readonly latestVersionOnly = signal<boolean>(true);
	protected readonly includeInactive = signal<boolean>(false);
	protected readonly dateBucket = signal<ReportDateBucket>(ReportDateBucket.Year);
	protected readonly installationWide = signal<boolean>(false);
	protected readonly canReportInstallation = this.authService.hasPermission(AppPermission.BrowseInstallationReport);

	protected readonly hasQuestions = computed(() => this.questions().length > 0);
	protected readonly isPlanSubject = computed(() => this.subject() === ReportSubjectType.Plan);
	protected readonly firstQuestion = computed(() => this.questions()[0] ?? null);

	/** Questions addressed by fieldset all come from one lineage; semantic ones span every lineage. */
	protected readonly sourceGroupId = computed<Guid>(() => this.questions().find(x => x.kind === 'fieldset')?.groupId ?? null);
	protected readonly sourceLabel = computed(() => this.questions().find(x => x.kind === 'fieldset')?.groupLabel ?? null);
	protected readonly sourceVersionCount = computed(() => this.questions().find(x => x.kind === 'fieldset')?.versionCount ?? 0);
	protected readonly spansAllGroups = computed(() => !this.sourceGroupId());
	protected readonly blueprintIsSource = computed(() => this.isPlanSubject() && !!this.sourceGroupId());

	protected readonly scopeText = computed(() => {
		const parts: string[] = [];
		if (this.installationWide()) parts.push(this.language.instant('REPORTS.SCOPE.EVERY-TENANT'));
		if (this.isPlanSubject()) {
			parts.push(this.spansAllGroups()
				? this.language.instant('REPORTS.SCOPE.PLANS-ALL')
				: this.language.instant('REPORTS.SCOPE.PLANS-ON', { source: this.sourceLabel() }));
		} else {
			parts.push(this.spansAllGroups()
				? this.language.instant('REPORTS.SCOPE.ASSETS-ALL')
				: this.language.instant('REPORTS.SCOPE.ASSETS-ON', { source: this.sourceLabel() }));
		}
		if (!this.spansAllGroups() && this.sourceVersionCount() > 1) {
			parts.push(this.language.instant('REPORTS.SCOPE.VERSIONS', { count: this.sourceVersionCount() }));
		}
		parts.push(this.language.instant(this.latestVersionOnly() ? 'REPORTS.SCOPE.LATEST-ONLY' : 'REPORTS.SCOPE.ALL-VERSIONS'));
		return parts.join(' · ');
	});

	/**
	 * Structural breakdowns, plus one entry per question. Question keys mirror the order the
	 * questions are sent in, so 'q:2' addresses the same question the engine calls q2 and
	 * 'p:0' the first plan question.
	 */
	protected readonly breakdownOptions = computed<BreakdownOption[]>(() => {
		const structural: ReportDimensionType[] = this.isPlanSubject()
			? [ReportDimensionType.PlanStatus, ReportDimensionType.PlanAccessType, ReportDimensionType.PlanBlueprint,
				ReportDimensionType.PlanVersionStatus, ReportDimensionType.CreatedPeriod, ReportDimensionType.FinalizedPeriod]
			: [ReportDimensionType.DescriptionStatus, ReportDimensionType.PlanStatus, ReportDimensionType.PlanAccessType,
				ReportDimensionType.PlanBlueprint, ReportDimensionType.TemplateVersion, ReportDimensionType.CreatedPeriod,
				ReportDimensionType.FinalizedPeriod];
		if (this.installationWide()) structural.unshift(ReportDimensionType.Tenant);

		const options: BreakdownOption[] = structural.map(type => ({
			token: 'd:' + type,
			label: this.language.instant('REPORTS.BREAKDOWN.OPTIONS.' + type)
		}));
		this.questions().forEach((question, index) => options.push({
			token: 'q:' + index,
			label: this.language.instant('REPORTS.BREAKDOWN.ANSWER-TO', { question: questionLabel(question) })
		}));
		this.planQuestions().forEach((question, index) => options.push({
			token: 'p:' + index,
			label: this.language.instant('REPORTS.BREAKDOWN.PLAN-ANSWER-TO', { question: question.label })
		}));
		return options;
	});

	/** Date grouping only matters when a date is counted or broken down by. */
	protected readonly showDateBucket = computed(() =>
		this.breakdown().some(x => x === 'd:' + ReportDimensionType.CreatedPeriod || x === 'd:' + ReportDimensionType.FinalizedPeriod)
		|| (this.report()?.questions ?? []).some(x => x.fieldType === 'datePicker'));

	//endregion

	//region result

	protected readonly report = signal<Report>(null);
	protected readonly running = signal<boolean>(false);
	protected readonly exporting = signal<boolean>(false);
	protected readonly planOutline = signal<ReportOutline>(null);
	protected readonly loadingPlanOutline = signal<boolean>(false);

	//endregion

	//region start view

	protected readonly startingPoints = signal<ReportSemanticHit[]>([]);
	protected readonly templateControl = new FormControl<Guid>(null);
	protected readonly blueprintControl = new FormControl<Guid>(null);
	protected readonly templateConfiguration = this.descriptionTemplateService.descriptionTemplateGroupAutocompleteConfiguration;
	protected readonly blueprintConfiguration = this.planBlueprintService.planBlueprintGroupSingleAutocompleteConfiguration;
	// Across tenants the pickers list every tenant's lineages, from the question search; the
	// template and blueprint listings only ever show the current tenant's.
	private readonly pickedSources = new Map<string, ReportSourceHit>();
	protected readonly installationTemplateConfiguration = this.sourcePicker(ReportSubjectType.Description);
	protected readonly installationBlueprintConfiguration = this.sourcePicker(ReportSubjectType.Plan);
	protected readonly browseSubject = signal<ReportSubjectType>(ReportSubjectType.Description);
	protected readonly browseOutline = signal<ReportOutline>(null);
	protected readonly loadingBrowse = signal<boolean>(false);
	protected readonly browseSelection = signal<SelectedQuestion[]>([]);

	//endregion

	private readonly runRequests = new Subject<void>();
	private readonly queries = new Subject<ReportLookup>();

	ngOnInit(): void {
		// Every change re-runs the report; the debounce lets a multi-select settle first.
		this.runRequests.pipe(debounceTime(350), takeUntilDestroyed(this.destroyRef)).subscribe(() => this.run());
		// A newer run cancels the one in flight, so a slow earlier answer can never overwrite it.
		this.queries.pipe(
			switchMap(lookup => this.reportService.query(lookup).pipe(
				map(report => ({ report, error: null })),
				catchError(error => of({ report: null as Report, error })))),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({ report, error }) => {
			// On failure the previous numbers no longer match the chips on screen.
			this.report.set(report);
			this.running.set(false);
			if (error) this.onError(error);
		});

		this.templateControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(groupId => this.browse(ReportSubjectType.Description, groupId));
		this.blueprintControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(groupId => this.browse(ReportSubjectType.Plan, groupId));

		this.restoreFromUrl(this.route.snapshot.queryParamMap);
		this.loadStartingPoints();
	}

	//region choosing a question

	protected onPicked(pick: ReportQuestionSearchPick): void {
		const ref = pick.kind === 'question' ? fromQuestionHit(pick.hit) : fromSemanticHit(pick.hit);
		this.applyQuestions([ref], ref.subject);
	}

	protected onStartingPoint(hit: ReportSemanticHit): void {
		this.applyQuestions([fromSemanticHit(hit)], hit.subject);
	}

	protected runBrowse(): void {
		const outline = this.browseOutline();
		if (!outline || this.browseSelection().length === 0) return;

		const refs = this.browseSelection().map(selected => this.refFromOutline(outline, selected.fieldSetId, selected.fieldIds)).filter(x => !!x);
		this.applyQuestions(refs, this.browseSubject());
		this.templateControl.setValue(null, { emitEvent: false });
		this.blueprintControl.setValue(null, { emitEvent: false });
		this.browseOutline.set(null);
		this.browseSelection.set([]);
	}

	protected changeQuestion(): void {
		this.questions.set([]);
		this.planQuestions.set([]);
		this.planOutline.set(null);
		this.report.set(null);
		this.breakdown.set([]);
		// The start page shows no filter chips, so filters kept here would narrow the next report unseen.
		this.filters.set(emptyFilters());
		this.changed();
	}

	private applyQuestions(refs: ReportQuestionRef[], subject: ReportSubjectType): void {
		if (subject !== this.subject()) {
			// Asset-only filters have no meaning on a plan report; plan-level ones carry over.
			const current = this.filters();
			this.filters.set({ ...emptyFilters(), planStatusIds: current.planStatusIds, accessTypes: current.accessTypes, planReferenceIds: current.planReferenceIds });
			this.planQuestions.set([]);
			this.planOutline.set(null);
		}
		this.subject.set(subject);
		this.questions.set(refs);
		this.report.set(null);
		this.pruneBreakdowns();
		if (!this.isPlanSubject() && this.filters().planBlueprintGroupId && !this.planOutline()) this.loadPlanOutline(this.filters().planBlueprintGroupId);
		this.changed();
	}

	private browse(subject: ReportSubjectType, groupId: Guid): void {
		if (!groupId) {
			if (this.browseSubject() === subject) this.browseOutline.set(null);
			return;
		}
		// Only one of the two pickers can be in use; choosing in one clears the other.
		if (subject === ReportSubjectType.Description) this.blueprintControl.setValue(null, { emitEvent: false });
		else this.templateControl.setValue(null, { emitEvent: false });

		this.browseSubject.set(subject);
		this.browseSelection.set([]);
		this.loadingBrowse.set(true);
		this.outlineOf(subject, groupId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: outline => { this.browseOutline.set(outline); this.loadingBrowse.set(false); },
			error: error => { this.loadingBrowse.set(false); this.browseOutline.set(null); this.onError(error); }
		});
	}

	private sourcePicker(subject: ReportSubjectType): SingleAutoCompleteConfiguration {
		const search = (like: string) => this.reportService.searchQuestions({ like, subject, limit: 50, installationWide: true, includeSources: true }).pipe(
			map(result => (result.sources ?? []).filter(x => x.subject === subject)),
			tap(items => items.forEach(x => this.pickedSources.set(x.groupId.toString(), x))));
		const label = (item: ReportSourceHit) => item.tenantCode ? `${item.label} (${item.tenantCode})` : item.label;
		return {
			initialItems: () => search(''),
			filterFn: (searchQuery: string) => search(searchQuery),
			getSelectedItem: (groupId: Guid) => of(this.pickedSources.get(groupId?.toString())),
			displayFn: label,
			titleFn: label,
			subtitleFn: (item: ReportSourceHit) => this.language.instant(item.versionCount === 1 ? 'REPORTS.SEARCH.VERSIONS-ONE' : 'REPORTS.SEARCH.VERSIONS', { count: item.versionCount }),
			valueAssign: (item: ReportSourceHit) => item.groupId
		};
	}

	private loadStartingPoints(): void {
		this.reportService.searchQuestions({ like: '', limit: STARTING_POINTS, installationWide: this.installationWide() }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.startingPoints.set((result.semantics ?? []).filter(x => x.groupCount > 0)),
			error: () => this.startingPoints.set([])
		});
	}

	//endregion

	//region refining

	protected onFiltersChange(next: ReportFilters): void {
		const previousBlueprint = this.filters().planBlueprintGroupId ?? null;
		this.filters.set(next);
		if ((next.planBlueprintGroupId ?? null) !== previousBlueprint) {
			this.planQuestions.set([]);
			this.breakdown.set(this.breakdown().filter(x => !x.startsWith('p:')));
			this.loadPlanOutline(next.planBlueprintGroupId);
		}
		this.changed();
	}

	protected onBreakdownChange(next: string[]): void {
		this.breakdown.set(next);
		this.changed();
	}

	protected onQuestionsChange(next: ReportQuestionRef[]): void {
		this.questions.set(next);
		this.changed();
	}

	protected onPlanQuestionsChange(next: SelectedQuestion[]): void {
		this.planQuestions.set(next);
		this.pruneBreakdowns();
		this.changed();
	}

	protected onLatestVersionOnlyChange(value: boolean): void {
		this.latestVersionOnly.set(value);
		this.changed();
	}

	protected onIncludeInactiveChange(value: boolean): void {
		this.includeInactive.set(value);
		this.changed();
	}

	protected onInstallationWideChange(value: boolean): void {
		this.installationWide.set(value);
		// The pickers change with the scope, so a pick from the other scope's list goes.
		this.templateControl.setValue(null, { emitEvent: false });
		this.blueprintControl.setValue(null, { emitEvent: false });
		this.browseOutline.set(null);
		this.browseSelection.set([]);
		// The tenant breakdown only exists across tenants.
		this.pruneBreakdowns();
		this.loadStartingPoints();
		this.changed();

		// Which templates a semantic is read from depends on the scope.
		const current = this.questions();
		if (!current.some(x => x.kind === 'semantic')) return;
		forkJoin(current.map(x => x.kind === 'semantic' ? this.resolveSemantic(x.semantic, x.subject) : of(x)))
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(refs => {
				if (this.questions() === current) this.questions.set(refs);
			});
	}

	/** A semantic question as the search sees it in the current scope, or a bare one if it is not found. */
	private resolveSemantic(semantic: string, subject: ReportSubjectType): Observable<ReportQuestionRef> {
		const bare = { kind: 'semantic', subject, semantic, title: semantic } as ReportQuestionRef;
		// The search matches tags by substring, so a longer tag could take a single slot; ask for enough to find the exact one.
		return this.reportService.searchQuestions({ like: semantic, subject, limit: 30, installationWide: this.installationWide() }).pipe(
			map(result => {
				const hit = (result.semantics ?? []).find(x => x.semantic === semantic && x.subject === subject);
				return hit ? fromSemanticHit(hit) : bare;
			}),
			catchError(() => of(bare)));
	}

	protected onDateBucketChange(value: ReportDateBucket): void {
		this.dateBucket.set(value);
		this.changed();
	}

	/** Plan questions are only offered for asset reports narrowed to one blueprint. */
	private loadPlanOutline(groupId: Guid, restore?: string[]): void {
		if (!groupId || this.isPlanSubject()) {
			this.planOutline.set(null);
			return;
		}
		this.loadingPlanOutline.set(true);
		this.reportService.planBlueprintOutline(groupId, this.installationWide()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: outline => {
				this.planOutline.set(outline);
				this.loadingPlanOutline.set(false);
				if (restore?.length) {
					this.planQuestions.set(restore.map(x => this.selectedFromOutline(outline, x)).filter(x => !!x));
					this.pruneBreakdowns();
					this.changed();
				}
			},
			error: error => { this.loadingPlanOutline.set(false); this.onError(error); }
		});
	}

	/** A breakdown that pointed at a question no longer in the report has to go. */
	private pruneBreakdowns(): void {
		const valid = new Set(this.breakdownOptions().map(x => x.token));
		const kept = this.breakdown().filter(x => valid.has(x));
		if (kept.length !== this.breakdown().length) this.breakdown.set(kept);
	}

	//endregion

	//region running

	private changed(): void {
		this.writeUrl();
		if (this.hasQuestions()) this.runRequests.next();
	}

	private run(): void {
		if (!this.hasQuestions()) return;
		this.running.set(true);
		this.queries.next(this.buildLookup());
	}

	protected exportCsv(): void {
		this.exporting.set(true);
		this.reportService.exportCsv(this.buildLookup()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: response => {
				const blob = new Blob([response.body], { type: 'application/csv' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
				FileSaver.saveAs(blob, filename);
				this.exporting.set(false);
			},
			error: error => { this.exporting.set(false); this.onError(error); }
		});
	}

	protected copyLink(): void {
		navigator.clipboard.writeText(window.location.href).then(
			() => this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-COPY-TO-CLIPBOARD'), SnackBarNotificationLevel.Success),
			() => this.uiNotificationService.snackBarNotification(this.language.instant('REPORTS.ERRORS.COPY-FAILED'), SnackBarNotificationLevel.Error));
	}

	protected questionTitle(question: ReportQuestionRef): string {
		return question.title;
	}

	private buildLookup(): ReportLookup {
		const lookup = new ReportLookup();
		lookup.subject = this.subject();
		lookup.questions = this.questions().map(x => this.toSelection(x));
		lookup.latestVersionOnly = this.latestVersionOnly();
		lookup.includeInactive = this.includeInactive();
		lookup.dateBucket = this.dateBucket();
		lookup.installationWide = this.installationWide();
		lookup.groupBy = this.breakdown().map(token => this.toDimension(token)).filter(x => !!x);

		const filters = this.filters();
		if (this.isPlanSubject()) {
			lookup.planBlueprintGroupId = this.sourceGroupId() ?? filters.planBlueprintGroupId ?? undefined;
		} else {
			lookup.descriptionTemplateGroupId = this.sourceGroupId() ?? undefined;
			if (filters.planBlueprintGroupId) lookup.planBlueprintGroupId = filters.planBlueprintGroupId;
			if (this.planQuestions().length > 0) {
				lookup.planQuestions = this.planQuestions().map(x => ({ fieldSetId: x.fieldSetId, fieldIds: x.fieldIds, label: x.label }));
			}

			const descriptionLookup = new DescriptionLookup();
			let hasDescriptionFilter = false;
			if (filters.descriptionStatusIds.length > 0) { descriptionLookup.statusIds = filters.descriptionStatusIds; hasDescriptionFilter = true; }
			if (filters.createdAfter) { descriptionLookup.createdAfter = filters.createdAfter; hasDescriptionFilter = true; }
			if (filters.createdBefore) { descriptionLookup.createdBefore = filters.createdBefore; hasDescriptionFilter = true; }
			if (filters.descriptionReferenceIds.length > 0) {
				const referenceLookup = new DescriptionReferenceLookup();
				referenceLookup.referenceIds = filters.descriptionReferenceIds;
				referenceLookup.isActive = [IsActive.Active];
				descriptionLookup.descriptionReferenceSubQuery = referenceLookup;
				hasDescriptionFilter = true;
			}
			if (hasDescriptionFilter) lookup.descriptionSubQuery = descriptionLookup;
		}

		const planLookup = new PlanLookup();
		let hasPlanFilter = false;
		if (filters.planStatusIds.length > 0) { planLookup.statusIds = filters.planStatusIds; hasPlanFilter = true; }
		if (filters.accessTypes.length > 0) { planLookup.accessTypes = filters.accessTypes; hasPlanFilter = true; }
		if (filters.planReferenceIds.length > 0) {
			const referenceLookup = new PlanReferenceLookup();
			referenceLookup.referenceIds = filters.planReferenceIds;
			referenceLookup.isActive = [IsActive.Active];
			planLookup.planReferenceSubQuery = referenceLookup;
			hasPlanFilter = true;
		}
		if (hasPlanFilter) lookup.planSubQuery = planLookup;

		return lookup;
	}

	private toSelection(question: ReportQuestionRef): ReportQuestionSelection {
		return question.kind === 'semantic'
			? { semantic: question.semantic, label: question.semantic }
			: { fieldSetId: question.fieldSetId, fieldIds: question.fieldIds, label: questionLabel(question) };
	}

	private toDimension(token: string): ReportDimensionSelection {
		const [kind, value] = token.split(':');
		if (kind === 'd') return { type: Number(value) as ReportDimensionType };
		if (kind === 'q') return { type: ReportDimensionType.Question, questionKey: 'q' + value };
		if (kind === 'p') return { type: ReportDimensionType.PlanQuestion, questionKey: 'p' + value };
		return null;
	}

	//endregion

	//region url

	/**
	 * The report definition as query parameters — short keys, one value each, ids only. The
	 * labels are looked up again on load, so a link stays valid when a template is renamed.
	 */
	private writeUrl(): void {
		const params: Params = {};
		if (this.installationWide()) params['all'] = '1';
		if (this.isPlanSubject()) params['s'] = 'p';

		const fieldSetQuestions = this.questions().filter(x => x.kind === 'fieldset');
		const semanticQuestions = this.questions().filter(x => x.kind === 'semantic');
		if (fieldSetQuestions.length > 0) {
			params['g'] = fieldSetQuestions[0].groupId?.toString();
			params['q'] = fieldSetQuestions.map(x => x.fieldSetId + '~' + (x.fieldIds ?? []).join(','));
		}
		if (semanticQuestions.length > 0) params['sem'] = semanticQuestions.map(x => x.semantic);

		const filters = this.filters();
		if (filters.planBlueprintGroupId) params['pb'] = filters.planBlueprintGroupId.toString();
		if (this.planQuestions().length > 0) params['pq'] = this.planQuestions().map(x => x.fieldSetId + '~' + x.fieldIds.join(','));
		if (filters.descriptionStatusIds.length > 0) params['ds'] = filters.descriptionStatusIds.map(x => x.toString()).join(',');
		if (filters.planStatusIds.length > 0) params['ps'] = filters.planStatusIds.map(x => x.toString()).join(',');
		if (filters.accessTypes.length > 0) params['ac'] = filters.accessTypes.join(',');
		if (filters.planReferenceIds.length > 0) params['pr'] = filters.planReferenceIds.map(x => x.toString()).join(',');
		if (filters.descriptionReferenceIds.length > 0) params['dr'] = filters.descriptionReferenceIds.map(x => x.toString()).join(',');
		if (filters.createdAfter) params['ca'] = this.toDateParam(filters.createdAfter);
		if (filters.createdBefore) params['cb'] = this.toDateParam(filters.createdBefore);
		if (!this.latestVersionOnly()) params['lv'] = '0';
		if (this.includeInactive()) params['ia'] = '1';
		if (this.dateBucket() === ReportDateBucket.Month) params['db'] = 'm';
		if (this.breakdown().length > 0) params['by'] = this.breakdown().join(',');

		this.router.navigate([], { relativeTo: this.route, queryParams: params, replaceUrl: true });
	}

	private restoreFromUrl(params: ParamMap): void {
		const subject = params.get('s') === 'p' ? ReportSubjectType.Plan : ReportSubjectType.Description;
		this.subject.set(subject);
		this.installationWide.set(this.canReportInstallation && params.get('all') === '1');

		const filters = emptyFilters();
		filters.descriptionStatusIds = this.guidList(params.get('ds'));
		filters.planStatusIds = this.guidList(params.get('ps'));
		filters.accessTypes = (params.get('ac') ?? '').split(',').filter(x => x !== '').map(x => Number(x) as PlanAccessType);
		filters.planReferenceIds = this.guidList(params.get('pr'));
		filters.descriptionReferenceIds = this.guidList(params.get('dr'));
		filters.planBlueprintGroupId = params.get('pb') && Guid.isGuid(params.get('pb')) ? Guid.parse(params.get('pb')) : null;
		filters.createdAfter = this.fromDateParam(params.get('ca'));
		filters.createdBefore = this.fromDateParam(params.get('cb'));
		this.filters.set(filters);

		this.latestVersionOnly.set(params.get('lv') !== '0');
		this.includeInactive.set(params.get('ia') === '1');
		this.dateBucket.set(params.get('db') === 'm' ? ReportDateBucket.Month : ReportDateBucket.Year);
		this.breakdown.set((params.get('by') ?? '').split(',').filter(x => x !== ''));

		const groupId = params.get('g');
		const encodedQuestions = params.getAll('q');
		const semantics = params.getAll('sem');
		const loads: Observable<ReportQuestionRef[]>[] = [];

		if (groupId && Guid.isGuid(groupId) && encodedQuestions.length > 0) {
			loads.push(this.outlineOf(subject, Guid.parse(groupId)).pipe(
				map(outline => encodedQuestions.map(x => this.refFromEncoded(outline, x)).filter(x => !!x)),
				catchError(() => of([] as ReportQuestionRef[]))));
		}
		for (const semantic of semantics) {
			loads.push(this.resolveSemantic(semantic, subject).pipe(map(ref => [ref])));
		}
		if (loads.length === 0) return;

		forkJoin(loads).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(lists => {
			this.questions.set(lists.flat());
			this.pruneBreakdowns();
			if (!this.hasQuestions()) return;
			if (filters.planBlueprintGroupId) this.loadPlanOutline(filters.planBlueprintGroupId, params.getAll('pq'));
			this.run();
		});
	}

	private guidList(value: string): Guid[] {
		return (value ?? '').split(',').filter(x => Guid.isGuid(x)).map(x => Guid.parse(x));
	}

	private toDateParam(date: Date): string {
		const value = new Date(date);
		return `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`;
	}

	private fromDateParam(value: string): Date {
		if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return null;
		const [year, month, day] = value.split('-').map(x => Number(x));
		return new Date(year, month - 1, day);
	}

	//endregion

	//region outline helpers

	private outlineOf(subject: ReportSubjectType, groupId: Guid): Observable<ReportOutline> {
		return subject === ReportSubjectType.Plan
			? this.reportService.planBlueprintOutline(groupId, this.installationWide())
			: this.reportService.descriptionTemplateOutline(groupId, this.installationWide());
	}

	private refFromEncoded(outline: ReportOutline, encoded: string): ReportQuestionRef {
		const [fieldSetId, fieldIds] = encoded.split('~');
		return this.refFromOutline(outline, fieldSetId, (fieldIds ?? '').split(',').filter(x => x !== ''));
	}

	private refFromOutline(outline: ReportOutline, fieldSetId: string, fieldIds: string[]): ReportQuestionRef {
		const fieldSet = this.findFieldSet(outline.sections, fieldSetId);
		if (!fieldSet) return null;

		const known = (fieldSet.fields ?? []).map(x => x.id);
		let counted = (fieldIds ?? []).filter(x => known.includes(x));
		if (counted.length === 0) counted = defaultCountedFields(fieldSet.fields).map(x => x.id);
		return {
			kind: 'fieldset',
			subject: outline.subject,
			title: fieldSet.title,
			numbering: fieldSet.numbering,
			groupId: outline.groupId,
			groupLabel: outline.label,
			versionCount: outline.versionCount,
			fieldSetId: fieldSet.id,
			fieldIds: counted,
			allFields: fieldSet.fields ?? []
		};
	}

	private selectedFromOutline(outline: ReportOutline, encoded: string): SelectedQuestion {
		const ref = this.refFromEncoded(outline, encoded);
		if (!ref) return null;
		return { fieldSetId: ref.fieldSetId, fieldIds: ref.fieldIds, numbering: ref.numbering, label: questionLabel(ref) };
	}

	private findFieldSet(sections: ReportOutlineSection[], fieldSetId: string): ReportOutlineFieldSet {
		for (const section of sections ?? []) {
			const found = (section.fieldSets ?? []).find(x => x.id === fieldSetId) ?? this.findFieldSet(section.sections, fieldSetId);
			if (found) return found;
		}
		return null;
	}

	//endregion

	private onError(error: any): void {
		this.uiNotificationService.snackBarNotification(
			error?.error?.message ?? this.language.instant('REPORTS.ERRORS.RUN-FAILED'),
			SnackBarNotificationLevel.Error);
	}
}
