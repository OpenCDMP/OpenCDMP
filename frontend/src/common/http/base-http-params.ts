import { HttpParams } from '@angular/common/http';
import { InterceptorContext } from './interceptor-context';

export class BaseHttpParams extends HttpParams {
	interceptorContext?: InterceptorContext;
}
