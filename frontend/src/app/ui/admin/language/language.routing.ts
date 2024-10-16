import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LanguageEditorComponent } from './editor/language-editor.component';
import { LanguageListingComponent } from './listing/language-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { LanguageEditorResolver } from './editor/language-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: LanguageListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: LanguageEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditLanguage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-LANGUAGE'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: LanguageEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': LanguageEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditLanguage]
			},
			getFromTitleService: true,
			usePrefix: false
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [LanguageEditorResolver]
})
export class LanguageRoutingModule { }
