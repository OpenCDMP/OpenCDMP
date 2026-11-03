import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, OnInit, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { DashboardSlideButtonType } from '@app/core/common/enum/dashboard-slide-button-type';
import { DashboardSlideImageFit } from '@app/core/common/enum/dashboard-slide-image-fit';
import { DashboardSlideKind } from '@app/core/common/enum/dashboard-slide-kind';
import { DashboardSlide, DashboardSlidePersist, DashboardSlideTranslation } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { A11yAnnouncerService } from '@app/core/services/accessibility/a11y-announcer.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { DashboardContentSlideComponent } from '@app/ui/dashboard/dashboard-slides/dashboard-content-slide.component';
import { UrlValidator } from '@common/forms/validation/custom-validator';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import moment from 'moment';

export interface TcSlideLanguage {
	code: string;
	name: string;
}

interface SlideTextForm {
	title: FormControl<string>;
	text: FormControl<string>;
	buttonText: FormControl<string>;
}

// a language other than the default may be left empty, but text without a title is not shown anywhere
const titleWithText = (group: AbstractControl): ValidationErrors | null => {
	const value = group.value as DashboardSlideTranslation;
	return !value.title?.trim() && (value.text?.trim() || value.buttonText?.trim()) ? { titleRequired: true } : null;
};

