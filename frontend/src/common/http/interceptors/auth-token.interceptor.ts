import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { AuthService } from '../../../app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class AuthTokenInterceptor extends BaseInterceptor {

	constructor(
		private authService: AuthService,
		configurationService: ConfigurationService) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.AuthToken; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const authToken: string = this.authService.hasAccessToken() ? this.authService.currentAuthenticationToken() : null;
		if (!authToken) { return next.handle(req); }
		req = req.clone({
			setHeaders: {
				Authorization: `Bearer ${authToken}`
			}
		});
		return next.handle(req);
	}
}
