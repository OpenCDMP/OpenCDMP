import { Language } from '@app/core/model/language/language';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { LanguageHttpService } from './language.http.service';
import { Injector } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export class TranslateServerLoader implements TranslateLoader {

	constructor(
		private languageHttpService: LanguageHttpService,
		private authService: AuthService,
	) {	}


	getTranslation(lang: string): Observable<any> {
		const tenantCode = this.authService.selectedTenant();
		return this.languageHttpService.getSingleWithCode(lang, tenantCode == 'default' ? null : tenantCode, [
			nameof<Language>(x => x.id),
			nameof<Language>(x => x.code),
			nameof<Language>(x => x.payload),
		]).pipe(map(x => JSON.parse(x.payload)));
	}
}
