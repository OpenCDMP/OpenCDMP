import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { Observable, of as of } from 'rxjs';
import { AuthGuard } from '@app/core/auth-guard.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class KpiAuthGuard extends AuthGuard {

	constructor(private configurationService: ConfigurationService,authService: AuthService, router: Router) {
		super(authService, router);
	}

	kpiCanActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
		if (this.configurationService.kpiServiceEnabled != true) return of(false);
		return this.canActivate(route, state);
	}

}
