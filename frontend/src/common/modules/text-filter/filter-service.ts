import { Injectable } from '@angular/core';

@Injectable()
export class FilterService {

	constructor() {

	}

	transformLike(value: string): string {
		if (!value) {
			return undefined;
		} else {
			// We replace * with %. We also escape the _ operator. If % or _ is found in the query, we suppose that the user entered it on purpose.
			// Otherwise we append the % operator at the end.
			let transformedValue = value.split('*').join('%');
			if (!transformedValue.includes('%')) { transformedValue = transformedValue + '%'; }
			transformedValue = transformedValue.split('_').join('\_');
			return transformedValue;
		}
	}

	reverseLikeTransformation(value: string): string {
		if (!value) {
			return undefined;
		} else {
			let transformedValue = this.replaceAll(value, '\\_', '_');
			if (transformedValue.endsWith('%')) { transformedValue = transformedValue.substring(0, transformedValue.length - 1); }
			return transformedValue;
		}
	}

	private replaceAll(str: string, find: string, replace: string) {
		return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
	}

	private escapeRegExp(str: string) {
		return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
	}
}
