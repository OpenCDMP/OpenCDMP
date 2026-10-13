import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { DepositPreviewModel, DepositPreviewRequest, PreviewFieldOption } from '@app/core/model/deposit/deposit-preview';
import { DepositAuthenticateRequest, DepositRequest } from '@app/core/model/deposit/deposit-request';
import { Guid } from '@common/types/guid';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { DepositAuthMethodResult } from '@app/core/model/deposit/deposit-auth-method-result';
import { DepositFile } from '@app/core/model/deposit/deposit-file';
import { DepositFileKind } from '@app/core/common/enum/deposit-file-kind';

@Injectable()
export class DepositHttpService {

	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}deposit`; }

	getAvailableRepos(reqFields: string[] = []): Observable<DepositConfiguration[]> {
		const url = `${this.apiBase}/repositories/available`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<DepositConfiguration[]>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getRepository(repositoryId: string, reqFields: string[] = []): Observable<DepositConfiguration> {
		const url = `${this.apiBase}//repositories/${repositoryId}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<DepositConfiguration>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getAccessToken(item: DepositAuthenticateRequest): Observable<string> {
		const url = `${this.apiBase}/get-access-token`;
		return this.http.post<string>(url, item).pipe(catchError((error: any) => throwError(error)));
	}

	deposit(item: DepositRequest): Observable<EntityDoi> {
		const url = `${this.apiBase}/deposit`;
		return this.http.post<EntityDoi>(url, item).pipe(catchError((error: any) => throwError(error)));
	}

	preview(item: DepositPreviewRequest): Observable<DepositPreviewModel> {
		const url = `${this.apiBase}/preview`;
		return this.http.post<DepositPreviewModel>(url, item).pipe(catchError((error: any) => throwError(error)));
	}

	searchVocabulary(repositoryId: string, fieldCode: string, planId: Guid, like?: string): Observable<PreviewFieldOption[]> {
		const url = `${this.apiBase}/repositories/${repositoryId}/vocab/${fieldCode}`;
		const params: any = { planId: planId };
		if (like) params.like = like;
		return this.http.get<PreviewFieldOption[]>(url, { params }).pipe(catchError((error: any) => throwError(error)));
	}

	getLogo(repositoryId: string): Observable<HttpResponse<string>> {
		const url = `${this.apiBase}/repositories/${repositoryId}/logo`;
		return this.http.get(url, { responseType: 'text', observe: 'response' });
	}

	getFiles(repositoryId: string, planId: Guid): Observable<DepositFile[]> {
		const url = `${this.apiBase}/repositories/${repositoryId}/files`;
		return this.http.get<DepositFile[]>(url, { params: { planId: planId.toString() } }).pipe(catchError((error: any) => throwError(error)));
	}

	getFile(repositoryId: string, planId: Guid, kind: DepositFileKind): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/repositories/${repositoryId}/files/${kind}`;
		return this.http.get<any>(url, { params: { planId: planId.toString() }, responseType: 'blob', observe: 'response' }).pipe(catchError((error: any) => throwError(error)));
	}

	getAvailableAuthMethods(repositoryId: string): Observable<DepositAuthMethodResult> {
		const url = `${this.apiBase}/repositories/${repositoryId}/get-available-auth-methods`;
		return this.http
			.get<DepositAuthMethodResult>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
}
