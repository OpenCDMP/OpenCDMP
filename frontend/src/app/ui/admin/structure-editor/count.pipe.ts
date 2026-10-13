import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

/**
 * Translates a count-bearing key, using its `-ONE` sibling when the count is exactly 1 and that sibling exists,
 * so the editor never shows "1 questions". Usage: `'KEY' | bp2Count: n` or `'KEY' | bp2Count: n : { name: x }`.
 */
@Pipe({
    name: 'bp2Count', pure: false
})
export class Bp2CountPipe implements PipeTransform {
	constructor(private language: TranslateService) { }

	transform(key: string, count: number, params?: Record<string, unknown>): string {
		const one = `${key}-ONE`;
		const useOne = count === 1 && this.language.instant(one) !== one;
		return this.language.instant(useOne ? one : key, { count, ...(params ?? {}) });
	}
}
