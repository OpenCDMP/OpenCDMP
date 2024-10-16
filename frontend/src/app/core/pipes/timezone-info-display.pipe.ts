import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';
import 'moment-timezone';

@Pipe({ name: 'timezoneInfoDisplay' })
export class TimezoneInfoDisplayPipe implements PipeTransform {
	constructor() { }

	public transform(value): any {
		return value + ' (GMT' + moment.tz(value).format('Z') + ')';
	}
}
