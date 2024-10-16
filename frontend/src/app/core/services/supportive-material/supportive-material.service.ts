import { HttpResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { SupportiveMaterial, SupportiveMaterialPersist } from "@app/core/model/supportive-material/supportive-material";
import { SupportiveMaterialLookup } from "@app/core/query/supportive-material.lookup";
import { QueryResult } from "@common/model/query-result";
import { Guid } from "@common/types/guid";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { nameof } from "ts-simple-nameof";
import { ConfigurationService } from "../configuration/configuration.service";
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { SupportiveMaterialFieldType } from "@app/core/common/enum/supportive-material-field-type";
import { BaseHttpParams } from "@common/http/base-http-params";
import { InterceptorType } from "@common/http/interceptors/interceptor-type";
import { AuthService } from "../auth/auth.service";

@Injectable()
export class SupportiveMaterialService {

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
		private authService: AuthService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}supportive-material`; }

	getPayload(type: SupportiveMaterialFieldType, language: string): Observable<HttpResponse<Blob>> {
		if (this.authService.isLoggedIn() && this.authService.currentAccountIsAuthenticated() ) {
			return this.getPayloadLogin(type, language);
		} else {
			return this.getPayloadPublic(type, language);
		}
	}

	getPayloadLogin(type: SupportiveMaterialFieldType, language: string): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/get-payload/${type}/${language}`;
		return this.http.get<HttpResponse<Blob>>(url, { responseType: 'blob', observe: 'response' });
	}

	getPayloadPublic(type: SupportiveMaterialFieldType, language: string): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/public/get-payload/${type}/${language}`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};
		return this.http.get(url, { params: params, responseType: 'blob', observe: 'response' });
	}

	query(q: SupportiveMaterialLookup): Observable<QueryResult<SupportiveMaterial>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<SupportiveMaterial>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<SupportiveMaterial> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<SupportiveMaterial>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: SupportiveMaterialPersist): Observable<SupportiveMaterial> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<SupportiveMaterial>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<SupportiveMaterial> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<SupportiveMaterial>(url).pipe(
				catchError((error: any) => throwError(error)));
	}


	// LOOKUP

	public static DefaultSupportiveMaterialLookup(): SupportiveMaterialLookup {
		const lookup = new SupportiveMaterialLookup();

		lookup.project = {
			fields: [
				nameof<SupportiveMaterial>(x => x.id),
				nameof<SupportiveMaterial>(x => x.type),
				nameof<SupportiveMaterial>(x => x.languageCode),
				nameof<SupportiveMaterial>(x => x.payload),
				nameof<SupportiveMaterial>(x => x.createdAt),
				nameof<SupportiveMaterial>(x => x.updatedAt),
				nameof<SupportiveMaterial>(x => x.isActive)
			]
		};
		lookup.order = { items: [nameof<SupportiveMaterial>(x => x.type)] };
		lookup.page = { offset: 0, size: 10 };
		lookup.isActive = [IsActive.Active];
		return lookup;
	}

}
