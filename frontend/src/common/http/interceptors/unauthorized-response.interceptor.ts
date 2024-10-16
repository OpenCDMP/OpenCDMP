import { HttpErrorResponse, HttpHandler, HttpHeaderResponse, HttpProgressEvent, HttpRequest, HttpResponse, HttpSentEvent, HttpUserEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, from, throwError } from 'rxjs';
import { catchError, filter, mergeMap, tap } from 'rxjs/operators';
import { AuthService } from '../../../app/core/services/auth/auth.service';
import { BaseInterceptor } from './base.interceptor';
import { InterceptorType } from './interceptor-type';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class UnauthorizedResponseInterceptor extends BaseInterceptor {

	constructor(
		public router: Router,
		private authService: AuthService,
		configurationService: ConfigurationService
	) { super(configurationService); }

	get type(): InterceptorType { return InterceptorType.UnauthorizedResponse; }

	private accountRefresh$: Observable<boolean> = null;

	interceptRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpSentEvent | HttpHeaderResponse | HttpProgressEvent | HttpResponse<any> | HttpUserEvent<any>> {
		return next.handle(req).pipe(
			catchError(error => {
				if (error instanceof HttpErrorResponse) {
					switch ((<HttpErrorResponse>error).status) {
						case 401:
							return this.handle401Error(req, next);
						default:
							return throwError(error);
					}
				} else {
					return throwError(error);
				}
			}));
	}

	private handle401Error(req: HttpRequest<any>, next: HttpHandler) {
		if (!this.accountRefresh$) {
			this.accountRefresh$ = from(
				this.authService.refreshToken().then((isRefreshed) => {
					this.accountRefresh$ = null;
					if (!isRefreshed) {
						this.handleUnauthorized();
						return false;
					}

					return true;
				}).catch(x => {
					this.handleUnauthorized();
					return false;
				})
			).pipe(filter((x) => x));
		}
		return this.accountRefresh$.pipe(mergeMap(account => this.repeatRequest(req, next)));
	}

	private repeatRequest(originalRequest: HttpRequest<any>, next: HttpHandler) {
		const newAuthenticationToken: String = this.authService.currentAuthenticationToken();
		const newRequest = originalRequest.clone({
			setHeaders: {
				Authorization: `Bearer ${newAuthenticationToken}`
			}
		});
		return next.handle(newRequest);
	}

	private handleUnauthorized() {
		if (!this.isLoginRoute() && !this.isSignupRoute()) {
			this.authService.clear();
			this.router.navigate(['/unauthorized', { queryParams: { returnUrl: this.router.url } }]);
		}
	}

	private isLoginRoute(): boolean {
		return this.router.isActive('login', false);
	}

	private isSignupRoute(): boolean {
		return this.router.isActive('signup-register', false) || this.router.isActive('signup-invitation', false);
	}
}
