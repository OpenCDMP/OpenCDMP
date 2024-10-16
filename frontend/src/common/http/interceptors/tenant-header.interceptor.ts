import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseInterceptor } from '@common/http/interceptors/base.interceptor';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { Observable } from 'rxjs';

@Injectable()
export class TenantHeaderInterceptor extends BaseInterceptor {

	constructor(
		public installationConfiguration: ConfigurationService,
		private authService: AuthService) { super(installationConfiguration); }

	get type(): InterceptorType { return InterceptorType.TenantHeaderInterceptor; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const selectedTenant: string = this.authService.selectedTenant();
		if (!selectedTenant) { return next.handle(req); }

		req = req.clone({
			headers: req.headers.set('x-tenant', selectedTenant)
		});
		return next.handle(req);
	}
}
