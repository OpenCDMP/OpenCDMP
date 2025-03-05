import { Injectable } from '@angular/core';
import { IndicatorDashboardConfig } from '@citesa/kpi-client/types';
import { Observable } from 'rxjs';
import { BaseHttpV2Service } from '../../app/core/services/http/base-http-v2.service';
import { ConfigurationService } from '../../app/core/services/configuration/configuration.service';

@Injectable()
export class IndicatorDashboardService {

	private get apiBase(): string { return `${this.installationConfiguration.kpiServiceAddress}api/dashboard`; }

	constructor(
        private http: BaseHttpV2Service,
		private installationConfiguration: ConfigurationService
	) { }

	public getDashboard(dashboardKey: string): Observable<IndicatorDashboardConfig> {
		const url = `${this.apiBase}/by-key/${dashboardKey}`;

		return this.http
			.get<IndicatorDashboardConfig>(url);
	}
}
