import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';

@Injectable()
export class StorageFileService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}storage-file`; }

	getSingle(id: Guid, reqFields: string[] = []): Observable<StorageFile> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<StorageFile>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	download(id: Guid ): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/download/${id}`;

		return this.http.get(url,  { responseType: 'blob', observe: 'response' });
	}

	uploadTempFiles(item: File): Observable<StorageFile[]> {
		const url = `${this.apiBase}/upload-temp-files`;

		const formData: FormData = new FormData();
		formData.append('files', item);

		const params = new BaseHttpParams();
        params.interceptorContext = {
            excludedInterceptors: [InterceptorType.JSONContentType]
        };

		return this.http
			.post<StorageFile[]>(url, formData, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

}
