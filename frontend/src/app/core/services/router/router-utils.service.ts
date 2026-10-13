import { Injectable } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { TenantHandlingService } from '../tenant/tenant-handling.service';

@Injectable()
export class RouterUtilsService {

	constructor(
		private tenantHandlingService: TenantHandlingService,
		private authService: AuthService
	) {
	}

	// Templates call generateUrl on every change detection, and each call parses and serialises a UrlTree.
	private readonly urlCache = new Map<string, string>();

	generateUrl(url: string | string[], joinAt: string = ''): string {
		const tenant = this.authService.selectedTenant() ?? 'default';
		const path = Array.isArray(url) ? url.join(joinAt) : url;
		const key = tenant + '\n' + path;
		let result = this.urlCache.get(key);
		if (result === undefined) {
			if (this.urlCache.size >= 2000) { this.urlCache.clear(); }
			result = this.tenantHandlingService.getUrlEnrichedWithTenantCode(path, tenant);
			this.urlCache.set(key, result);
		}
		return result;
	}
}