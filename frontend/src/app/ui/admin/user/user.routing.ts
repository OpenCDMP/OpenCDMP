import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "@app/core/auth-guard.service";
import { UserListingComponent } from "./listing/user-listing.component";
import { AppPermission } from "@app/core/common/enum/permission.enum";

const routes: Routes = [
	{
		path: '',
		component: UserListingComponent,
		canActivate: [AuthGuard],
		data: {
			authContext: {
				permissions: [AppPermission.ViewUserPage]
			}
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
]

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
})
export class UsersRoutingModule { }

const tenantUserRoutes: Routes = [
	{
		path: '',
		component: UserListingComponent,
		canActivate: [AuthGuard],
		data: {
			authContext: {
				permissions: [AppPermission.ViewTenantUserPage]
			},
			tenantAdminMode: true
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
]

@NgModule({
	imports: [RouterModule.forChild(tenantUserRoutes)],
	exports: [RouterModule],
})
export class TenantUsersRoutingModule { }
