import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { SemanticsLookup } from '@app/core/query/semantic.lookup';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';

@Injectable()
export class SemanticsService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}semantics`; }


	searchSemantics(q: SemanticsLookup ): Observable<String[]> {
		const url = `${this.apiBase}`;
		return this.http.post<String[]>(url, q).pipe(catchError((error: any) => throwError(error)));
	}


	// Autocomplete

	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.searchSemantics(this.buildSemanticsAutocompleteLookup()).pipe(map(x => x)),
		filterFn: (searchQuery: string, data?: any) => this.searchSemantics(this.buildSemanticsAutocompleteLookup(searchQuery)).pipe(map(x => x)),
		displayFn: (item) => item,
		titleFn: (item) => item,
		valueAssign: (item) => item,
	};

	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.searchSemantics(this.buildSemanticsAutocompleteLookup()).pipe(map(x => x)),
		filterFn: (searchQuery: string, data?: any) => this.searchSemantics(this.buildSemanticsAutocompleteLookup(searchQuery)).pipe(map(x => x)),
		displayFn: (item) => item,
		titleFn: (item) => item,
	}

	private buildSemanticsAutocompleteLookup(like?: string ): SemanticsLookup {
		const lookup: SemanticsLookup = new SemanticsLookup();
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}
