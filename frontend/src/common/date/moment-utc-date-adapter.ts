import { Inject, Injectable, Optional } from '@angular/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import moment, { Moment } from 'moment';

@Injectable()
export class MomentUtcDateAdapter extends MomentDateAdapter {

	constructor(@Optional() @Inject(MAT_DATE_LOCALE) dateLocale: string) {
		super(dateLocale);
	}

	// selected from datepicker
	createDate(year: number, month: number, date: number): Moment {
		// Moment.js will create an invalid date if any of the components are out of bounds, but we
		// explicitly check each case so we can throw more descriptive errors.
		if (month < 0 || month > 11) {
			throw Error(`Invalid month index "${month}". Month index has to be between 0 and 11.`);
		}

		if (date < 1) {
			throw Error(`Invalid date "${date}". Date has to be greater than 0.`);
		}

		const result = moment.utc({ year, month, date }).locale(this.locale);

		// If the result isn't valid, the date must have been out of bounds for this month.
		if (!result.isValid()) {
			throw Error(`Invalid date "${date}" for month with index "${month}".`);
		}

		return result;
	}

	// manually writing on the textbox
	parse(value: any, parseFormat: string | string[]): Moment | null {
		const initialParse = super.parse(value, parseFormat);
		if (!initialParse) {
			return null
		}

		if (!initialParse.isValid()) { return initialParse; }

		const result = moment.utc({
			year: initialParse.year(),
			month: initialParse.month(),
			date: initialParse.date(),
			hour: initialParse.hour(),
			minute: initialParse.minute()
		}).locale(this.locale);
		return result;
	}
}
