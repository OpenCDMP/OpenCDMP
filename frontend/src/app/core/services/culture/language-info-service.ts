import { registerLocaleData } from '@angular/common';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { LoggingService } from '../logging/logging-service';
import { TypeUtils } from '../utilities/type-utils.service';
import { LanguageInfo } from '@app/core/model/language-info';

const availableLanguageInfos: LanguageInfo[] = require('../../../../assets/localization/languages.json');

@Injectable()
export class LanguageInfoService {

	private languageInfoValues = new Map<string, LanguageInfo>(); // cultures by name
	private languageInfoeChangeSubject = new Subject<LanguageInfo>();
	private currentLanguageInfo: LanguageInfo;

	constructor(
		private typeUtils: TypeUtils,
		private logger: LoggingService
	) {
		if (availableLanguageInfos) {
			this.languageInfoValues = new Map<string, LanguageInfo>();
			availableLanguageInfos.forEach(languageInfo => {
				this.languageInfoValues.set(languageInfo.code, languageInfo);
			});
		}
	}

	getLanguageInfoValues(): LanguageInfo[] {
		const values: LanguageInfo[] = [];
		this.languageInfoValues.forEach((value) => values.push(value));
		values.sort((a,b)=> a.name.localeCompare(b.name));
		return values;
	}

	getLanguageInfoValue(languageInfo: string): LanguageInfo | undefined {
		return this.languageInfoValues.get(languageInfo);
	}

	languageInfoSelected(languageInfo: string | LanguageInfo) {
		let newLanguageInfoName: string;
		if (this.typeUtils.isString(languageInfo)) {
			if (this.currentLanguageInfo && this.currentLanguageInfo.code === languageInfo) { return; }
			newLanguageInfoName = languageInfo;
		} else {
			if (this.currentLanguageInfo && this.currentLanguageInfo.code === languageInfo.code) { return; }
			newLanguageInfoName = languageInfo.code;
		}

		const newLanguageInfo = this.languageInfoValues.get(newLanguageInfoName);
		if (!newLanguageInfo) {
			this.logger.error(`unsupported culture given: ${newLanguageInfo}`); //TODO: throw error?
			return;
		}
		this.currentLanguageInfo = newLanguageInfo;
		this.languageInfoeChangeSubject.next(newLanguageInfo);

		// Set angular locale based on user selection.
		// This is a very hacky way to map cultures with angular cultures, since there is no mapping. We first try to
		// use the culture with the specialization (ex en-US), and if not exists we import the base culture (first part).
		let locale = newLanguageInfo.code;
		import(`../../../../../node_modules/@angular/common/locales/${locale}.mjs`).catch(reason => {
			this.logger.error('Could not load locale: ' + locale);
		}).then(selectedLocale => {
			if (selectedLocale) {
				registerLocaleData(selectedLocale.default);
			} else {
				import(`../../../../../node_modules/@angular/common/locales/${locale}.mjs`).catch(reason => {
					this.logger.error('Could not load locale: ' + locale);
				}).then(selectedDefaultLocale => {
					if (selectedDefaultLocale !== undefined) {
						registerLocaleData(selectedDefaultLocale.default);
					}
				});
			}
		});
	}

	getLanguageInfoChangeObservable(): Observable<LanguageInfo> {
		return this.languageInfoeChangeSubject.asObservable();
	}

	getCurrentLanguageInfo(): LanguageInfo {
		return this.currentLanguageInfo;
	}
}
