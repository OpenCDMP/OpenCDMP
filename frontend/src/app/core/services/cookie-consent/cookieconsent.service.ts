import { Injectable } from '@angular/core';
import {
    run,
    setLanguage,
    show,
    showPreferences,
    hide,
} from 'vanilla-cookieconsent';
import { LanguageService } from '../language/language.service';
import { Guid } from '@common/types/guid';
import { CookieService } from 'ngx-cookie-service';
import { ConfigurationService } from '../configuration/configuration.service';
import { OpenCDMPCustomTranslationCompiler } from '@app/utilities/translate/opencdmp-custom-translation-compiler';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { LanguageHttpService } from '../language/language.http.service';
import { TranslateServerLoader } from '../language/server.loader';

@Injectable({
    providedIn: 'root',
})
export class CookieConsentService {
    private isInitialized = false;
    private config = {
        categories: {
            necessary: {
                enabled: true,
                readOnly: true,
            },
            analytics: {},
            functionality: {}
        },
        language: {
            default: 'en',
            translations: {
            },
        },
        revision: this.installationConfiguration.cookieConsentRevision ?? 0
    };

    constructor(
        private languageService: LanguageService,
        private cookieService: CookieService,
        private installationConfiguration: ConfigurationService,
        private languageHttpService: LanguageHttpService,
        private authService: AuthService
    ) { }

    init(): void {
        if (this.isInitialized) {
            console.warn('CookieConsent already initialized');
            return;
        }

        try {
            // Load through the app's loader (bundled file plus overrides) and the custom compiler, as ngx-translate does
            const loader = new TranslateServerLoader(this.languageHttpService, this.authService);
            this.languageService.getAvailableLanguagesCodes().forEach((lang) => this.config.language.translations[lang] = () =>
                firstValueFrom(loader.getTranslation(lang))
                    .then((translations) => new OpenCDMPCustomTranslationCompiler().compileTranslations(translations, lang))
            );
            run(this.config);
            this.isInitialized = true;
            console.log('CookieConsent initialized successfully');
        } catch (error) {
            console.error('Failed to initialize CookieConsent:', error);
        }
    }

    showConsentModal(): void {
        if (!this.isInitialized) return;
        show();
    }

    showPreferencesModal(): void {
        if (!this.isInitialized) return;
        showPreferences();
    }

    hideModal(): void {
        if (!this.isInitialized) return;
        hide();
    }

    setLanguage(lang: string): void {
        if (!this.isInitialized) return;
        setLanguage(lang);
    }

    public currentConsents(): CurrentConsent{
        const consents = this.cookieService.get('cc_cookie');
        if(!consents){ return null; }
        try {
            const parsedConsents = JSON.parse(consents) as CurrentConsent;
            return parsedConsents
        } catch {
            return null;
        }
    }
}

export interface CurrentConsent {
    categories: ConsentType[];
    consentTimestamp: Date;
    lastConsentTimestamp: Date;
    consentId: Guid;
    expirationTime: number;
    languageCode: string;
}

export enum ConsentType {
    Necessary = 'necessary',
    Analytics = 'analytics',
    Functionality = 'functionality'
}