import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Report, ReportOutline, ReportQuestionSearch } from '@app/core/model/report/report';
import { ReportLookup, ReportQuestionSearchLookup } from '@app/core/query/report.lookup';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class ReportService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}report`; }

	descriptionTemplateOutline(groupId: Guid, installationWide = false): Observable<ReportOutline> {
		const url = `${this.apiBase}/outline/description-template/${groupId}`;
		return this.http.get<ReportOutline>(url, { params: { installationWide } }).pipe(catchError((error: any) => throwError(error)));
	}

	planBlueprintOutline(groupId: Guid, installationWide = false): Observable<ReportOutline> {
		const url = `${this.apiBase}/outline/plan-blueprint/${groupId}`;
		return this.http.get<ReportOutline>(url, { params: { installationWide } }).pipe(catchError((error: any) => throwError(error)));
	}

	searchQuestions(lookup: ReportQuestionSearchLookup): Observable<ReportQuestionSearch> {
		const url = `${this.apiBase}/questions/search`;
		return this.http.post<ReportQuestionSearch>(url, lookup).pipe(catchError((error: any) => throwError(error)));
	}

	query(lookup: ReportLookup): Observable<Report> {
		const url = `${this.apiBase}/query`;
		return this.http.post<Report>(url, lookup).pipe(catchError((error: any) => throwError(error)));
	}

	exportCsv(lookup: ReportLookup): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/export/csv`;
		return this.http.post<any>(url, lookup, { responseType: 'blob', observe: 'response' })
			.pipe(catchError((error: any) => throwError(error)));
	}
}
