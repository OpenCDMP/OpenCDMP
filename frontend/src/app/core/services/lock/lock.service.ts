import { HttpHeaders, HttpParamsOptions } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Lock, LockPersist, LockStatus, UnlockMultipleTargetsPersist } from '@app/core/model/lock/lock.model';
import { LockLookup } from '@app/core/query/lock.lookup';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { BaseHttpParams } from '@common/http/base-http-params';

@Injectable()
export class LockService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}lock`; }

	query(q: LockLookup): Observable<QueryResult<Lock>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Lock>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Lock> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Lock>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: LockPersist): Observable<Lock> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Lock>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid, target: Guid): Observable<Lock> {
		const url = `${this.apiBase}/${id}/${target}`;

		return this.http
			.delete<Lock>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	checkLockStatus(targetId: Guid, reqFields: string[] = []): Observable<LockStatus> {
		const url = `${this.apiBase}/target/status/${targetId}`;
		const options = { params: { f: reqFields } };

		return this.http.get<LockStatus>(url, options)
			.pipe(catchError((error: any) => throwError(error)));
	}
	
	lock(targetId: Guid, targetType: LockTargetType): Observable<boolean> {
		return this.http.get<boolean>(`${this.apiBase}/target/lock/${targetId}/${targetType}`)
			.pipe(catchError((error: any) => throwError(error)));
	}

	touchLock(targetId: Guid): Observable<Boolean> {
		return this.http.get<Boolean>(`${this.apiBase}/target/touch/${targetId}`)
			.pipe(catchError((error: any) => throwError(error)));
	}

	unlockTarget(targetId: Guid): Observable<any> {
		return this.http.delete(`${this.apiBase}/target/unlock/${targetId}`)
			.pipe(catchError((error: any) => throwError(error)));
	}

	getSingleWithTarget(targetId: Guid, reqFields: string[] = []): Observable<Lock> {
		const url = `${this.apiBase}/target/${targetId}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Lock>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	checkAndLock(targetId: Guid, targetType: LockTargetType): Observable<Boolean> {
		return this.http.get<Boolean>(`${this.apiBase}/target/check-lock/${targetId}/${targetType}`)
			.pipe(catchError((error: any) => throwError(error)));
	}

	unlockTargetMultiple(item: UnlockMultipleTargetsPersist): Observable<boolean> {
		const url = `${this.apiBase}/target/unlock-multiple`;
		return this.http.post<boolean>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}
}
