import { Pipe, PipeTransform } from '@angular/core';
import { CultureInfo } from '../services/culture/culture-service';

@Pipe({ name: 'cultureInfoDisplay' })
export class CultureInfoDisplayPipe implements PipeTransform {
	constructor() { }

	public transform(value: CultureInfo): any {
		return value.displayName + ' [' + value.name + ']';
	}
}