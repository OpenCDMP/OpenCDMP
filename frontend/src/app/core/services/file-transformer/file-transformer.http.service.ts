import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { RepositoryFileFormat } from '@app/core/model/file/file-format.model';

@Injectable()
export class FileTransformerHttpService extends BaseService {

	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService
	) { super(); }

	private get apiBase(): string { return `${this.configurationService.server}file-transformer`; }

	getAvailableConfigurations(): Observable<RepositoryFileFormat[]> {
		const url = `${this.apiBase}/available`;
		return this.http.get<RepositoryFileFormat[]>(url).pipe(catchError((error: any) => throwError(error)));
	}

	exportPlan(planId: Guid, repositoryId: string, format: string): Observable<any> {
		const url = `${this.apiBase}/export-plan`;
		return this.http.post<any>(url, {id: planId, repositoryId: repositoryId, format: format}, {responseType: 'blob', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}

	exportPublicPlan(planId: Guid, repositoryId: string, format: string): Observable<any> {
		const url = `${this.apiBase}/export-public-plan`;
		return this.http.post<any>(url, {id: planId, repositoryId: repositoryId, format: format}, {responseType: 'blob', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}

	exportDescription(id: Guid, repositoryId: string, format: string): Observable<any> {
		const url = `${this.apiBase}/export-description`;
		return this.http.post<any>(url, {id: id, repositoryId: repositoryId, format: format}, {responseType: 'blob', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}

	exportPublicDescription(id: Guid, repositoryId: string, format: string): Observable<any> {
		const url = `${this.apiBase}/export-public-description`;
		return this.http.post<any>(url, {id: id, repositoryId: repositoryId, format: format}, {responseType: 'blob', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}
}
