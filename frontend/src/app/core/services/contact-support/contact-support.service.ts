import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { ContactSupportPersist, PublicContactSupportPersist } from "../../model/contact/contact-support-form-model";
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { catchError } from "rxjs/operators";

@Injectable()
export class ContactSupportService {


	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}contact-support`; }

	send(item: ContactSupportPersist): Observable<ContactSupportPersist> {
		const url = `${this.apiBase}/send`;

		return this.http
			.post<ContactSupportPersist>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendPublic(item: PublicContactSupportPersist): Observable<PublicContactSupportPersist> {
		const url = `${this.apiBase}/public/send`;

		return this.http
			.post<PublicContactSupportPersist>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

}
