
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class BaseHttpV2Service {
	constructor(
		protected http: HttpClient
	) {
	}

	get<T>(url: string, options?: Object): Observable<T> {
		return this.http.get<T>(url, options);
	}

	post<T>(url: string, body: any, options?: Object): Observable<T> {
		return this.http.post<T>(url, body, options);
	}

	put<T>(url: string, body: any, options?: Object): Observable<T> {
		return this.http.put<T>(url, body, options);
	}

	delete<T>(url: string, options?: Object): Observable<T> {
		return this.http.delete<T>(url, options);
	}

	patch<T>(url: string, body: any, options?: Object): Observable<T> {
		return this.http.patch<T>(url, body, options);
	}

	head<T>(url: string, options?: Object): Observable<T> {
		return this.http.head<T>(url, options);
	}

	options<T>(url: string, options?: Object): Observable<T> {
		return this.http.options<T>(url, options);
	}
}
