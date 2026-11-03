import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { DashboardSlideKind } from '@app/core/common/enum/dashboard-slide-kind';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { DashboardSlide, DashboardSlidePersist, DashboardSlideTranslation, DashboardSlidesTenantConfiguration, DefaultUserLocaleTenantConfiguration, FeaturedEntities, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { A11yAnnouncerService } from '@app/core/services/accessibility/a11y-announcer.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import moment from 'moment';
import { Observable, filter, map, switchMap, tap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TcSlideDrawerComponent, TcSlideLanguage } from './tc-slide-drawer.component';

const SLIDES = [nameof<TenantConfiguration>(x => x.dashboardSlides), nameof<DashboardSlidesTenantConfiguration>(x => x.slides)].join('.');
const SLIDE_FIELDS: string[] = [
	nameof<TenantConfiguration>(x => x.id),
	nameof<TenantConfiguration>(x => x.type),
	nameof<TenantConfiguration>(x => x.hash),
	nameof<TenantConfiguration>(x => x.updatedAt),
	[SLIDES, nameof<DashboardSlide>(x => x.id)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.kind)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.enabled)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.showFrom)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.showUntil)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.storageFile), nameof<StorageFile>(x => x.fullName)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.imageFit)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.buttonType)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.buttonTarget)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.languageCode)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.title)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.text)].join('.'),
	[SLIDES, nameof<DashboardSlide>(x => x.translations), nameof<DashboardSlideTranslation>(x => x.buttonText)].join('.'),
];

const SLIDES_PER_PAGE = 3;

interface SlideWhen {
	label: string;
	ended: boolean;
}

