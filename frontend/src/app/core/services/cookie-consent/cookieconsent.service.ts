import { Injectable } from '@angular/core';
import CookieConsent, {
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
        private installationConfiguration: ConfigurationService
    ) { }

    init(): void {
        if (this.isInitialized) {
            console.warn('CookieConsent already initialized');
            return;
        }

        try {
            this.languageService.getAvailableLanguagesCodes().forEach((lang) => this.config.language.translations[lang] = `assets/i18n/${lang}.json`)
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
        let consents = this.cookieService.get('cc_cookie');
        if(!consents){ return null; }
        try {
            let parsedConsents = JSON.parse(consents) as CurrentConsent;
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