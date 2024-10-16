import { Injectable } from '@angular/core';
import { TimezoneInfoDisplayPipe } from '@app/core/pipes/timezone-info-display.pipe';
import { DefaultUserLocaleCultureLookup, DefaultUserLocaleTimezoneLookup } from '@app/core/query/default-user-locale.lookup';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import moment from 'moment';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { CultureInfo, CultureService } from '../culture/culture-service';

@Injectable()
export class DefaultUserLocaleService {

	constructor(
		private timezoneInfoDisplayPipe: TimezoneInfoDisplayPipe,
		private cultureService: CultureService
	) { }


	queryTimezone(q: DefaultUserLocaleTimezoneLookup): Observable<string[]> {
		let timezones = of(moment.tz.names().sort((x, y) => x.localeCompare(y)));

		if (q.like) {
			let likeValue = q.like.toLowerCase();
			timezones = timezones.pipe(map((items: string[]) => {
				let filteredItems: string[] = items.filter(i => this.timezoneInfoDisplayPipe.transform(i).toLowerCase().includes(likeValue));
				if (filteredItems != null && filteredItems?.length > 0) return filteredItems;
				else return null;
			}));
		}

		if (q.selectedItem) {
			timezones = timezones.pipe(map((items: string[]) => {
				let filteredItem: string = items.find(i => i === q.selectedItem);
				if (filteredItem != null) return [filteredItem];
				else return null;
			}));
		}

		return timezones;
	}

	queryCulture(q: DefaultUserLocaleCultureLookup) {
		let cultures = of(this.cultureService.getCultureValues().sort((x, y) => x.displayName.localeCompare(y.displayName)));

		if (q.like) {
			let likeValue = q.like.toLowerCase();
			cultures = cultures.pipe(map((items: CultureInfo[]) => {
				let filteredItems = items.filter(i => {
					const displayValue = `${i?.displayName} - ${i?.name}`;
					if (displayValue.toLowerCase().includes(likeValue)) {
						return true;
					} else {
						return false;
					}
				});
				if (filteredItems != null && filteredItems?.length > 0) return filteredItems;
				else return null;
			}));
		}

		if (q.selectedItem) {
			let selectedItemValue = q.selectedItem.toLowerCase();
			cultures = cultures.pipe(map((items: CultureInfo[]) => {
				let selected = items.find(i => {
					if (i.name.toLowerCase().includes(selectedItemValue)) {
						return true;
					} else {
						return false;
					}
				});
				if (selected != null) {
					return [selected];
				}
				else return null;
			}));
		}

		return cultures;
	}


	//
	// Autocomplete Commons
	//
	singleTimezoneAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.queryTimezone(this.buildTimezoneAutocompleteLookup(null)),
		filterFn: (searchQuery: string, data?: any) => this.queryTimezone(this.buildTimezoneAutocompleteLookup(searchQuery)),
		getSelectedItem: (selectedItem: string) => this.queryTimezone(this.buildTimezoneAutocompleteLookup(null, selectedItem)),
		displayFn: (item: string) => this.getTimezoneDisplayFn(item),
		titleFn: (item: string) => this.timezoneInfoDisplayPipe.transform(item),
		valueAssign: (item: string) => item,
	};

	private buildTimezoneAutocompleteLookup(like?: string, selectedItem?: string): DefaultUserLocaleTimezoneLookup {
		const lookup: DefaultUserLocaleTimezoneLookup = new DefaultUserLocaleTimezoneLookup();
		lookup.page = { size: 100, offset: 0 };
		if (like) { lookup.like = like; }
		if (selectedItem) { lookup.selectedItem = selectedItem }
		return lookup;
	}

	getTimezoneDisplayFn(value: any) {
		return Array.isArray(value) ? this.timezoneInfoDisplayPipe.transform(value[0]) : this.timezoneInfoDisplayPipe.transform(value);
	}


	singleCultureAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.queryCulture(this.buildCultureAutocompleteLookup(null)),
		filterFn: (searchQuery: string, data?: any) => this.queryCulture(this.buildCultureAutocompleteLookup(searchQuery)),
		getSelectedItem: (selectedItem: string) => this.queryCulture(this.buildCultureAutocompleteLookup(null, selectedItem)),
		displayFn: (item: any) => this.getCultureDisplayFn(item),
		titleFn: (item: CultureInfo) => this.getCultureTitleFn(item),
		valueAssign: (item: CultureInfo) => item.name,
	};

	private buildCultureAutocompleteLookup(like?: string, selectedItem?: string): DefaultUserLocaleCultureLookup {
		const lookup: DefaultUserLocaleCultureLookup = new DefaultUserLocaleCultureLookup();
		lookup.page = { size: 100, offset: 0 };
		if (like) { lookup.like = like; }
		if (selectedItem) { lookup.selectedItem = selectedItem }
		return lookup;
	}

	private getCultureTitleFn(culture: any): string {
		return `${culture?.displayName} - ${culture?.name}`;
	}

	private getCultureDisplayFn(culture: any): string {
		if (Array.isArray(culture)) {
			return this.getCultureTitleFn(culture[0]);
		} else {
			return this.getCultureTitleFn(culture);
		}
	}
}
