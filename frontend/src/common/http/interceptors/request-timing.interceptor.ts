import { HttpEvent, HttpHandler, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoggingService } from '../../../app/core/services/logging/logging-service';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class RequestTimingInterceptor extends BaseInterceptor {

	constructor(
		private logger: LoggingService,
		configurationService: ConfigurationService) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.RequestTiming; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const started = Date.now();
		return next
			.handle(req).pipe(
				tap(event => {
					if (event instanceof HttpResponse) {
						const elapsed = Date.now() - started;
						if (req.method === 'POST') {
							this.logger.info(`POST Request at ${req.url} with params: ${req.serializeBody()} took ${elapsed} ms.`);
						} else {
							this.logger.info(`${req.method} Request at ${req.urlWithParams} took ${elapsed} ms.`);
						}
					}
				}));
	}
}
