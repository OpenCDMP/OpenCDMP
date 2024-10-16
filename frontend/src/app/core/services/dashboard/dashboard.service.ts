import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DashboardStatistics } from '@app/core/model/dashboard/dashboard-statistics';
import { RecentActivityItem } from '@app/core/model/dashboard/recent-activity-item';
import { RecentActivityItemLookup } from '@app/core/query/recent-activity-item-lookup.lookup';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';

@Injectable()
export class DashboardService {

	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}dashboard`; }

	getMyRecentActivityItems(q: RecentActivityItemLookup): Observable<RecentActivityItem[]> {
		const url = `${this.apiBase}/mine/recent-activity`;
		return this.http.post<RecentActivityItem[]>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getMyDashboardStatistics(): Observable<DashboardStatistics> {
		const url = `${this.apiBase}/mine/get-statistics`;
		return this.http.get<DashboardStatistics>(url).pipe(catchError((error: any) => throwError(error)));
	}

	getPublicDashboardStatistics(): Observable<DashboardStatistics> {
		const url = `${this.apiBase}/public/get-statistics`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};
		return this.http.get<DashboardStatistics>(url, { params: params }).pipe(catchError((error: any) => throwError(error)));
	}
}
