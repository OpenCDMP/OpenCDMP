import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { DashboardSlideKind } from '@app/core/common/enum/dashboard-slide-kind';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DashboardSlide, DashboardSlideTranslation, DashboardSlidesTenantConfiguration, DefaultUserLocaleTenantConfiguration, FeaturedEntities, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { UserSettingsInformation } from '@app/core/model/user-settings/user-settings.model';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { UserSetting, UserSettingsService, UserSettingsType } from '@app/core/services/user-settings/user-settings.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import moment from 'moment';
import { map, take } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { DashboardContentSlideComponent } from './dashboard-content-slide.component';
import { DashboardSlideDialogComponent, DashboardSlideDialogData } from './dashboard-slide-dialog.component';

interface Recommendation {
	isBlueprint: boolean;
	id: Guid;
	groupId: Guid;
	label: string;
	typeName: string;
	description: string;
	version: number | string;
	usageCount: number;
}

type SlidePage =
	| { kind: 'content'; key: string; title: string; text: string; buttonText: string; slide: DashboardSlide }
	| { kind: 'recommended'; key: string; items: Recommendation[] };

/** The record and version of the slides a member hid; new or edited slides change the version and show them again. */
export class DashboardSlidesSettings {
	hidden: string = null;
}

const DASHBOARD_SLIDES_SETTINGS: UserSettingsInformation<DashboardSlidesSettings> = { key: 'DashboardSlidesUserSettings', type: DashboardSlidesSettings };

const SLIDES = [nameof<TenantConfiguration>(x => x.dashboardSlides), nameof<DashboardSlidesTenantConfiguration>(x => x.slides)].join('.');
const FEATURED_BLUEPRINTS = [nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints)].join('.');
const FEATURED_TEMPLATES = [nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates)].join('.');

/**
 * The organisation's slides at the top of the Dashboard: its own, or else the default organisation's.
 * The recommended slide shows the featured blueprints and templates, 3 on each page.
 */
