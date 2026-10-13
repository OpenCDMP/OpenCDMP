import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { StructureEditorModule } from '../../structure-editor/structure-editor.module';
import { Bp2VersionHistoryComponent } from '../../structure-editor/version-history/version-history.component';
import { VersionHistorySource } from '../../structure-editor/version-history/version-history-source';
import { DefinitionEditorModule } from '../description-template.module';
import { DescriptionTemplateEditorComponent } from './description-template-editor.component';
import { DescriptionTemplateEditorResolver } from './description-template-editor.resolver';
import { EditorsPanelComponent } from './editors-panel/editors-panel.component';
import { Dt2DetailsTabComponent } from './tabs/details-tab/details-tab.component';
import { Dt2ReviewTabComponent } from './tabs/review-tab/review-tab.component';
import { TemplateVersionSource } from './template-version-source';

const editPermission = { authContext: { permissions: [AppPermission.EditDescriptionTemplate] } };

/** Mounted under `/asset-templates`, after the listing. */
const routes: Routes = [
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: { ...editPermission, ...BreadcrumbService.generateRouteDataConfiguration({ title: 'BREADCRUMBS.NEW-DESCRIPTION-TEMPLATES' }) }
	},
	{
		path: 'clone/:cloneid',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': DescriptionTemplateEditorResolver },
		data: { ...editPermission, ...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true }), action: 'clone' }
	},
	{
		path: 'new-version/:newversionid',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': DescriptionTemplateEditorResolver },
		data: { ...editPermission, action: 'new-version' }
	},
	{
		path: 'versions/:groupid',
		canActivate: [AuthGuard],
		component: Bp2VersionHistoryComponent,
		providers: [{ provide: VersionHistorySource, useClass: TemplateVersionSource }],
		data: { ...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true }) }
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': DescriptionTemplateEditorResolver }
	}
];

/** Its own routing module, imported first: the routes of modules imported further down (the description form brings a catch-all) must come after these. */
@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [DescriptionTemplateEditorResolver]
})
export class DescriptionTemplateEditorRoutingModule { }

/**
 * The description template editor. Loaded lazily from the template routes, so this module can import the shared
 * structure module (which itself needs `DefinitionEditorModule`) without a static import cycle.
 */
@NgModule({
    imports: [
        DescriptionTemplateEditorRoutingModule,
        CommonUiModule,
        StructureEditorModule,
        DefinitionEditorModule,
        DescriptionTemplateEditorComponent,
        Dt2DetailsTabComponent,
        Dt2ReviewTabComponent,
        EditorsPanelComponent
    ]
})
export class DescriptionTemplateEditorModule { }