/** Edits one content slide of the Dashboard: its text in each language, image, button and dates. */
@Component({
	selector: 'app-tc-slide-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, RichTextEditorModule, DashboardContentSlideComponent],
	templateUrl: './tc-slide-drawer.component.html',
	styleUrls: ['../workflows/tc-status-drawer.component.scss', './tc-slide-drawer.component.scss'],
	host: { '(document:keydown.escape)': 'close()' },
})
export class TcSlideDrawerComponent implements OnInit {
	private readonly storageFileService = inject(StorageFileService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly announcer = inject(A11yAnnouncerService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	/** Null for a new slide. */
	readonly slide = input<DashboardSlide | null>(null);
	readonly languages = input.required<TcSlideLanguage[]>();
	readonly defaultLanguage = input.required<string>();
	readonly busy = input(false);
	readonly saved = output<DashboardSlidePersist>();
	readonly closed = output<void>();

	private readonly titleField = viewChild<ElementRef<HTMLInputElement>>('titleField');

	protected readonly buttonTypes = DashboardSlideButtonType;
	protected readonly buttonOptions = [
		{ type: DashboardSlideButtonType.Url, label: 'TENANT-CONFIGURATION.ONBOARDING.SLIDES.EDITOR.URL' },
		{ type: DashboardSlideButtonType.Email, label: 'TENANT-CONFIGURATION.ONBOARDING.SLIDES.EDITOR.EMAIL' },
	];
	protected readonly imageFitOptions = [
		{ fit: DashboardSlideImageFit.Fill, label: 'TENANT-CONFIGURATION.ONBOARDING.SLIDES.EDITOR.IMAGE-FILL' },
		{ fit: DashboardSlideImageFit.Fit, label: 'TENANT-CONFIGURATION.ONBOARDING.SLIDES.EDITOR.IMAGE-FIT' },
	];
	protected readonly form = new FormGroup({
		storageFileId: new FormControl<Guid | null>(null),
		imageFit: new FormControl<DashboardSlideImageFit>(DashboardSlideImageFit.Fill),
		buttonType: new FormControl<DashboardSlideButtonType | null>(null),
		buttonTarget: new FormControl<string>(''),
		showFrom: new FormControl<moment.Moment | null>(null),
		showUntil: new FormControl<moment.Moment | null>(null),
	});
	protected readonly texts = new FormGroup<Record<string, FormGroup<SlideTextForm>>>({});

	protected readonly selectedCode = signal('');
	protected readonly selectedText = computed(() => {
		this.textsVersion();
		return this.texts.controls[this.selectedCode()] ?? null;
	});
	private readonly textsVersion = signal(0);
	private readonly values = signal(0);

	/** One tab per language the slide has text in, the default language first. */
	protected readonly tabs = computed(() => {
		this.textsVersion();
		const names = new Map(this.languages().map(x => [x.code, x.name]));
		const codes = Object.keys(this.texts.controls).filter(code => code !== this.defaultLanguage());
		return [this.defaultLanguage(), ...codes].map(code => ({ code, name: names.get(code) ?? code.toUpperCase() }));
	});
	protected readonly addableLanguages = computed(() => {
		this.textsVersion();
		return this.languages().filter(x => !this.texts.controls[x.code]);
	});
	protected readonly selectedName = computed(() => this.tabs().find(x => x.code === this.selectedCode())?.name ?? '');

	protected readonly uploading = signal(false);
	protected readonly imageUrl = signal<string | null>(null);
	protected readonly imageName = signal<string | null>(null);

	/** The preview shows the selected language, with the default language's text where it has none. */
	protected readonly preview = computed(() => {
		this.values();
		const own = this.texts.controls[this.selectedCode()]?.value;
		const fallback = this.texts.controls[this.defaultLanguage()]?.value;
		return {
			title: own?.title || fallback?.title || '',
			text: own?.text || fallback?.text || '',
			buttonText: own?.buttonText || fallback?.buttonText || '',
			buttonType: this.form.controls.buttonType.value,
			buttonTarget: this.form.controls.buttonTarget.value,
			imageFit: this.form.controls.imageFit.value,
		};
	});

	ngOnInit(): void {
		const slide = this.slide();
		for (const translation of slide?.translations ?? []) this.addText(translation.languageCode, translation);
		this.addText(this.defaultLanguage());
		this.selectedCode.set(this.defaultLanguage());

		this.form.reset({
			storageFileId: slide?.storageFile?.id ?? null,
			imageFit: slide?.imageFit ?? DashboardSlideImageFit.Fill,
			buttonType: slide?.buttonType ?? null,
			buttonTarget: slide?.buttonTarget ?? '',
			showFrom: slide?.showFrom ? moment.utc(slide.showFrom) : null,
			showUntil: slide?.showUntil ? moment.utc(slide.showUntil) : null,
		});
		this.setTargetValidators(this.form.controls.buttonType.value);
		this.form.controls.buttonType.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(type => this.setTargetValidators(type));
		this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.values.update(x => x + 1));
		this.texts.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.values.update(x => x + 1));

		const file = slide?.storageFile;
		if (file?.id) {
			this.imageName.set(file.fullName ?? file.name ?? null);
			this.storageFileService.download(file.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: response => this.replaceImageUrl(URL.createObjectURL(response.body)),
				error: () => this.replaceImageUrl(null),
			});
		}
		this.destroyRef.onDestroy(() => this.replaceImageUrl(null));
		setTimeout(() => this.titleField()?.nativeElement.focus());
	}

	protected close(): void {
		this.closed.emit();
	}

	protected selectLanguage(code: string): void {
		this.selectedCode.set(code);
	}

	protected addLanguage(code: string): void {
		this.addText(code);
		this.selectedCode.set(code);
		setTimeout(() => this.titleField()?.nativeElement.focus());
	}

	protected removeLanguage(code: string): void {
		if (code === this.defaultLanguage()) return;
		this.texts.removeControl(code);
		this.textsVersion.update(x => x + 1);
		this.selectedCode.set(this.defaultLanguage());
	}

	/** Selecting the chosen kind again removes the button. */
	protected toggleButtonType(type: DashboardSlideButtonType): void {
		const control = this.form.controls.buttonType;
		control.setValue(control.value === type ? null : type);
		control.markAsDirty();
	}

	protected selectImageFit(fit: DashboardSlideImageFit): void {
		const control = this.form.controls.imageFit;
		if (control.value === fit) return;
		control.setValue(fit);
		control.markAsDirty();
	}

	protected onImageSelected(event: Event): void {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		input.value = '';
		if (!file) return;
		if (!file.type.startsWith('image/')) {
			this.announcer.announce(this.language.instant('TENANT-CONFIGURATION.BRANDING.LOGO-INVALID-FILE'), 'assertive');
			return;
		}
		this.uploading.set(true);
		this.storageFileService.uploadTempFiles(file).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: response => {
				this.uploading.set(false);
				this.form.controls.storageFileId.setValue(response[0].id);
				this.form.markAsDirty();
				this.replaceImageUrl(URL.createObjectURL(file));
				this.imageName.set(file.name);
				this.announcer.announce(this.language.instant('FILE-UPLOADER.ANNOUNCE.ADDED'), 'polite');
			},
			error: error => {
				this.uploading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected removeImage(): void {
		this.form.controls.storageFileId.setValue(null);
		this.replaceImageUrl(null);
		this.imageName.set(null);
		this.announcer.announce(this.language.instant('FILE-UPLOADER.ANNOUNCE.REMOVED'), 'polite');
	}

	protected save(): void {
		const defaultText = this.texts.controls[this.defaultLanguage()];
		if (this.form.controls.buttonType.value != null && !defaultText.controls.buttonText.value?.trim()) {
			defaultText.controls.buttonText.setErrors({ required: true });
		}
		const from = this.form.controls.showFrom.value;
		const until = this.form.controls.showUntil.value;
		if (from && until && until.isBefore(from, 'day')) this.form.controls.showUntil.setErrors({ beforeFrom: true });

		this.form.markAllAsTouched();
		this.texts.markAllAsTouched();
		if (this.form.invalid) return;
		if (this.texts.invalid) {
			// show the first language that needs fixing
			const invalid = Object.keys(this.texts.controls).find(code => this.texts.controls[code].invalid);
			if (invalid) this.selectedCode.set(invalid);
			return;
		}

		const raw = this.form.getRawValue();
		const codes = [this.defaultLanguage(), ...Object.keys(this.texts.controls).filter(code => code !== this.defaultLanguage())];
		const slide = this.slide();
		this.saved.emit({
			id: slide?.id ?? null,
			kind: DashboardSlideKind.Content,
			enabled: slide?.enabled ?? true,
			showFrom: raw.showFrom ? raw.showFrom.format('YYYY-MM-DD') : null,
			showUntil: raw.showUntil ? raw.showUntil.format('YYYY-MM-DD') : null,
			storageFileId: raw.storageFileId,
			imageFit: raw.storageFileId ? raw.imageFit : null,
			buttonType: raw.buttonType,
			buttonTarget: raw.buttonType != null ? raw.buttonTarget?.trim() : null,
			translations: codes
				.map(code => ({ languageCode: code, ...this.texts.controls[code].getRawValue() }))
				.filter(x => x.title?.trim()),
		});
	}

	private addText(code: string, translation?: DashboardSlideTranslation): void {
		if (this.texts.controls[code]) return;
		const isDefault = code === this.defaultLanguage();
		this.texts.addControl(code, new FormGroup<SlideTextForm>({
			title: new FormControl(translation?.title ?? '', isDefault ? Validators.required : null),
			text: new FormControl(translation?.text ?? '', isDefault ? Validators.required : null),
			buttonText: new FormControl(translation?.buttonText ?? ''),
		}, isDefault ? null : titleWithText));
		this.textsVersion.update(x => x + 1);
	}

	private setTargetValidators(type: DashboardSlideButtonType | null): void {
		const target = this.form.controls.buttonTarget;
		if (type === DashboardSlideButtonType.Url) target.setValidators([Validators.required, UrlValidator()]);
		else if (type === DashboardSlideButtonType.Email) target.setValidators([Validators.required, Validators.email]);
		else target.clearValidators();
		target.updateValueAndValidity({ emitEvent: false });
	}

	private replaceImageUrl(url: string | null): void {
		const previous = this.imageUrl();
		if (previous) URL.revokeObjectURL(previous);
		this.imageUrl.set(url);
	}
}
