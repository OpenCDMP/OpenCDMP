import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { CssColorsTenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { A11yAnnouncerService } from '@app/core/services/accessibility/a11y-announcer.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { TenantConfigurationEditorModel as CssColorsEditorModel } from '@app/ui/admin/tenant-configuration/models/css-colors/css-colors-editor.model';
import { CssColorsEditorResolver } from '@app/ui/admin/tenant-configuration/models/css-colors/css-colors-editor.resolver';
import { TenantConfigurationEditorModel as LogoEditorModel } from '@app/ui/admin/tenant-configuration/models/logo/logo-editor.model';
import { LogoEditorResolver } from '@app/ui/admin/tenant-configuration/models/logo/logo-editor.resolver';
import { FormService } from '@common/forms/form-service';
import { CleanupJsonString } from '@common/forms/validation/custom-validator';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { NgxColorsModule } from 'ngx-colors';
import { Observable, map, of, startWith, switchMap, tap } from 'rxjs';
import { TcPart } from '../../shared/tc-part';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSaveable, TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';

interface TcColorPreset { name: string; color: string; }

/** A logo uploaded on this page but not saved yet, tied to the form it was uploaded into. */
interface TcPendingLogo { form: UntypedFormGroup; url: string; name: string; }

/** Branding: the main colour, the logo and the custom style variables of the tenant. */
@Component({
	selector: 'app-tc-branding',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, NgxColorsModule, TcSaveBarComponent, TcStateTagComponent],
	providers: [TcSectionService],
	templateUrl: './tc-branding.component.html',
	styleUrl: './tc-branding.component.scss',
})
export class TcBrandingComponent {
	protected readonly section = inject(TcSectionService);
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly formService = inject(FormService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly tenantHandlingService = inject(TenantHandlingService);
	private readonly storageFileService = inject(StorageFileService);
	private readonly announcer = inject(A11yAnnouncerService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	protected readonly presets: TcColorPreset[] = [
		{ name: 'BLUE', color: '#1F57C3' },
		{ name: 'TEAL', color: '#0F766E' },
		{ name: 'INDIGO', color: '#4338CA' },
		{ name: 'RED', color: '#B42336' },
		{ name: 'SLATE', color: '#334155' },
	];
	protected readonly overrideExample = '{ "--link-color": "#1F57C3" }';
	protected readonly themingUrl = 'https://opencdmp.github.io/administering/tenant-management/branding/';

	/**
	 * Built here rather than with `section.part()`, so that the section saves it through {@link colorsSaveable},
	 * which re-applies the theme after a successful save.
	 */
	protected readonly colors = new TcPart(
		{ tenantConfigurationService: this.tenantConfigurationService, formService: this.formService, httpErrorHandlingService: this.httpErrorHandlingService, destroyRef: this.destroyRef },
		TenantConfigurationType.CssColors, [...CssColorsEditorResolver.lookupFields(), 'belongsToCurrentTenant'], () => new CssColorsEditorModel(), () => !this.section.canEdit);
	private readonly colorsSaveable: TcSaveable = {
		dirty: this.colors.dirty,
		save: () => this.saveColors(),
		discard: () => this.colors.discard(),
	};

	protected readonly logo = this.section.part(TenantConfigurationType.Logo, LogoEditorResolver.lookupFields(), () => new LogoEditorModel());

	protected readonly loading = computed(() => this.colors.loading() || this.logo.loading());

	protected readonly colorsForm = computed(() => this.colors.form()?.get('cssColors') as UntypedFormGroup | null);
	protected readonly primaryColor = computed(() => this.colorsForm()?.get('primaryColor') as UntypedFormControl);
	protected readonly primaryColorInput = computed(() => this.colorsForm()?.get('primaryColorInput') as UntypedFormControl);
	protected readonly cssOverride = computed(() => this.colorsForm()?.get('cssOverride') as UntypedFormControl);
	protected readonly storageFileId = computed(() => this.logo.form()?.get('logo')?.get('storageFileId') as UntypedFormControl);

	private readonly colorsValue = toSignal(
		toObservable(this.colorsForm).pipe(switchMap(form => form
			? form.valueChanges.pipe(startWith(null), map(() => ({ ...form.getRawValue(), primaryColorValid: !form.get('primaryColor')?.invalid })))
			: of(null))),
		{ initialValue: null as { primaryColor?: string; cssOverride?: string; primaryColorValid?: boolean } | null });

	/** The chosen main colour, or null while it is empty or not a valid colour. */
	protected readonly chosenColor = computed(() => {
		const value = this.colorsValue();
		return value?.primaryColor && value.primaryColorValid ? value.primaryColor : null;
	});

	protected readonly selectedPreset = computed(() => this.chosenColor()?.toLowerCase() ?? null);

	/** CSS variables of the preview: the chosen colour plus the overrides it understands. */
	protected readonly previewStyle = computed(() => {
		const primary = this.chosenColor();
		const overrides = this.parseOverrides(this.colorsValue()?.cssOverride);
		const style: Record<string, string> = {};
		const previewPrimary = overrides['--md-sys-color-primary'] ?? primary;
		if (previewPrimary) {
			style['--tc-pv-primary'] = previewPrimary;
			style['--tc-pv-on-primary'] = overrides['--md-sys-color-on-primary'] ?? this.onColor(previewPrimary);
		}
		const link = overrides['--link-color'] ?? primary;
		if (link) style['--tc-pv-link'] = link;
		return style;
	});

	private readonly pendingLogo = signal<TcPendingLogo | null>(null);
	private readonly savedLogoUrl = signal<string | null>(null);
	protected readonly uploading = signal(false);

	protected readonly savedLogoName = computed(() => {
		const file = this.logo.record()?.logo?.storageFile;
		if (!file) return null;
		return file.fullName || (file.extension ? `${file.name}.${file.extension.replace(/^\./, '')}` : file.name);
	});
	protected readonly logoPending = computed(() => !!this.pendingLogo() && this.pendingLogo().form === this.logo.form());
	protected readonly logoUrl = computed(() => this.logoPending() ? this.pendingLogo().url : this.savedLogoUrl());
	protected readonly logoName = computed(() => this.logoPending() ? this.pendingLogo().name : this.savedLogoName());
	protected readonly logoEditable = computed(() => this.section.canEdit && !this.logo.inherited());
	protected readonly canRemoveLogo = computed(() => this.logoEditable() && (this.logoPending() || (this.logo.changed() && this.section.canReset)));

	constructor() {
		this.section.register(this.colorsSaveable);
		this.colors.load().subscribe();

		// The editor models do not pass `disabled` to their nested forms: lock them here.
		effect(() => this.lockIfReadOnly(this.colors.form(), this.colors.inherited()));
		effect(() => this.lockIfReadOnly(this.logo.form(), this.logo.inherited()));

		// Keeps the typed colour and the picker in sync.
		effect(onCleanup => {
			const input = this.primaryColorInput();
			const picker = this.primaryColor();
			if (!input || !picker) return;
			const fromInput = input.valueChanges.subscribe(color => picker.setValue(color, { emitEvent: false }));
			const fromPicker = picker.valueChanges.subscribe(color => input.setValue(color, { emitEvent: false }));
			onCleanup(() => { fromInput.unsubscribe(); fromPicker.unsubscribe(); });
		});

		// The saved logo, downloaded for the preview.
		toObservable(computed(() => this.logo.record()?.logo?.storageFile?.id ?? null)).pipe(
			switchMap(id => id ? this.storageFileService.download(Guid.parse(id.toString())).pipe(map(response => URL.createObjectURL(response.body))) : of(null)),
			takeUntilDestroyed(),
		).subscribe({
			next: url => this.replaceUrl(this.savedLogoUrl, url),
			error: () => this.replaceUrl(this.savedLogoUrl, null),
		});

		// A new form (after a save, a discard or a reset) drops the pending upload.
		effect(() => {
			const form = this.logo.form();
			const pending = this.pendingLogo();
			if (pending && pending.form !== form) {
				URL.revokeObjectURL(pending.url);
				this.pendingLogo.set(null);
			}
		});

		this.destroyRef.onDestroy(() => {
			if (this.savedLogoUrl()) URL.revokeObjectURL(this.savedLogoUrl());
			if (this.pendingLogo()) URL.revokeObjectURL(this.pendingLogo().url);
		});
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected selectPreset(color: string): void {
		const control = this.primaryColor();
		if (!control || control.disabled) return;
		control.markAsDirty();
		control.setValue(color);
	}

	protected resetColors(): void {
		this.section.confirmReset(() => this.colors.reset().pipe(tap(() => this.tenantHandlingService.loadAndApplyTenantCssColors())));
	}

	protected resetLogo(): void {
		this.section.confirmReset(() => this.logo.reset().pipe(tap(() => this.announcer.announce(this.language.instant('FILE-UPLOADER.ANNOUNCE.REMOVED'), 'polite'))));
	}

	protected onLogoSelected(event: Event): void {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		input.value = '';
		const control = this.storageFileId();
		const form = this.logo.form();
		if (!file || !control || !form) return;
		if (!file.type.startsWith('image/')) {
			this.announcer.announce(this.language.instant('TENANT-CONFIGURATION.BRANDING.LOGO-INVALID-FILE'), 'assertive');
			return;
		}
		this.uploading.set(true);
		this.storageFileService.uploadTempFiles(file).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: response => {
				this.uploading.set(false);
				if (this.logo.form() !== form) return;
				const previous = this.pendingLogo();
				if (previous) URL.revokeObjectURL(previous.url);
				this.pendingLogo.set({ form, url: URL.createObjectURL(file), name: file.name });
				control.markAsDirty();
				control.markAsTouched();
				control.setValue(response[0].id.toString());
				this.announcer.announce(this.language.instant('FILE-UPLOADER.ANNOUNCE.ADDED'), 'polite');
			},
			error: error => {
				this.uploading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	/** Drops an upload that is not saved yet, or else resets the tenant's logo to the installation default. */
	protected removeLogo(): void {
		if (this.logoPending()) {
			this.logo.discard();
			this.announcer.announce(this.language.instant('FILE-UPLOADER.ANNOUNCE.REMOVED'), 'polite');
		} else {
			this.resetLogo();
		}
	}

	/** Saves the colours and, on success, applies them to the app right away. */
	private saveColors(): Observable<boolean> {
		const override = this.cssOverride();
		const value = override?.value as string;
		// The override must be one JSON object.
		if (value && (!value.startsWith('{') || !value.endsWith('}'))) {
			override.setValue(`{${value.replace(/[{}]+/g, '')}}`);
		}
		const applied = this.colorsForm()?.getRawValue() as CssColorsTenantConfiguration;
		return this.colors.save().pipe(tap(saved => {
			if (saved && applied) this.tenantHandlingService.applyTenantCssColors(applied);
		}));
	}

	private lockIfReadOnly(form: UntypedFormGroup | null, inherited: boolean): void {
		if (form && (inherited || !this.section.canEdit) && form.enabled) form.disable({ emitEvent: false });
	}

	private replaceUrl(target: typeof this.savedLogoUrl, url: string | null): void {
		const previous = target();
		if (previous && previous !== url) URL.revokeObjectURL(previous);
		target.set(url);
	}

	private parseOverrides(input: string | null | undefined): Record<string, string> {
		if (!input) return {};
		try {
			const json = JSON.parse(CleanupJsonString(input));
			if (!json || typeof json !== 'object' || Array.isArray(json)) return {};
			return Object.fromEntries(Object.entries(json).filter(([, v]) => typeof v === 'string' || typeof v === 'number').map(([k, v]) => [k, String(v)]));
		} catch {
			return {};
		}
	}

	/** Black or white text, whichever reads better on the given colour (white when it cannot be parsed). */
	private onColor(color: string): string {
		const hex = /^#?([0-9a-f]{3}|[0-9a-f]{6})$/i.exec(color.trim())?.[1];
		if (!hex) return '#fff';
		const full = hex.length === 3 ? hex.split('').map(x => x + x).join('') : hex;
		const [r, g, b] = [0, 2, 4].map(i => parseInt(full.substring(i, i + 2), 16) / 255)
			.map(c => c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4));
		const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
		return luminance > 0.179 ? '#1c2230' : '#fff';
	}
}
