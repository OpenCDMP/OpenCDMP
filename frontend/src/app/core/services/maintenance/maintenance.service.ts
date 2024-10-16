import { Injectable } from "@angular/core";
import { BaseService } from "@common/base/base.service";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { ConfigurationService } from "../configuration/configuration.service";
import { BaseHttpV2Service } from "../http/base-http-v2.service";

@Injectable()
export class MaintenanceService extends BaseService {

	constructor(
		private http: BaseHttpV2Service,
		private installationConfiguration: ConfigurationService,
	) { super(); }

	private get apiBase(): string { return `${this.installationConfiguration.server}maintenance`; }

	generateElasticIndex(): Observable<any> {
		const url = `${this.apiBase}/index/elastic`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	clearElasticIndex(): Observable<any> {
		const url = `${this.apiBase}/index/elastic`;
		return this.http
			.delete<any>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendUserTouchEvents(): Observable<any> {
		const url = `${this.apiBase}/events/users/touch`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendTenantTouchEvents(): Observable<any> {
		const url = `${this.apiBase}/events/tenants/touch`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendPlanTouchEvents(): Observable<any> {
		const url = `${this.apiBase}/events/plans/touch`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendDescriptionTouchEvents(): Observable<any> {
		const url = `${this.apiBase}/events/descriptions/touch`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendPlanAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/plans/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendDescriptionAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/descriptions/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	
	sendBlueprintAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/plan-blueprints/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendDescriptionTemplateAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/description-templates/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendDescriptionTemplateTypeAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/description-template-types/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendPrefillingSourceAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/prefilling-sources/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendReferenceTypeAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/reference-types/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendUserAccountingEntriesEvents(): Observable<any> {
		const url = `${this.apiBase}/events/users/accounting-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendIndicatorCreateEvents(): Observable<any> {
		const url = `${this.apiBase}/events/indicator-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendIndicatorResetEvents(): Observable<any> {
		const url = `${this.apiBase}/events/indicator-reset-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendIndicatorAccessEvents(): Observable<any> {
		const url = `${this.apiBase}/events/indicator-access-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendIndicatorPointEvents(): Observable<any> {
		const url = `${this.apiBase}/events/indicator-point-entry`;
		return this.http
			.post<any>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}
}
