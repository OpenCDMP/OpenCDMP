import { Injectable, Signal, signal } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { LanguageHttpService } from './language.http.service';

@Injectable()
export class LanguageService extends BaseService {
    private _languageSignal = signal<string>(null);
	private get currentLanguage(): string{
        return this._languageSignal();
    }
    private set currentLanguage(language: string) {
        this._languageSignal.set(language);
    }
	private availableLanguageCodes: string[];

	constructor(
		private translate: TranslateService,
		private languageHttpService: LanguageHttpService
	) {
		super();
	}

	public changeLanguage(lang: string) {
		this.currentLanguage = lang;
		this.translate.use(lang);
	}

	public getCurrentLanguage() {
		if (this.currentLanguage == null) throw new Error("languages not loaded");
		return this.currentLanguage;
	}

    public getCurrentLanguageSignal(): Signal<string> {
        return this._languageSignal;
    }

	public loadAvailableLanguages(tenantCode: string | null = null): Observable<string[]> {
		return this.languageHttpService.queryAvailableCodes(this.languageHttpService.buildAutocompleteLookup(), tenantCode)
			.pipe(takeUntil(this._destroyed)).pipe(
				map((data) => {
					this.availableLanguageCodes = data.items;
					if (this.availableLanguageCodes.length > 0) this.currentLanguage = this.availableLanguageCodes[0];
					return this.availableLanguageCodes;
				})
			);
	}

	public getDefaultLanguagesCode() {
		if (this.availableLanguageCodes == null || this.availableLanguageCodes.length == 0) throw new Error("languages not loaded");
		return this.availableLanguageCodes[0];
	}

	private sortedLanguageCodes: { signature: string; codes: string[] } = null;

	// The default language first, then the rest by name in the current interface language.
	// The language menu reads this on every change detection, so only re-sort when the codes or their names change.
	public getAvailableLanguagesCodes() {
		if (this.availableLanguageCodes == null) throw new Error("languages not loaded");
		if (this.availableLanguageCodes.length === 0) return [];
		const names = new Map(this.availableLanguageCodes.map(code => [code, this.getLanguageName(code)]));
		const signature = [...names].join('|');
		if (this.sortedLanguageCodes?.signature !== signature) {
			const [defaultCode, ...others] = this.availableLanguageCodes;
			this.sortedLanguageCodes = { signature, codes: [defaultCode, ...others.sort((a, b) => names.get(a).localeCompare(names.get(b)))] };
		}
		return [...this.sortedLanguageCodes.codes];
	}

	// Custom languages have no GENERAL.LANGUAGES key, so fall back to the browser's name for the code.
	public getLanguageName(code: string): string {
		const key = 'GENERAL.LANGUAGES.' + code;
		const name = this.translate.instant(key);
		if (name !== key) return name;
		try {
			return new Intl.DisplayNames([code], { type: 'language' }).of(code) ?? code;
		} catch {
			return code;
		}
	}
}
