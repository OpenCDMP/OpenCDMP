import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TenantCopyRequest, TenantPurgeReport, TenantPurgeRequest, TransferExportRequest, TransferImportReport } from '@app/core/model/tenant-transfer/tenant-transfer';
import { BaseService } from '@common/base/base.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class TenantTransferService extends BaseService {

	constructor(
		private http: BaseHttpV2Service,
		private installationConfiguration: ConfigurationService,
	) { super(); }

	private get apiBase(): string { return `${this.installationConfiguration.server}transfer`; }

	exportBundle(request: TransferExportRequest): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/export`;
		return this.http
			.post<any>(url, request, { responseType: 'blob', observe: 'response' }).pipe(
				catchError((error: any) => throwError(error)));
	}

	analyzeImport(file: File): Observable<TransferImportReport> {
		const url = `${this.apiBase}/import/analyze`;
		return this.http
			.post<TransferImportReport>(url, this.buildFormData(file), { params: this.multipartParams() }).pipe(
				catchError((error: any) => throwError(error)));
	}

	importBundle(file: File): Observable<TransferImportReport> {
		const url = `${this.apiBase}/import`;
		return this.http
			.post<TransferImportReport>(url, this.buildFormData(file), { params: this.multipartParams() }).pipe(
				catchError((error: any) => throwError(error)));
	}

	analyzeCopy(request: TenantCopyRequest): Observable<TransferImportReport> {
		const url = `${this.apiBase}/copy/analyze`;
		return this.http
			.post<TransferImportReport>(url, request).pipe(
				catchError((error: any) => throwError(error)));
	}

	copyTenant(request: TenantCopyRequest): Observable<TransferImportReport> {
		const url = `${this.apiBase}/copy`;
		return this.http
			.post<TransferImportReport>(url, request).pipe(
				catchError((error: any) => throwError(error)));
	}

	analyzePurge(request: TenantPurgeRequest): Observable<TenantPurgeReport> {
		const url = `${this.apiBase}/purge/analyze`;
		return this.http
			.post<TenantPurgeReport>(url, request).pipe(
				catchError((error: any) => throwError(error)));
	}

	purge(request: TenantPurgeRequest): Observable<TenantPurgeReport> {
		const url = `${this.apiBase}/purge`;
		return this.http
			.post<TenantPurgeReport>(url, request).pipe(
				catchError((error: any) => throwError(error)));
	}

	private buildFormData(file: File): FormData {
		const formData = new FormData();
		formData.append('file', file);
		return formData;
	}

	private multipartParams(): BaseHttpParams {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return params;
	}
}
