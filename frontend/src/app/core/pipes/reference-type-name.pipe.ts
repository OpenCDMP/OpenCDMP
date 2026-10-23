import { Pipe, PipeTransform } from '@angular/core';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { getValue, TranslateService, TranslateStore } from '@ngx-translate/core';

// Translations are keyed REFERENCE-TYPES.NAMES.<code>, matching the code in any case; without one the type's own name shows.
@Pipe({
	name: 'referenceTypeName', pure: false
})
export class ReferenceTypeNamePipe implements PipeTransform {
	constructor(private language: TranslateService, private store: TranslateStore) { }

	transform(type: ReferenceType): string {
		// Only the current language: instant() would fall back to the tenant's default language.
		const names = getValue(this.store.getTranslations(this.language.getCurrentLang()), 'REFERENCE-TYPES.NAMES');
		const code = type?.code?.toLowerCase();
		if (!code || typeof names !== 'object')return type?.name;
		const key = Object.keys(names).find(x => x.toLowerCase() === code);
		return key && names[key] ? names[key] : type.name;
	}
}
