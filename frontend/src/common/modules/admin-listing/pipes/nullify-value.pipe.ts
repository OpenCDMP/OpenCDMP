import { Pipe, PipeTransform } from '@angular/core';

const nullSet = new Set([null, undefined]);

/** Prints "-" for a missing value in a listing cell (pass `true` as the argument to print nothing instead). */
@Pipe({
	name: 'nullifyValue',
	standalone: true,
})
export class NullifyValuePipe implements PipeTransform {
	transform(value: any, returnSpace?: boolean): any {
		if (!nullSet.has(value) || returnSpace) {
			return value;
		}
		return '-';
	}
}
