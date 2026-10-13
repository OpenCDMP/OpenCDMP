import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { DescriptionTemplateLibraryListingComponent } from './listing/description-template-library-listing.component';
import { DescriptionTemplateLibraryViewComponent } from './view/description-template-library-view.component';

const routes: Routes = [
    {
        path: '',
        component: DescriptionTemplateLibraryListingComponent,
        canActivate: [AuthGuard]
    },
    {
        path: ':id',
        component: DescriptionTemplateLibraryViewComponent,
        canActivate: [AuthGuard]
    },
    { path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent) },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class DescriptionTemplateLibraryRoutingModule { }