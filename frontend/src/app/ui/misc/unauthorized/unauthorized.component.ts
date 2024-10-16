import { AfterViewInit, Component, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-unauthorized-component',
	templateUrl: './unauthorized.component.html'
})
export class UnauthorizedComponent extends BaseComponent implements AfterViewInit {
	@Input()
	public message: string;
	constructor(
		private authService: AuthService,
		private route: ActivatedRoute,
		private router: Router,
		private routerUtils: RouterUtilsService
	) { super(); }

	ngAfterViewInit() {
		const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
		if (!this.authService.currentAccountIsAuthenticated()) {
			this.router.navigate(['/login'], { queryParams: { returnUrl: returnUrl } });
		} else {
			this.authService.refresh()
				.pipe(takeUntil(this._destroyed))
				.subscribe( //TODO HANDLE-ERRORS
					result => {
						if (!result) { this.router.navigate(['/login'], { queryParams: { returnUrl: returnUrl } }); } else { this.router.navigate([this.routerUtils.generateUrl('/home')]); }
					},
					err => console.error('An error occurred', err));
		}
	}
}
