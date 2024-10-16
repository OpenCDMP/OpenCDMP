import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ProgressIndicationService } from '../../../app/core/services/progress-indication/progress-indication-service';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class ProgressIndicationInterceptor extends BaseInterceptor {

	constructor(
		private progressIndicationService: ProgressIndicationService,
		configurationService: ConfigurationService) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.ProgressIndication; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		this.progressIndicationService.show();
		return next
			.handle(req).pipe(
				finalize(() => {
					this.progressIndicationService.dismiss();
				}));
	}
}
