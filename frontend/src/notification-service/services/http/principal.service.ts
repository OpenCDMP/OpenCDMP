import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { NotificationAccount } from '@notification-service/core/model/principal';

@Injectable()
export class NotificationPrincipalService {

	private get apiBase(): string { return `${this.installationConfiguration.notificationServiceAddress}api/notification/principal`; }

	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service
	) { }

	public me(options?: Object): Observable<NotificationAccount> {
		const url = `${this.apiBase}/me`;
		return this.http.get<NotificationAccount>(url, options);
	}
}
