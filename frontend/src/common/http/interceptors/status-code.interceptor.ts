
import {tap} from 'rxjs/operators';
import { Injectable } from "@angular/core";
import { BaseInterceptor } from "./base.interceptor";
import { InterceptorType } from "./interceptor-type";
import { HttpHandler, HttpRequest, HttpEvent } from "@angular/common/http";
import { Observable } from "rxjs";
import { Router } from "@angular/router";
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { AuthService } from '@app/core/services/auth/auth.service';

@Injectable()
export class StatusCodeInterceptor extends BaseInterceptor {

	type: InterceptorType;
	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent <any>> {
		return next.handle(req).pipe(tap(event => { }, err => {
		}));
	}

	constructor(
		private router: Router,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
		configurationService: ConfigurationService
	) { super(configurationService); }
}
