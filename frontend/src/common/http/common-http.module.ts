import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { AuthTokenInterceptor } from './interceptors/auth-token.interceptor';
import { JsonInterceptor } from './interceptors/json.interceptor';
import { LocaleInterceptor } from './interceptors/locale.interceptor';
import { ProgressIndicationInterceptor } from './interceptors/progress-indication.interceptor';
import { RequestTimingInterceptor } from './interceptors/request-timing.interceptor';
import { UnauthorizedResponseInterceptor } from './interceptors/unauthorized-response.interceptor';
import { StatusCodeInterceptor } from './interceptors/status-code.interceptor';
import { TenantHeaderInterceptor } from './interceptors/tenant-header.interceptor';

@NgModule({
	imports: [
	],
	declarations: [
	],
	providers: [
		{
			provide: HTTP_INTERCEPTORS,
			useClass: TenantHeaderInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: AuthTokenInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: JsonInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: LocaleInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: UnauthorizedResponseInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: RequestTimingInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: ProgressIndicationInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: StatusCodeInterceptor,
			multi: true,
		}
	]
})
export class CommonHttpModule { }
