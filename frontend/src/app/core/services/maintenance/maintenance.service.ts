import { Injectable } from "@angular/core";
import { BaseService } from "@common/base/base.service";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { DatabaseMigrationInfo, InstallationInfo, InstallationPersist, MaintenanceRun, MaintenanceRunPersist, MaintenanceTaskRun } from "@app/core/model/maintenance/maintenance-run";
import { ConfigurationService } from "../configuration/configuration.service";
import { BaseHttpV2Service } from "../http/base-http-v2.service";

@Injectable()
export class MaintenanceService extends BaseService {

	constructor(
		private http: BaseHttpV2Service,
		private installationConfiguration: ConfigurationService,
	) { super(); }

	private get apiBase(): string { return `${this.installationConfiguration.server}maintenance`; }

	clearElasticIndex(): Observable<any> {
		const url = `${this.apiBase}/index/elastic`;
		return this.http
			.delete<any>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	startRun(model: MaintenanceRunPersist): Observable<MaintenanceRun> {
		const url = `${this.apiBase}/runs`;
		return this.http
			.post<MaintenanceRun>(url, model).pipe(
				catchError((error: any) => throwError(error)));
	}

	latestRun(): Observable<MaintenanceRun | null> {
		const url = `${this.apiBase}/runs/latest`;
		return this.http
			.get<MaintenanceRun | null>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	stopRun(id: string): Observable<MaintenanceRun> {
		const url = `${this.apiBase}/runs/${id}/stop`;
		return this.http
			.post<MaintenanceRun>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	lastTaskRuns(): Observable<MaintenanceTaskRun[]> {
		const url = `${this.apiBase}/tasks/last-runs`;
		return this.http
			.get<MaintenanceTaskRun[]>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	databaseMigrations(): Observable<DatabaseMigrationInfo> {
		const url = `${this.apiBase}/database-migrations`;
		return this.http
			.get<DatabaseMigrationInfo>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	installation(): Observable<InstallationInfo> {
		const url = `${this.apiBase}/installation`;
		return this.http
			.get<InstallationInfo>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	persistInstallation(model: InstallationPersist): Observable<InstallationInfo> {
		const url = `${this.apiBase}/installation`;
		return this.http
			.post<InstallationInfo>(url, model).pipe(
				catchError((error: any) => throwError(error)));
	}
}
