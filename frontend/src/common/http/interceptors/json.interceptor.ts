import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class JsonInterceptor extends BaseInterceptor {

	constructor(
		configurationService: ConfigurationService
	) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.JSONContentType; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (!req.headers.has('Content-Type')) { req = req.clone({ headers: req.headers.set('Content-Type', 'application/json') }); }
		if (!req.headers.has('Accept')) { req = req.clone({ headers: req.headers.set('Accept', 'application/json') }); }

		return next.handle(req);
	}

}
