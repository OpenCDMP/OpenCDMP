import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseHttpParams } from '../base-http-params';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

export abstract class BaseInterceptor implements HttpInterceptor {

	constructor(public configurationService: ConfigurationService) { }

	abstract type: InterceptorType;
	abstract interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>;

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (this.isApplied(req)) {
			return this.interceptRequest(req, next);
		}
		return next.handle(req);
	}

	isApplied(req: HttpRequest<any>): boolean {

		if (req.params instanceof BaseHttpParams && req.params.interceptorContext && Array.isArray(req.params.interceptorContext.excludedInterceptors) && req.params.interceptorContext.excludedInterceptors.includes(this.type)) {
			return false;
		}

		return (req.params instanceof BaseHttpParams && req.params.interceptorContext && Array.isArray(req.params.interceptorContext.interceptAllRequests) && req.params.interceptorContext.interceptAllRequests.includes(this.type))
			|| req.url.startsWith(this.configurationService.server)
			|| req.url.startsWith(this.configurationService.notificationServiceAddress)
			|| req.url.startsWith(this.configurationService.annotationServiceAddress)
			|| req.url.startsWith(this.configurationService.kpiServiceAddress);
	}
}
