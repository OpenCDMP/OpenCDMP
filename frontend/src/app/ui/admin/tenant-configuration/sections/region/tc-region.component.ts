import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DefaultUserLocaleTenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { DefaultUserLocaleService } from '@app/core/services/default-user-locale/default-user-locale.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { TenantConfigurationEditorModel } from '@app/ui/admin/tenant-configuration/models/default-user-locale/default-user-locale-editor.model';
import { DefaultUserLocaleEditorResolver } from '@app/ui/admin/tenant-configuration/models/default-user-locale/default-user-locale-editor.resolver';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { catchError, of, startWith, switchMap } from 'rxjs';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';

/** Language & region: the default language, time zone and formats of new users. */
@Component({
	selector: 'app-tc-region',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, AutoCompleteModule, TcSaveBarComponent, TcStateTagComponent],
	providers: [TcSectionService],
	templateUrl: './tc-region.component.html',
	styleUrl: './tc-region.component.scss',
})
export class TcRegionComponent {
	protected readonly section = inject(TcSectionService);
	private readonly defaultUserLocaleService = inject(DefaultUserLocaleService);

	protected readonly locale = this.section.part(TenantConfigurationType.DefaultUserLocale, DefaultUserLocaleEditorResolver.lookupFields(), () => new TenantConfigurationEditorModel());
	protected readonly languageService = inject(LanguageService);
	protected readonly languages = this.languageService.getAvailableLanguagesCodes().slice().sort((x, y) => x.localeCompare(y));
	protected readonly timezoneConfiguration = this.defaultUserLocaleService.singleTimezoneAutocompleteConfiguration;
	protected readonly cultureConfiguration = this.defaultUserLocaleService.singleCultureAutocompleteConfiguration;

	protected readonly timezone = computed(() => this.locale.form()?.get('defaultUserLocale')?.get('timezone') as UntypedFormControl);
	protected readonly culture = computed(() => this.locale.form()?.get('defaultUserLocale')?.get('culture') as UntypedFormControl);
	protected readonly language = computed(() => this.locale.form()?.get('defaultUserLocale')?.get('language') as UntypedFormControl);

	private readonly cultureValue = toSignal(
		toObservable(this.culture).pipe(switchMap(control => control ? control.valueChanges.pipe(startWith(control.value)) : of(null))),
		{ initialValue: null as string | null });

	/** What new users get when the organisation stores nothing: the installation's configured defaults. */
	private readonly installationDefaults = toSignal(
		inject(BaseHttpV2Service).get<DefaultUserLocaleTenantConfiguration>(`${inject(ConfigurationService).server}tenant-configuration-defaults/default-user-locale`)
			.pipe(catchError(() => of(null))));

	constructor() {
		// shown in the fields rather than left empty; saving is only offered once something is changed
		effect(() => {
			const form = this.locale.form();
			const defaults = this.installationDefaults();
			if (!form || !defaults || this.locale.record()?.id) return;
			form.get('defaultUserLocale')?.patchValue({ timezone: defaults.timezone, language: defaults.language, culture: defaults.culture });
		});
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	/** How a date and a number look in the chosen culture. */
	protected readonly example = computed(() => {
		const culture = this.cultureValue();
		if (!culture) return null;
		try {
			const now = new Date();
			return `${new Intl.DateTimeFormat(culture, { dateStyle: 'short', timeStyle: 'short' }).format(now)} · ${new Intl.NumberFormat(culture).format(1234.5)}`;
		} catch {
			return null;
		}
	});
}
