import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';
import 'moment-timezone';
import { CultureService } from '../services/culture/culture-service';
import { TimezoneService } from '../services/timezone/timezone-service';

@Pipe({
	name: 'dateTimeFormatter'
})
export class DateTimeFormatPipe implements PipeTransform {

	constructor(private datePipe: DatePipe, private timezoneService: TimezoneService, private cultureService: CultureService) {

	}

	transform(value: any, format?: string, timezone?: string, locale?: string): string | null {

		// using timezone set in timezoneService by default. can be overwritten with pipe arguments
		const timezoneToUse = timezone ? timezone : moment(value).tz(this.timezoneService.getCurrentTimezone()).format('Z');
		let localeToUse = locale ? locale : this.cultureService.getCurrentCulture().name;

		return this.datePipe.transform(value, format, timezoneToUse, localeToUse);
	}
}


@Pipe({
	name: 'dataTableDateTimeFormatter'
})
// This is only used for the DataTable Column definition.
// It's a hacky way to apply format to the pipe because it only supports passing a pipe instance and calls transform in it without params.
export class DataTableDateTimeFormatPipe extends DateTimeFormatPipe implements PipeTransform {

	format: string;

	constructor(private _datePipe: DatePipe, private _timezoneService: TimezoneService, private _cultureService: CultureService) {
		super(_datePipe, _timezoneService, _cultureService);
	}

	public withFormat(format: string): DataTableDateTimeFormatPipe {
		this.format = format;
		return this;
	}

	transform(value: any): string | null {
		return super.transform(value, this.format);
	}
}
