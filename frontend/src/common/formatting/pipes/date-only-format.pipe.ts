import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform, inject } from '@angular/core';
import { CultureService } from '@app/core/services/culture/culture-service';
import 'moment-timezone';

@Pipe({
	name: 'dateOnly'
})
export class DateOnlyPipe implements PipeTransform {



	private cultureService = inject(CultureService);

	constructor(private datePipe: DatePipe) {

	}

	transform(value: any, format?: string, locale?: string): string | null {
		// using timezone set in timezoneService by default. can be overwritten with pipe arguments
		return this.datePipe.transform(value, format, null, locale ?? this.cultureService.getCurrentCulture().name);
	}
}

@Pipe({
	name: 'dataTableDateOnlyFormatter'
})
// This is only used for the DataTable Column definition.
// It's a hacky way to apply format to the pipe because it only supports passing a pipe instance and calls transform in it without params.
export class DataTableDateOnlyFormatPipe extends DateOnlyPipe implements PipeTransform {

	format: string;

	constructor(_datePipe: DatePipe) {
		super(_datePipe);
	}

	public withFormat(format: string): DataTableDateOnlyFormatPipe {
		this.format = format;
		return this;
	}

	transform(value: any): string | null {
		return super.transform(value, this.format);
	}
}
