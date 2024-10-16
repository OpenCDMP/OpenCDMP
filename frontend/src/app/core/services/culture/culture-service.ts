import { registerLocaleData } from '@angular/common';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { ConfigurationService } from '../configuration/configuration.service';
import { LoggingService } from '../logging/logging-service';
import { TypeUtils } from '../utilities/type-utils.service';

const availableCultures: CultureInfo[] = require('../../../../assets/localization/available-cultures.json');

export interface CultureInfo {
	name: string;
	displayName: string;
}

@Injectable()
export class CultureService {

	private cultureValues = new Map<string, CultureInfo>(); // cultures by name
	private cultureChangeSubject = new Subject<CultureInfo>();
	private currentCulture: CultureInfo;

	constructor(
		private typeUtils: TypeUtils,
		private logger: LoggingService
	) {
		if (availableCultures) {
			this.cultureValues = new Map<string, CultureInfo>();
			availableCultures.forEach(culture => {
				this.cultureValues.set(culture.name, culture);
			});
		}
	}

	getCultureValues(): CultureInfo[] {
		const values: CultureInfo[] = [];
		this.cultureValues.forEach((value) => values.push(value));
		return values;
	}

	getCultureValue(culture: string): CultureInfo | undefined {
		return this.cultureValues.get(culture);
	}

	cultureSelected(culture: string | CultureInfo) {
		let newCultureName: string;
		if (this.typeUtils.isString(culture)) {
			if (this.currentCulture && this.currentCulture.name === culture) { return; }
			newCultureName = culture;
		} else {
			if (this.currentCulture && this.currentCulture.name === culture.name) { return; }
			newCultureName = culture.name;
		}

		const newCulture = this.cultureValues.get(newCultureName);
		if (!newCulture) {
			this.logger.error(`unsupported culture given: ${newCultureName}`); //TODO: throw error?
			return;
		}
		this.currentCulture = newCulture;
		this.cultureChangeSubject.next(newCulture);

		// Set angular locale based on user selection.
		// This is a very hacky way to map cultures with angular cultures, since there is no mapping. We first try to
		// use the culture with the specialization (ex en-US), and if not exists we import the base culture (first part).
		let locale = newCulture.name;
		this.loadLocale(locale);
	}

	getCultureChangeObservable(): Observable<CultureInfo> {
		return this.cultureChangeSubject.asObservable();
	}

	getCurrentCulture(installationConfigurationService?: ConfigurationService): CultureInfo {
		if (this.currentCulture == null && installationConfigurationService != null) {
			this.cultureSelected(installationConfigurationService.defaultCulture);
		}
		return this.currentCulture;
	}

	private loadLocale(locale: string) {
		switch (locale) {

			case 'af':
				import('@angular/common/locales/af').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'am':
				import('@angular/common/locales/am').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ar-SA':
				import('@angular/common/locales/ar-SA').then(data => {
					registerLocaleData(data.default);
				});;
				break;
			case 'bg':
				import('@angular/common/locales/bg').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'cs':
				import('@angular/common/locales/cs').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'da':
				import('@angular/common/locales/da').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'de':
				import('@angular/common/locales/de').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'el':
				import('@angular/common/locales/el').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'en-GB':
				import('@angular/common/locales/en-GB').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'es-419':
				import('@angular/common/locales/es-419').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'es':
				import('@angular/common/locales/es').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'et':
				import('@angular/common/locales/et').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'fa':
				import('@angular/common/locales/fa').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'fi':
				import('@angular/common/locales/fi').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'fil':
				import('@angular/common/locales/fil').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'fr-CA':
				import('@angular/common/locales/fr-CA').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'fr':
				import('@angular/common/locales/fr').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'he':
				import('@angular/common/locales/he').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'hi':
				import('@angular/common/locales/hi').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'hr':
				import('@angular/common/locales/hr').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'hu':
				import('@angular/common/locales/hu').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'id':
				import('@angular/common/locales/id').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'is':
				import('@angular/common/locales/is').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'it':
				import('@angular/common/locales/it').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ja':
				import('@angular/common/locales/ja').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ko':
				import('@angular/common/locales/ko').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'lt':
				import('@angular/common/locales/lt').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'lv':
				import('@angular/common/locales/lv').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ms':
				import('@angular/common/locales/ms').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ne':
				import('@angular/common/locales/ne').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'nl':
				import('@angular/common/locales/nl').then(data => {
					registerLocaleData(data.default);
				});
			case 'no':
				import('@angular/common/locales/no').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'pl':
				import('@angular/common/locales/pl').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'pt':
				import('@angular/common/locales/pt').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'pt-PT':
				import('@angular/common/locales/pt-PT').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ro':
				import('@angular/common/locales/ro').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'ru':
				import('@angular/common/locales/ru').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'sk':
				import('@angular/common/locales/sk').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'sl':
				import('@angular/common/locales/sl').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'sv':
				import('@angular/common/locales/sv').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'sw-KE':
				import('@angular/common/locales/sw-KE').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'sw':
				import('@angular/common/locales/sw').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'th':
				import('@angular/common/locales/th').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'tr':
				import('@angular/common/locales/tr').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'uk':
				import('@angular/common/locales/uk').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'vi':
				import('@angular/common/locales/vi').then(data => {
					registerLocaleData(data.default);
				});
				break;
			case 'zh':
				import('@angular/common/locales/zh').then(data => {
					registerLocaleData(data.default);
				});
				break;
			default:
				import('@angular/common/locales/en').then(data => {
					registerLocaleData(data.default);
				});
				break;
		}
	}
}
