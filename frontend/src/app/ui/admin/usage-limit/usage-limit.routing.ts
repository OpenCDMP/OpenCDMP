import { inject, NgModule } from '@angular/core';
import { ActivatedRouteSnapshot, RouterModule, RouterStateSnapshot, Routes } from '@angular/router';
import { UsageLimitEditorComponent } from './editor/usage-limit-editor.component';
import { UsageLimitListingComponent } from './listing/usage-limit-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { UsageLimitEditorResolver } from './editor/usage-limit-editor.resolver';
import { AccountingAuthGuard } from '@app/core/services/accounting-auth-guard.service';

const routes: Routes = [
	{
		path: '',
		component: UsageLimitListingComponent,
		canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(AccountingAuthGuard).accountingCanActivate(next, state)]
	},
	{
		path: 'new',
		canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(AccountingAuthGuard).accountingCanActivate(next, state)],
		component: UsageLimitEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditUsageLimit]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-USAGE-LIMIT'
			})
		}
	},
	{
		path: ':id',
		canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(AccountingAuthGuard).accountingCanActivate(next, state)],
		component: UsageLimitEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': UsageLimitEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditUsageLimit]
			}
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [UsageLimitEditorResolver]
})
export class UsageLimitRoutingModule { }