/** Users & onboarding: the slides every member of the organisation sees at the top of the Dashboard. Every change is saved at once. */
@Component({
	selector: 'app-tc-dashboard-slides',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, DragDropModule, RouterLink, TcSlideDrawerComponent],
	templateUrl: './tc-dashboard-slides.component.html',
	styleUrl: './tc-dashboard-slides.component.scss',
})
export class TcDashboardSlidesComponent {
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly languageHttpService = inject(LanguageHttpService);
	private readonly languageService = inject(LanguageService);
	private readonly authService = inject(AuthService);
	private readonly routerUtils = inject(RouterUtilsService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly announcer = inject(A11yAnnouncerService);
	private readonly dialog = inject(MatDialog);
	private readonly language = inject(TranslateService);
	private readonly dateTimeFormatter = inject(DateTimeFormatPipe);
	private readonly destroyRef = inject(DestroyRef);

	readonly canEdit = input(false);

	protected readonly kinds = DashboardSlideKind;
	protected readonly contentDefaultsUrl = this.routerUtils.generateUrl('/tenant-configuration/content-defaults');
	protected readonly isDefaultTenant = (this.authService.selectedTenant() ?? 'default') === 'default';

	protected readonly loading = signal(true);
	protected readonly busy = signal(false);
	private readonly record = signal<TenantConfiguration | null>(null);
	protected readonly slides = signal<DashboardSlide[]>([]);
	protected readonly featuredCount = signal(0);
	protected readonly languages = signal<TcSlideLanguage[]>([]);
	protected readonly defaultLanguage = signal('en');
	/** The slide open in the editor; null slide for a new one. */
	protected readonly editing = signal<{ slide: DashboardSlide | null } | null>(null);

	protected readonly hasRecommended = computed(() => this.slides().some(x => x.kind === DashboardSlideKind.Recommended));
	protected readonly recommendedPages = computed(() => Math.ceil(this.featuredCount() / SLIDES_PER_PAGE));

	constructor() {
		this.load().subscribe();
		// the backend sorts featured items by ordinal, so it has to be requested
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.FeaturedEntities, [
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.ordinal)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.descriptionTemplates), nameof<DescriptionTemplate>(x => x.ordinal)].join('.'),
		]).pipe(takeUntilDestroyed()).subscribe(data => this.featuredCount.set((data?.featuredEntities?.planBlueprints?.length ?? 0) + (data?.featuredEntities?.descriptionTemplates?.length ?? 0)));
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.DefaultUserLocale, [
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.defaultUserLocale), nameof<DefaultUserLocaleTenantConfiguration>(x => x.language)].join('.'),
		]).pipe(takeUntilDestroyed()).subscribe(data => this.defaultLanguage.set(data?.defaultUserLocale?.language || 'en'));
		this.languageHttpService.query(this.languageHttpService.buildAutocompleteLookup())
			.pipe(takeUntilDestroyed())
			.subscribe(data => this.languages.set((data.items ?? []).map(x => ({ code: x.code, name: this.languageService.getLanguageName(x.code) }))));
	}

	protected titleOf(slide: DashboardSlide): string {
		const translations = slide.translations ?? [];
		return (translations.find(x => x.languageCode === this.defaultLanguage()) ?? translations[0])?.title ?? '';
	}

	protected languagesOf(slide: DashboardSlide): string {
		return (slide.translations ?? []).map(x => x.languageCode.toUpperCase()).join(', ');
	}

	protected whenOf(slide: DashboardSlide): SlideWhen {
		const today = moment().format('YYYY-MM-DD');
		if (slide.showUntil && slide.showUntil < today) return { label: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.ENDED', { date: this.formatDate(slide.showUntil) }), ended: true };
		if (slide.showFrom && slide.showFrom > today) {
			return {
				label: slide.showUntil
					? this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.FROM-UNTIL', { from: this.formatDate(slide.showFrom), until: this.formatDate(slide.showUntil) })
					: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.FROM', { date: this.formatDate(slide.showFrom) }),
				ended: false,
			};
		}
		if (slide.showUntil) return { label: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.UNTIL', { date: this.formatDate(slide.showUntil) }), ended: false };
		return { label: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.ALWAYS'), ended: false };
	}

	protected labelOf(slide: DashboardSlide): string {
		return slide.kind === DashboardSlideKind.Recommended ? this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.RECOMMENDED.TITLE') : this.titleOf(slide);
	}

	protected addContent(): void {
		this.editing.set({ slide: null });
	}

	protected addRecommended(): void {
		if (this.hasRecommended()) return;
		this.save([...this.persistList(), { id: null, kind: DashboardSlideKind.Recommended, enabled: true, showFrom: null, showUntil: null, storageFileId: null, imageFit: null, buttonType: null, buttonTarget: null, translations: [] }]);
	}

	protected edit(slide: DashboardSlide): void {
		this.editing.set({ slide });
	}

	protected onSlideSaved(persist: DashboardSlidePersist): void {
		const list = this.persistList();
		const index = persist.id ? list.findIndex(x => x.id === persist.id) : -1;
		if (index >= 0) list[index] = persist;
		else list.push(persist);
		this.save(list, () => this.editing.set(null));
	}

	protected toggle(slide: DashboardSlide, enabled: boolean): void {
		this.save(this.persistList().map(x => x.id === slide.id ? { ...x, enabled } : x));
	}

	protected duplicate(slide: DashboardSlide): void {
		const list = this.persistList();
		const index = list.findIndex(x => x.id === slide.id);
		list.splice(index + 1, 0, { ...list[index], id: null });
		this.save(list);
	}

	protected drop(event: CdkDragDrop<unknown>): void {
		if (event.previousIndex === event.currentIndex) return;
		this.move(event.previousIndex, event.currentIndex);
	}

	protected moveBy(index: number, step: -1 | 1): void {
		const target = index + step;
		if (target < 0 || target >= this.slides().length) return;
		const label = this.labelOf(this.slides()[index]);
		this.move(index, target);
		this.announcer.announce(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-MOVED', { item: label, index: target + 1, length: this.slides().length }), 'polite');
	}

	protected delete(slide: DashboardSlide): void {
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.DELETE-CONFIRM', { name: this.labelOf(slide) }),
				confirmButton: this.language.instant('TENANT-CONFIGURATION.ONBOARDING.SLIDES.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(
			filter(Boolean),
			takeUntilDestroyed(this.destroyRef),
		).subscribe(() => this.save(this.persistList().filter(x => x.id !== slide.id)));
	}

	private move(from: number, to: number): void {
		// reorder on screen right away, so the dragged row does not jump back while saving
		const slides = [...this.slides()];
		moveItemInArray(slides, from, to);
		this.slides.set(slides);
		this.save(this.persistList());
	}

	private load(): Observable<TenantConfiguration | null> {
		return this.tenantConfigurationService.getType(TenantConfigurationType.DashboardSlides, SLIDE_FIELDS).pipe(
			map(data => data?.id ? data : null),
			tap({
				next: data => {
					this.record.set(data);
					this.slides.set(data?.dashboardSlides?.slides ?? []);
					this.loading.set(false);
				},
				error: error => {
					this.loading.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			}),
			takeUntilDestroyed(this.destroyRef),
		);
	}

	/** Saves the whole list. Without slides the record is removed, so members get the default organisation's slides again. */
	private save(slides: DashboardSlidePersist[], done?: () => void): void {
		if (this.busy()) return;
		const record = this.record();
		const request: Observable<unknown> = slides.length === 0 && record?.id
			? this.tenantConfigurationService.delete(record.id)
			: this.tenantConfigurationService.persist({ id: record?.id, hash: record?.hash, type: TenantConfigurationType.DashboardSlides, dashboardSlides: { slides } });
		this.busy.set(true);
		request.pipe(switchMap(() => this.load()), takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.busy.set(false);
				done?.();
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.busy.set(false);
				this.slides.set(this.record()?.dashboardSlides?.slides ?? []);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private persistList(): DashboardSlidePersist[] {
		return this.slides().map(x => ({
			id: x.id,
			kind: x.kind,
			enabled: x.enabled,
			showFrom: x.showFrom,
			showUntil: x.showUntil,
			storageFileId: x.storageFile?.id ?? null,
			imageFit: x.imageFit,
			buttonType: x.buttonType,
			buttonTarget: x.buttonTarget,
			translations: x.translations ?? [],
		}));
	}

	// a date without a time: UTC keeps it on the same day in every timezone
	private formatDate(value: string): string {
		return this.dateTimeFormatter.transform(value, 'mediumDate', 'UTC');
	}
}
