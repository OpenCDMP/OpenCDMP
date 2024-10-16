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

	generateUrl(url: string | string[], joinAt: string = ''): string {
		const tenant = this.authService.selectedTenant() ?? 'default';
		if (Array.isArray(url)) {
			return this.tenantHandlingService.getUrlEnrichedWithTenantCode(url.join(joinAt), tenant)
		} else {
			return this.tenantHandlingService.getUrlEnrichedWithTenantCode(url, tenant);
		}
	}
}