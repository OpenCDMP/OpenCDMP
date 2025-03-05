import { Injectable } from '@angular/core';
import { AnnotationAccount } from '@annotation-service/core/model/principal';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { Observable } from 'rxjs';

@Injectable()
export class AnnotationPrincipalService {

	private get apiBase(): string { return `${this.installationConfiguration.annotationServiceAddress}api/annotation/principal`; }

	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service
	) { }

	public me(options?: Object): Observable<AnnotationAccount> {
		const url = `${this.apiBase}/me`;
		return this.http.get<AnnotationAccount>(url, options);
	}
}
