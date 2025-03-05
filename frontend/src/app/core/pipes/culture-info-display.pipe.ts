import { Pipe, PipeTransform } from '@angular/core';
import { CultureInfo } from '../services/culture/culture-service';

@Pipe({
    name: 'cultureInfoDisplay',
    standalone: false
})
export class CultureInfoDisplayPipe implements PipeTransform {
	constructor() { }

	public transform(value: CultureInfo): any {
		return value.displayName + ' [' + value.name + ']';
	}
}