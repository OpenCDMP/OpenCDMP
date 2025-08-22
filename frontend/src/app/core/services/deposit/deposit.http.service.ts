import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { DepositAuthenticateRequest, DepositRequest } from '@app/core/model/deposit/deposit-request';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { DepositAuthMethodResult } from '@app/core/model/deposit/deposit-auth-method-result';

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

	getLogo(repositoryId: string): Observable<HttpResponse<string>> {
		const url = `${this.apiBase}/repositories/${repositoryId}/logo`;
		return this.http.get(url, { responseType: 'string', observe: 'response' });
	}

	getAvailableAuthMethods(repositoryId: string): Observable<DepositAuthMethodResult> {
		const url = `${this.apiBase}/repositories/${repositoryId}/get-available-auth-methods`;
		return this.http
			.get<DepositAuthMethodResult>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
}
