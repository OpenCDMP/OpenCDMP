import { Injectable } from '@angular/core';
import { UserSettingPersist, UserSettings } from '@app/core/model/user-settings/user-settings.model';
import { Guid } from '@common/types/guid';
import { Observable, forkJoin, of } from 'rxjs';
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
		// Ask for the full row back: the caller re-caches the response, and the next update needs the new hash.
		const options = { params: { f: ['id', 'key', 'value', 'entityId', 'type', 'hash', 'createdAt', 'updatedAt'] } };

		return this.http.post<UserSettings>(url, item, options);
	}

	/**
	 * The backend only exposes a single-item `persist` (the former `persist-all-default` URL never
	 * existed server-side, so settings were only ever cached in localStorage). Persist each item and
	 * hand back the per-key results in the same shape the callers expect.
	 */
	persistAll(items: UserSettingPersist[]): Observable<UserSettings[]> {
		if (!items?.length) { return of([]); }
		return forkJoin(items.map(item => this.persist(item)));
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
