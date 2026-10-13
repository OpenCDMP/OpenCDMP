import { Injectable, inject } from '@angular/core';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { Observable, shareReplay } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

export type PluginSourceProvider = 'installation' | 'tenant';

/** One deposit, evaluator or file transformer plugin as the tenant administrator sees it. */
export interface PluginSourceOverview {
	pluginType: PluginType;
	id: string;
	provider: PluginSourceProvider;
	enabled: boolean;
	/** Null when the plugin is switched off and was therefore not contacted. */
	reachable: boolean | null;
	/** Only for the tenant's own plugins. */
	url?: string;
}

/** With only `pluginType` and `id`, tests the plugin already in effect; a blank secret reuses the stored one. */
export interface PluginConnectionTestRequest {
	pluginType: PluginType;
	id: string;
	url?: string;
	issuerUrl?: string;
	clientId?: string;
	clientSecret?: string;
	scope?: string;
}

export interface PluginConnectionTestResult {
	success: boolean;
	reportedId: string | null;
	message: string | null;
}

@Injectable({ providedIn: 'root' })
export class PluginIntegrationService {
	private readonly http = inject(BaseHttpV2Service);
	private readonly configurationService = inject(ConfigurationService);
	private readonly authService = inject(AuthService);
	private sources: { tenant: string, request: Observable<PluginSourceOverview[]> } | null = null;

	private get apiBase(): string { return `${this.configurationService.server}plugin-integration`; }

	// Reachability is a live check that can differ between two calls, so the overview card and the Plugins list share one answer.
	getSources(reload = false): Observable<PluginSourceOverview[]> {
		const tenant = this.authService.selectedTenant() ?? '';
		if (reload || this.sources?.tenant !== tenant) {
			this.sources = { tenant, request: this.http.get<PluginSourceOverview[]>(`${this.apiBase}/sources`).pipe(shareReplay(1)) };
		}
		return this.sources.request;
	}

	test(request: PluginConnectionTestRequest): Observable<PluginConnectionTestResult> {
		return this.http.post<PluginConnectionTestResult>(`${this.apiBase}/test`, request);
	}
}
