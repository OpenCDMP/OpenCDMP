import { Injectable, inject } from "@angular/core";
import { TimezoneService } from "@app/core/services/timezone/timezone-service";
import { MomentDatetimeAdapter } from "@mat-datetimepicker/moment";
import moment, { Moment } from "moment";

@Injectable()
export class MomentUtcDateTimeAdapter extends MomentDatetimeAdapter {

	private timezoneService = inject(TimezoneService);

	// selected from datepicker
	createDatetime(year: number, month: number, date: number, hour: number, minute: number): Moment {
		// Moment.js will create an invalid date if any of the components are out of bounds, but we
		// explicitly check each case so we can throw more descriptive errors.
		if (month < 0 || month > 11) {
			throw Error(`Invalid month index "${month}". Month index has to be between 0 and 11.`);
		}

		if (date < 1) {
			throw Error(`Invalid date "${date}". Date has to be greater than 0.`);
		}

		if (hour < 0 || hour > 23) {
			throw Error(`Invalid hours "${hour}". Hours has to be between 0 and 23.`);
		}

		if (minute < 0 || minute > 59) {
			throw Error(`Invalid minutes "${minute}". Minutes has to between 0 and 59.`);
		}


		const result = moment.utc({ year, month, date, hour, minute }).locale(this.locale);


		if (this.timezoneService) {// !!GOTCHA! super() calls createDate . At that moment it has no timezoneService
			result.tz(this.timezoneService.getCurrentTimezone(), true)
		}

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
		}).locale(this.locale)
			.tz(this.timezoneService.getCurrentTimezone(), true)
		return result;
	}

	format(date: Moment, displayFormat: string): string {
		date.tz(this.timezoneService.getCurrentTimezone());
		return super.format(date, displayFormat);
	}
}
