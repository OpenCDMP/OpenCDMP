import { Language } from '@app/core/model/language/language';
import { mergeDeep, TranslateLoader } from '@ngx-translate/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { LanguageHttpService } from './language.http.service';
import { AuthService } from '../auth/auth.service';

export class TranslateServerLoader implements TranslateLoader {

	constructor(
		private languageHttpService: LanguageHttpService,
		private authService: AuthService,
	) {	}


	getTranslation(lang: string): Observable<any> {
		const tenantCode = this.authService.selectedTenant();
		const overrides = this.languageHttpService.getSingleWithCode(lang, tenantCode == 'default' ? null : tenantCode, [
			nameof<Language>(x => x.id),
			nameof<Language>(x => x.code),
			nameof<Language>(x => x.payload),
		]).pipe(map(x => JSON.parse(x.payload)), catchError(() => of({})));

		return forkJoin([this.languageHttpService.getBundledTranslations(lang), overrides])
			.pipe(map(([bundled, tenantOverrides]) => mergeDeep(bundled, tenantOverrides)));
	}
}
