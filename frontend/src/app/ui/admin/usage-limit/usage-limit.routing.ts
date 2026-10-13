import { inject, NgModule } from '@angular/core';
import { ActivatedRouteSnapshot, RouterModule, RouterStateSnapshot, Routes } from '@angular/router';
import { AccountingAuthGuard } from '@app/core/services/accounting-auth-guard.service';
import { QuotasComponent } from './quotas/quotas.component';

const routes: Routes = [
	{
		path: '',
		component: QuotasComponent,
		canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(AccountingAuthGuard).accountingCanActivate(next, state)]
	},
	{ path: 'new', redirectTo: '', pathMatch: 'full' },
	{ path: ':id', redirectTo: '', pathMatch: 'full' },
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class UsageLimitRoutingModule { }
