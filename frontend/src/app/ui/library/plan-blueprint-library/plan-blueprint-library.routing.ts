import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { PlanBlueprintLibraryListingComponent } from './listing/plan-blueprint-library-listing.component';
import { PlanBlueprintLibraryViewComponent } from './view/plan-blueprint-library-view.component';

const routes: Routes = [
    {
        path: '',
        component: PlanBlueprintLibraryListingComponent,
        canActivate: [AuthGuard]
    },
    {
        path: ':id',
        component: PlanBlueprintLibraryViewComponent,
        canActivate: [AuthGuard]
    },
    { path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent) },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class PlanBlueprintLibraryRoutingModule { }