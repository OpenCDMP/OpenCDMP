import { Injectable } from '@angular/core';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from './base-http-v2.service';
import { Observable } from 'rxjs';

@Injectable()
export class UserTermsAcceptanceService {

	private get apiBase(): string {
		return `${this.configurationService.server}user-terms-acceptance`;
	}

	constructor(
		private configurationService: ConfigurationService,
		private http: BaseHttpV2Service,
	) { }

	accept(): Observable<void> {
		return this.http.post<void>(`${this.apiBase}/accept`, null);
	}
}