@Component({
	selector: 'app-dashboard-slides',
	templateUrl: './dashboard-slides.component.html',
	styleUrl: './dashboard-slides.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [MatButton, MatIconButton, MatIcon, RouterLink, TranslatePipe, DashboardContentSlideComponent]
})
export class DashboardSlidesComponent {
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly storageFileService = inject(StorageFileService);
	private readonly userSettingsService = inject(UserSettingsService);
	private readonly principalService = inject(PrincipalService);
	private readonly authService = inject(AuthService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly routerUtils = inject(RouterUtilsService);

	private readonly record = signal<TenantConfiguration | null>(null);
	private readonly items = signal<Recommendation[]>([]);
	private readonly defaultLanguage = signal<string | null>(null);
	private readonly currentLanguage = toSignal(this.language.onLangChange.pipe(map(x => x.lang)), { initialValue: this.language.currentLang });
	private readonly width = signal(0);
	/** Recommended items on one slide: 3 where they fit, fewer on narrower screens, so their text is never cut. */
	protected readonly perPage = computed(() => this.width() >= 880 ? 3 : this.width() >= 560 ? 2 : 1);
	protected readonly tenantName = signal<string>(null);
	protected readonly imageUrls = signal<Map<string, string>>(new Map());

	/** What members see now: slides that are on and within their dates, the recommended one split into pages. */
	protected readonly pages = computed<SlidePage[]>(() => {
		const today = moment().format('YYYY-MM-DD');
		const perPage = this.perPage();
		const pages: SlidePage[] = [];
		for (const slide of this.record()?.dashboardSlides?.slides ?? []) {
			if (!slide.enabled || (slide.showFrom && slide.showFrom > today) || (slide.showUntil && slide.showUntil < today)) continue;
			if (slide.kind === DashboardSlideKind.Recommended) {
				for (let i = 0; i < this.items().length; i += perPage) pages.push({ kind: 'recommended', key: `${slide.id}-${i}`, items: this.items().slice(i, i + perPage) });
			} else {
				pages.push({ kind: 'content', key: `${slide.id}`, slide, ...this.textOf(slide.translations ?? []) });
			}
		}
		return pages;
	});
	protected readonly index = signal(0);
	/** Slides from the default organisation, shown because the organisation has none of its own. */
	protected readonly fromDefault = computed(() => this.record()?.belongsToCurrentTenant === false || !this.tenantName());

	private readonly hiddenKey = signal<string | null>(null);
	private readonly recordKey = computed(() => `${this.record()?.id}:${this.record()?.dashboardSlides?.version ?? 0}`);
	protected readonly hidden = computed(() => this.hiddenKey() === this.recordKey());

	constructor() {
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.DashboardSlides, this.slideFields())
			.pipe(takeUntilDestroyed())
			.subscribe({
				next: data => {
					this.record.set(data?.id ? data : null);
					this.loadImages(data?.dashboardSlides?.slides ?? []);
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
			});
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.FeaturedEntities, this.featuredFields())
			.pipe(takeUntilDestroyed())
			.subscribe({
				next: data => this.items.set([
					...(data?.featuredEntities?.planBlueprints ?? []).map(x => this.toRecommendation(x, true)),
					...(data?.featuredEntities?.descriptionTemplates ?? []).map(x => this.toRecommendation(x, false))
				]),
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
			});
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.DefaultUserLocale, [
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.defaultUserLocale), nameof<DefaultUserLocaleTenantConfiguration>(x => x.language)].join('.'),
		]).pipe(takeUntilDestroyed()).subscribe(data => this.defaultLanguage.set(data?.defaultUserLocale?.language ?? null));
		this.userSettingsService.get(DASHBOARD_SLIDES_SETTINGS).pipe(take(1)).subscribe({
			next: settings => this.hiddenKey.set(settings?.defaultSetting?.value?.hidden ?? null),
			// no settings yet (404 for a first-time user): nothing hidden
			error: () => this.hiddenKey.set(null),
		});

		if ((this.authService.selectedTenant() ?? 'default') !== 'default') {
			this.principalService.myTenantsTranslated()
				.pipe(takeUntilDestroyed())
				.subscribe(tenants => this.tenantName.set(tenants.find(x => x.code === this.authService.selectedTenant())?.name ?? null));
		}

		// the slider's own width, which also changes when the side menu opens or closes
		const host = inject(ElementRef<HTMLElement>).nativeElement;
		const observer = new ResizeObserver(entries => this.width.set(entries[0].contentRect.width));
		observer.observe(host);
		this.destroyRef.onDestroy(() => observer.disconnect());

		// a narrower slider splits the recommended slide into more pages; keep the position in range
		effect(() => {
			const count = this.pages().length;
			if (this.index() >= count && count > 0) this.index.set(count - 1);
		});
		this.destroyRef.onDestroy(() => this.imageUrls().forEach(url => URL.revokeObjectURL(url)));
	}

	previous(): void {
		this.index.update(x => Math.max(x - 1, 0));
	}

	next(): void {
		this.index.update(x => Math.min(x + 1, this.pages().length - 1));
	}

	/** "Read more": the whole slide in a dialog. */
	openSlide(page: SlidePage, index: number): void {
		if (page.kind !== 'content') return;
		const data: DashboardSlideDialogData = {
			heading: this.fromDefault() ? this.language.instant('DASHBOARD.SLIDES.FROM-DEFAULT') : this.language.instant('DASHBOARD.SLIDES.FROM', { tenant: this.tenantName() }),
			position: this.language.instant('DASHBOARD.SLIDES.POSITION', { index: index + 1, count: this.pages().length }),
			title: page.title,
			text: page.text,
			buttonType: page.slide.buttonType,
			buttonTarget: page.slide.buttonTarget,
			buttonText: page.buttonText,
		};
		this.dialog.open(DashboardSlideDialogComponent, { width: '640px', maxWidth: '90vw', data });
	}

	goTo(index: number): void {
		this.index.set(index);
	}

	toggleHidden(): void {
		const hidden = this.hidden() ? null : this.recordKey();
		this.hiddenKey.set(hidden);
		this.userSettingsService.get(DASHBOARD_SLIDES_SETTINGS).pipe(take(1)).subscribe({
			next: settings => this.storeHidden(hidden, settings?.defaultSetting ?? null),
			error: () => this.storeHidden(hidden, null),
		});
	}

	private storeHidden(hidden: string | null, stored: UserSetting<DashboardSlidesSettings> | null): void {
		this.userSettingsService.set({
			id: stored?.id ?? null,
			name: stored?.name ?? 'Default',
			key: DASHBOARD_SLIDES_SETTINGS.key,
			type: UserSettingsType.Setting,
			value: { hidden },
			hash: stored?.hash ?? null,
			isDefault: true,
		}, true, DASHBOARD_SLIDES_SETTINGS);
	}

	/** The member's language, else the organisation's default language, else the first one; field by field. */
	private textOf(translations: DashboardSlideTranslation[]): { title: string; text: string; buttonText: string } {
		const fallback = translations.find(x => x.languageCode === this.defaultLanguage()) ?? translations[0];
		const own = translations.find(x => x.languageCode === this.currentLanguage()) ?? fallback;
		return {
			title: own?.title || fallback?.title || '',
			text: own?.text || fallback?.text || '',
			buttonText: own?.buttonText || fallback?.buttonText || '',
		};
	}

	private loadImages(slides: DashboardSlide[]): void {
		for (const slide of slides) {
			const id = slide.storageFile?.id;
			if (!id || slide.kind !== DashboardSlideKind.Content) continue;
			this.storageFileService.download(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: response => this.imageUrls.update(urls => new Map(urls).set(`${slide.id}`, URL.createObjectURL(response.body))),
				// the slide still shows, with its text across the whole width
				error: () => null,
			});
		}
	}

	private toRecommendation(item: PlanBlueprint | DescriptionTemplate, isBlueprint: boolean): Recommendation {
		return {
			isBlueprint,
			id: item.id,
			groupId: item.groupId,
			label: item.label,
			typeName: item.type?.name,
			description: item.description,
			version: item.version,
			usageCount: item.usageCount ?? 0
		};
	}

	private slideFields(): string[] {
		return [
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.belongsToCurrentTenant),
			[nameof<TenantConfiguration>(x => x.dashboardSlides), nameof<DashboardSlidesTenantConfiguration>(x => x.version)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.id)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.kind)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.enabled)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.showFrom)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.showUntil)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.imageFit)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.buttonType)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.buttonTarget)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.languageCode)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.title)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.text)].join('.'),
			[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.buttonText)].join('.'),
		];
	}

	private featuredFields(): string[] {
		return [
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.id)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.label)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.description)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.groupId)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.version)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.ordinal)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.usageCount)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.id)].join('.'),
			[FEATURED_BLUEPRINTS, nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.name)].join('.'),

			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.description)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.version)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.ordinal)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.usageCount)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
			[FEATURED_TEMPLATES, nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
		];
	}
}
