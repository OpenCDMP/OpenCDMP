import { TranslateCompiler } from '@ngx-translate/core';

export class OpenCDMPCustomTranslationCompiler implements TranslateCompiler {
	/*
		* Needed by ngx-translate
		*/
	public compile(value: string, lang: string): string {
		return value;
	}

	/*
	* Triggered once from TranslateCompiler
	* Initiates recurive this.parseReferencePointers()
	* Returns modified translations object for ngx-translate to process
	*/
	public compileTranslations(translations: any, lang: string) {
		this.parseReferencePointers(translations, translations);
		return translations;
	}

	/*
	 * Triggered once from this.compileTranslations()
	 * Recursively loops through an object,
	 * replacing any property value that has a string starting with "@APP_CORE." with the APP_CORE global string definition.
	 * i.e. @APP_CORE.LOCATION.OVERVIEW becomes Location Overview
	 */
	private parseReferencePointers(currentTranslations, masterLanguageFile) {
		Object.keys(currentTranslations).forEach((key) => {
			if (currentTranslations[key] !== null && typeof currentTranslations[key] === 'object') {
				this.parseReferencePointers(currentTranslations[key], masterLanguageFile);
				return;
			}
			if (typeof currentTranslations[key] === 'string') {
				const searchRegex = /{{\s([A-Z_:]*)\s?}}/g;
				const replaceRegex = /({{\s?[A-Z_:]*\s?}})/g;

				let matches;
				while ((matches = searchRegex.exec(currentTranslations[key])) !== null) {
					// This is necessary to avoid infinite loops with zero-width matches
					if (matches.index === searchRegex.lastIndex) {
						searchRegex.lastIndex++;
					}
					const searchKey = matches[1];
					if (masterLanguageFile.hasOwnProperty(searchKey)) {
						// Replace the full translate syntax with the translated value
						currentTranslations[key] = currentTranslations[key].replace(replaceRegex, masterLanguageFile[searchKey]);
					} else {
						// If we can't find the value, display only the missing key instead of the full translate syntax
						currentTranslations[key] = currentTranslations[key].replace(replaceRegex, searchKey);
						console.error(`Error: Unable to find translation '${searchKey}'!`)
					}
				}
			}
		});
	}
}