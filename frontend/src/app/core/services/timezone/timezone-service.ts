import { Injectable } from '@angular/core';
import moment from 'moment';
import 'moment-timezone';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class TimezoneService {

	private timezoneValues: string[];
	private timezoneChangeSubject = new Subject<string>();
	private currentTimezone: string;

	constructor() {
		this.timezoneValues = moment.tz.names();
	}

	getTimezoneValues(): string[] {
		return this.timezoneValues;
	}

	hasTimezoneValue(timezone: string): boolean {
		return this.timezoneValues.includes(timezone);
	}

	timezoneSelected(timezone: string) {
		if (this.currentTimezone === timezone) { return; }
		this.currentTimezone = timezone;
		this.timezoneChangeSubject.next(timezone);
	}

	getTimezoneChangeObservable(): Observable<string> {
		return this.timezoneChangeSubject.asObservable();
	}

	getCurrentTimezone(): string {
		return this.currentTimezone;
	}

	public buildDateTime(params: {
		time: string,
		date: moment.Moment | string
	}): moment.Moment {

		const { time, date } = params;

		if (!time || !date) {
			return null;
		}

		const momentTime = moment.duration(time);
		const momentDate = moment(date);

		const toReturn = moment.utc({
			year: momentDate.year(),
			month: momentDate.month(),
			date: momentDate.date(),
			hour: momentTime.hours(),
			minute: momentTime.minutes()
		})

		return toReturn.tz(this.getCurrentTimezone(), true);
	}


	public splitDateTime(params: {
		dateTime: moment.Moment,
	}): { date: moment.Moment, time: string } | null {


		const { dateTime } = params

		if (!dateTime) {
			return null;
		}


		const dateTimeMoment = moment(dateTime).tz(this.getCurrentTimezone());


		const date =
			moment.utc({
				year: dateTimeMoment.year(),
				month: dateTimeMoment.month(),
				date: dateTimeMoment.date(),
			});

		const hours = dateTimeMoment.hour();
		const minutes = dateTimeMoment.minute();


		const hoursString = hours > 10 ? hours.toString() : `0${hours}`
		const minutesString = hours > 10 ? minutes.toString() : `0${minutes}`




		return {
			date,
			time: `${hoursString}:${minutesString}:00`
		};
	}
}
