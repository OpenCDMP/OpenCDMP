import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'limitTo',
    standalone: false
})
export class NgForLimitPipe implements PipeTransform {
	transform(items: any[], limit: number) {
		if (items.length > limit) {
			return items.slice(0, limit);
		} else { return items; }
	}
}
