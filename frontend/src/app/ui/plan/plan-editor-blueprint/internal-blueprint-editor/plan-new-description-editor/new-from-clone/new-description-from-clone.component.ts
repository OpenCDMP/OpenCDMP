import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { QueryResult } from '@common/model/query-result';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { finalize, forkJoin, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { templateTypeStyle } from '../shared/template-type-style';

const PAGE_SIZE = 8;

/**
 * "Copy from an existing asset": the user's own and the public assets whose template is linked to
 * this section. Choosing one hands the source asset back to the host, which clones it.
 */
@Component({
	selector: 'app-new-description-from-clone',
	imports: [DatePipe, MatButtonModule, MatButtonToggleModule, MatIconModule, MatProgressBarModule, MatTooltipModule, TextFilterModule, TranslateModule],
	templateUrl: './new-description-from-clone.component.html',
	styleUrl: './new-description-from-clone.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewDescriptionFromCloneComponent {
	private readonly descriptionService = inject(DescriptionService);
	private readonly authService = inject(AuthService);
	private readonly filterService = inject(FilterService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);

	/** Group ids of the templates linked to the section — only assets using one of them can be copied. */
	sectionTemplateGroupIds = input<Guid[]>([]);
	maxMultiplicityIds = input<Guid[]>([]);
	canEditTemplateList = input<boolean>(false);

	copyDescription = output<Description>();
	navigateToParentSection = output<void>();

	protected readonly canClone = this.authService.hasPermission(AppPermission.CloneDescription);
	protected readonly canClonePublic = this.authService.hasPermission(AppPermission.PublicCloneDescription);

	protected readonly publicView = signal<boolean>(!this.canClone);
	protected readonly initialized = signal(false);
	protected readonly loading = signal(false);
	protected searchTerm = '';

	protected readonly descriptions = signal<Description[]>([]);
	protected readonly totalCount = signal(0);
	private offset = 0;

	protected readonly publicDescriptions = signal<Description[]>([]);
	protected readonly publicTotalCount = signal(0);
	private publicOffset = 0;

	protected readonly typeStyle = templateTypeStyle;

	private readonly refresh$ = new Subject<string>();

	constructor() {
		this.refresh$.pipe(
			tap(() => this.loading.set(true)),
			switchMap(term => forkJoin([
				this.canClone ? this.fetchMine(term) : of(null),
				this.canClonePublic ? this.fetchPublic(term) : of(null),
			])),
			takeUntilDestroyed(this.destroyRef),
		).subscribe({
			next: () => { this.loading.set(false); this.initialized.set(true); },
			error: error => { this.loading.set(false); this.initialized.set(true); this.httpErrorHandlingService.handleBackedRequestError(error); },
		});

		effect(() => {
			this.sectionTemplateGroupIds();
			untracked(() => this.reload(this.searchTerm));
		});
	}

	protected get hasSectionTemplates(): boolean {
		return !!this.sectionTemplateGroupIds()?.length;
	}

	protected get emptyMessage(): { key: string; params?: Record<string, unknown> } {
		if (!this.hasSectionTemplates) {
			return { key: this.canEditTemplateList() ? 'NEW-DESCRIPTION.FROM-TEMPLATE.NO-RECOMMENDED-TEMPLATES.OWNER.TITLE' : 'NEW-DESCRIPTION.FROM-TEMPLATE.NO-RECOMMENDED-TEMPLATES.CONTRIBUTOR.TITLE' };
		}
		if (this.searchTerm) {
			return { key: 'NEW-DESCRIPTION.COPY.NO-DESCRIPTIONS-MATCH-SEARCH', params: { term: this.filterService.reverseLikeTransformation(this.searchTerm) } };
		}
		return { key: 'NEW-DESCRIPTION.COPY.NO-DESCRIPTIONS' };
	}

	protected isDisabled(description: Description): { disabled: boolean; reason: string } {
		const groupId = description.descriptionTemplate?.groupId;
		if (!this.sectionTemplateGroupIds()?.includes(groupId)) {
			return { disabled: true, reason: 'NEW-DESCRIPTION.COPY.WRONG-TEMPLATE' };
		}
		if (this.maxMultiplicityIds()?.includes(groupId)) {
			return { disabled: true, reason: 'NEW-DESCRIPTION.COPY.MAX-MULTIPLICITY' };
		}
		return { disabled: false, reason: '' };
	}

	protected onSearch(term: string): void {
		this.searchTerm = term;
		this.reload(term);
	}

	protected loadMore(): void {
		const request = this.publicView()
			? this.fetchPublic(this.searchTerm, this.publicOffset + PAGE_SIZE)
			: this.fetchMine(this.searchTerm, this.offset + PAGE_SIZE);
		this.loading.set(true);
		request.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))).subscribe({
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
		});
	}

	private reload(term: string): void {
		if (!this.hasSectionTemplates) {
			this.descriptions.set([]);
			this.publicDescriptions.set([]);
			this.totalCount.set(0);
			this.publicTotalCount.set(0);
			this.initialized.set(true);
			return;
		}
		this.refresh$.next(term);
	}

	private buildLookup(like: string, offset: number) {
		const templateSubQuery = new DescriptionTemplateLookup();
		templateSubQuery.groupIds = this.sectionTemplateGroupIds();
		return this.descriptionService.buildLookup({
			like: like || null,
			isActive: [IsActive.Active],
			descriptionTemplateSubQuery: templateSubQuery,
			page: { offset, size: PAGE_SIZE },
			order: { items: [nameof<Description>(x => x.label)] },
			project: {
				fields: [
					nameof<Description>(x => x.id),
					nameof<Description>(x => x.label),
					nameof<Description>(x => x.description),
					nameof<Description>(x => x.createdAt),
					nameof<Description>(x => x.updatedAt),
					[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
					[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
					[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
					[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
					[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
					[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
					[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
				],
			},
		});
	}

	private fetchMine(like: string, offset = 0): Observable<QueryResult<Description>> {
		this.offset = offset;
		return this.descriptionService.query(this.buildLookup(like, offset)).pipe(
			tap(result => {
				this.descriptions.set(offset ? [...this.descriptions(), ...(result?.items ?? [])] : (result?.items ?? []));
				this.totalCount.set(result?.count ?? 0);
			}),
		);
	}

	private fetchPublic(like: string, offset = 0): Observable<QueryResult<Description>> {
		this.publicOffset = offset;
		return this.descriptionService.publicQuery(this.buildLookup(like, offset)).pipe(
			tap(result => {
				this.publicDescriptions.set(offset ? [...this.publicDescriptions(), ...(result?.items ?? [])] : (result?.items ?? []));
				this.publicTotalCount.set(result?.count ?? 0);
			}),
		);
	}
}
