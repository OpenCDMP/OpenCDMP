import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnDestroy, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Plan } from '@app/core/model/plan/plan';
import { PluginConfigurationFieldPersist } from '@app/core/model/plugin-configuration/plugin-configuration';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import FileSaver from 'file-saver';
import { Subject, catchError, debounceTime, distinctUntilChanged, filter, map, of, switchMap, tap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';

type PreviewState = 'idle' | 'loading' | 'ready' | 'stale' | 'error';

/**
 * A PDF of one of the organisation's plans, rendered by the plugin with the settings as they are being edited.
 * Refreshes by itself a moment after the last change, and on demand.
 */
@Component({
	selector: 'app-tc-settings-preview',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	template: `
		<section class="tc-pv" aria-labelledby="tc-pv-h">
			<div class="tc-pv__head">
				<h2 id="tc-pv-h">{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW' | translate }}</h2>
				<span class="tc-pv__state" [attr.data-state]="state()" aria-live="polite">{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW-STATE.' + state().toUpperCase() | translate }}</span>
			</div>
			<mat-form-field class="tc-pv__plan" subscriptSizing="dynamic">
				<mat-label>{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW-WITH' | translate }}</mat-label>
				<mat-icon matPrefix aria-hidden="true">search</mat-icon>
				<input matInput type="text" [value]="query()" (input)="query.set($any($event.target).value)" (focus)="$any($event.target).select()"
					[matAutocomplete]="planPicker" [placeholder]="'TENANT-CONFIGURATION.EXPORT.SEARCH-PLANS' | translate">
				<mat-autocomplete #planPicker="matAutocomplete" (optionSelected)="choose($event.option.value)" [displayWith]="planLabel">
					@for (plan of plans(); track plan.id) {
						<mat-option [value]="plan">{{ plan.label }}@if (plan.version > 1) { <span class="tc-pv__version">v{{ plan.version }}</span> }</mat-option>
					} @empty {
						<mat-option disabled>{{ 'TENANT-CONFIGURATION.EXPORT.NO-PLANS-MATCH' | translate }}</mat-option>
					}
				</mat-autocomplete>
			</mat-form-field>
			<div class="tc-pv__actions">
				<button mat-stroked-button type="button" (click)="refresh()" [disabled]="!planId() || state() === 'loading'">
					<mat-icon aria-hidden="true">refresh</mat-icon>{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW-REFRESH' | translate }}
				</button>
				<button mat-button type="button" (click)="download()" [disabled]="!blob()">
					<mat-icon aria-hidden="true">download</mat-icon>{{ 'TENANT-CONFIGURATION.EXPORT.DOWNLOAD-PDF' | translate }}
				</button>
			</div>
			<div class="tc-pv__frame">
				@if (url(); as src) {
					<iframe [src]="src" [title]="'TENANT-CONFIGURATION.EXPORT.PREVIEW-FRAME' | translate" [class.tc-pv__dim]="state() === 'loading' || state() === 'stale'"></iframe>
				}
				@if (state() === 'loading') {
					<div class="tc-pv__overlay"><mat-spinner diameter="32" /></div>
				} @else if (state() === 'error') {
					<p class="tc-pv__message">{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW-FAILED' | translate }}@if (error()) { <br><span class="tc-pv__detail">{{ error() }}</span> }</p>
				} @else if (!url()) {
					<p class="tc-pv__message">{{ (plans().length === 0 ? 'TENANT-CONFIGURATION.EXPORT.PREVIEW-NO-PLANS' : 'TENANT-CONFIGURATION.EXPORT.PREVIEW-EMPTY') | translate }}</p>
				}
			</div>
			<p class="tc-pv__note">{{ 'TENANT-CONFIGURATION.EXPORT.PREVIEW-NOTE' | translate }}</p>
		</section>
	`,
	styles: [`
		:host { display: block; }
		.tc-pv { padding: 18px; border: 1px solid var(--md-sys-color-outline-variant, #e1e5ee); border-radius: 12px; background: var(--md-sys-color-surface, #fff); }
		.tc-pv__head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
		.tc-pv__head h2 { margin: 0; font-size: 16px; font-weight: 800; }
		.tc-pv__state { font-size: 12px; font-weight: 600; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-pv__state[data-state="stale"] { color: var(--md-sys-color-primary, #2457c5); }
		.tc-pv__state[data-state="error"] { color: var(--md-sys-color-error, #b3261e); }
		.tc-pv__plan { width: 100%; }
		.tc-pv__version { margin-left: 6px; font-size: 12px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-pv__actions { display: flex; flex-wrap: wrap; gap: 8px; margin: 10px 0; }
		.tc-pv__frame { position: relative; height: 640px; border-radius: 8px; overflow: hidden; background: var(--md-sys-color-surface-container, #eef1f6); }
		.tc-pv__frame iframe { width: 100%; height: 100%; border: 0; transition: opacity 150ms ease; }
		.tc-pv__dim { opacity: 0.45; }
		.tc-pv__overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
		.tc-pv__message { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 0; padding: 24px; text-align: center; font-size: 13px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-pv__detail { font-size: 12px; }
		.tc-pv__note { margin: 10px 0 0; font-size: 12px; color: var(--md-sys-color-on-surface-variant, #566072); }
	`],
})
export class TcSettingsPreviewComponent implements OnDestroy {
	readonly repositoryId = input.required<string>();
	/** The plugin's settings as edited. */
	readonly fields = input<PluginConfigurationFieldPersist[]>([]);

	private readonly http = inject(BaseHttpV2Service);
	private readonly configurationService = inject(ConfigurationService);
	private readonly sanitizer = inject(DomSanitizer);
	private readonly destroyRef = inject(DestroyRef);

	private readonly planService = inject(PlanService);
	private readonly filterService = inject(FilterService);
	/** What is typed in the plan field; the most recently updated plans while it is empty. */
	protected readonly query = signal('');
	protected readonly plans = toSignal(toObservable(this.query).pipe(
		debounceTime(250),
		map(x => x.trim()),
		distinctUntilChanged(),
		switchMap(term => this.planService.query(this.planLookup(term === this.selectedLabel ? '' : term)).pipe(map(x => x.items ?? []), catchError(() => of([] as Plan[]))))),
		{ initialValue: [] as Plan[] });
	protected readonly planId = signal<Guid | null>(null);
	private selectedLabel = '';
	protected readonly planLabel = (plan: Plan | string | null) => typeof plan === 'string' ? plan : plan?.label ?? '';
	protected readonly state = signal<PreviewState>('idle');
	protected readonly error = signal<string | null>(null);
	protected readonly blob = signal<Blob | null>(null);
	private objectUrl: string | null = null;
	protected readonly url = signal<SafeResourceUrl | null>(null);

	private readonly requests = new Subject<void>();
	private readonly fingerprint = computed(() => JSON.stringify(this.fields()));

	constructor() {
		// the most recently updated plan, once the list arrives
		effect(() => {
			const plans = this.plans();
			if (plans.length > 0 && !untracked(this.planId)) untracked(() => this.choose(plans[0]));
		});

		this.requests.pipe(
			filter(() => !!this.planId()),
			tap(() => { this.state.set('loading'); this.error.set(null); }),
			switchMap(() => this.http.post<Blob>(`${this.configurationService.server}file-transformer/preview-settings`,
				{ repositoryId: this.repositoryId(), planId: this.planId(), fields: this.fields() },
				{ responseType: 'blob' }).pipe(
					map(blob => ({ blob, error: null as string | null })),
					catchError((e: HttpErrorResponse) => of({ blob: null as Blob | null, error: typeof e.error === 'string' ? e.error : e.statusText || null })))),
			takeUntilDestroyed(),
		).subscribe(({ blob, error }) => {
			if (!blob) {
				this.state.set('error');
				this.error.set(error);
				return;
			}
			this.show(blob);
			this.state.set('ready');
		});

		// a change marks the preview out of date and renders again shortly after typing stops
		toObservable(this.fingerprint).pipe(
			tap(() => { if (this.blob()) this.state.set('stale'); }),
			debounceTime(1500),
			takeUntilDestroyed(),
		).subscribe(() => this.requests.next());
		toObservable(this.planId).pipe(filter(Boolean), takeUntilDestroyed()).subscribe(() => this.requests.next());
	}

	ngOnDestroy(): void {
		if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
	}

	protected choose(plan: Plan): void {
		this.selectedLabel = plan.label ?? '';
		this.query.set(this.selectedLabel);
		this.planId.set(plan.id);
	}

	protected refresh(): void {
		this.requests.next();
	}

	protected download(): void {
		const blob = this.blob();
		if (blob) FileSaver.saveAs(blob, 'export-preview.pdf');
	}

	private show(blob: Blob): void {
		if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
		const pdf = blob.type === 'application/pdf' ? blob : new Blob([blob], { type: 'application/pdf' });
		this.objectUrl = URL.createObjectURL(pdf);
		this.blob.set(pdf);
		// a blob URL this page just created, of a PDF the platform rendered
		this.url.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.objectUrl + '#view=FitH&toolbar=0'));
	}

	private planLookup(like: string): PlanLookup {
		const lookup = new PlanLookup();
		if (like) lookup.like = this.filterService.transformLike(like);
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 25 };
		lookup.order = { items: ['-' + nameof<Plan>(x => x.updatedAt)] };
		lookup.project = { fields: [nameof<Plan>(x => x.id), nameof<Plan>(x => x.label), nameof<Plan>(x => x.version), nameof<Plan>(x => x.updatedAt)] };
		return lookup;
	}
}
