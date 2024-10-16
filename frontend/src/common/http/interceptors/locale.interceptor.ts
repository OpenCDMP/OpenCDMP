import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class LocaleInterceptor extends BaseInterceptor {

	constructor(
		private language: TranslateService,
		configurationService: ConfigurationService) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.Locale; }

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (this.language.currentLang) { req = req.clone({ headers: req.headers.set('Accept-Language', this.language.currentLang) }); }
		return next.handle(req);
	}
}
