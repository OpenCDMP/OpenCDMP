import { Injectable, Signal, signal } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { TranslateService } from '@ngx-translate/core';
import { CookieService } from 'ngx-cookie-service';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { LanguageHttpService } from './language.http.service';
import { ConfigurationService } from '../configuration/configuration.service';

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
		private languageHttpService: LanguageHttpService,
		private configurationService: ConfigurationService,
		private cookieService: CookieService
	) {
		super();
	}

	public changeLanguage(lang: string) {
		this.currentLanguage = lang;
		this.translate.use(lang);
		this.setLanguageCookie(lang);
	}

	public getCookieLanguage(): string | null {
		return this.cookieService.get(this.configurationService.languageCookieName) || null;
	}

	private setLanguageCookie(lang: string): void {
		this.cookieService.set(this.configurationService.languageCookieName, lang, 5000, null, this.configurationService.languageCookieDomain || null, false, 'Lax');
	}

	public getCurrentLanguage() {
		if (this.currentLanguage == null) throw new Error("languages not loaded");
		return this.currentLanguage;
	}

    public getCurrentLanguageSignal(): Signal<string> {
        return this._languageSignal;
    }

	public loadAvailableLanguages(): Observable<string[]> {
		return this.languageHttpService.queryAvailableCodes(this.languageHttpService.buildAutocompleteLookup())
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

	public getAvailableLanguagesCodes() {
		if (this.availableLanguageCodes == null) throw new Error("languages not loaded");
		return this.availableLanguageCodes;
	}
}
