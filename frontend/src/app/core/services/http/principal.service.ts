import { Injectable } from '@angular/core';
import { AppAccount } from '@app/core/model/auth/principal';
import { Observable } from 'rxjs';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { Tenant } from '@app/core/model/tenant/tenant';

@Injectable()
export class PrincipalService {

	private get apiBase(): string { return `${this.installationConfiguration.server}principal`; }

	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service
	) { }

	public me(options?: Object): Observable<AppAccount> {
		const url = `${this.apiBase}/me`;
		return this.http.get<AppAccount>(url, options);
	}

	public myTenants(options?: Object): Observable<Array<Tenant>> {
		const url = `${this.apiBase}/my-tenants`;
		return this.http.get<Array<Tenant>>(url, options);
	}
}
