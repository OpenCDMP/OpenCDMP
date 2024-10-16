import { Injectable } from '@angular/core';
import { UserSettingPersist, UserSettings } from '@app/core/model/user-settings/user-settings.model';
import { Guid } from '@common/types/guid';
import { Observable } from 'rxjs';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class UserSettingsHttpService {
	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service) { }

	private get apiBase(): string { return `${this.installationConfiguration.server}user-settings`; }

	getSingle(key: string): Observable<UserSettings> {
		const url = `${this.apiBase}/${key}`;

		return this.http.get<UserSettings>(url);
	}

	persist(item: UserSettingPersist): Observable<UserSettings> {
		const url = `${this.apiBase}/persist`;

		return this.http.post<UserSettings>(url, item);
	}

	persistAll(items: UserSettingPersist[]): Observable<UserSettings[]> {
		const url = `${this.apiBase}/persist-all-default`;

		return this.http.post<UserSettings[]>(url, items);
	}

	delete(id: Guid): Observable<UserSettings> {
		const url = `${this.apiBase}/${id}`;
		return this.http
			.delete<UserSettings>(url);
	}

	share(item: UserSettingPersist, targetId: Guid): Observable<UserSettings> {
		const url = `${this.apiBase}/share/${targetId}`;

		return this.http.post<UserSettings>(url, item);
	}
}
