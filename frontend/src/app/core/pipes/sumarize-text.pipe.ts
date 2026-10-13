import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'sumarizeText' })
export class SumarizeTextPipe implements PipeTransform {
	transform(items: any[], limit: number = 50) {
		if (items.length > limit) {
			return `${items.slice(0, limit-3)}...`;
		} else { return items; }
	}
}
