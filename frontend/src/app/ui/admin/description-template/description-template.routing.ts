import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { DescriptionTemplateListingComponent } from './listing/description-template-listing.component';

const routes: Routes = [
	{
		path: '',
		component: DescriptionTemplateListingComponent,
		canActivate: [AuthGuard]
	},
	{
		// lazy, because the editor's module imports the shared structure module, which imports this one's DefinitionEditorModule
		path: '',
		loadChildren: () => import('./editor/description-template-editor.module').then(m => m.DescriptionTemplateEditorModule)
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class DescriptionTemplateRoutingModule { }
