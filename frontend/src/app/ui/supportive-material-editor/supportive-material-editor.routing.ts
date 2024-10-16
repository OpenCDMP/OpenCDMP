import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SupportiveMaterialEditorComponent } from './supportive-material-editor.component';
import { AuthGuard } from '@app/core/auth-guard.service';


const routes: Routes = [
	{ path: '', component: SupportiveMaterialEditorComponent, canActivate: [AuthGuard] },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SupportiveMaterialEditorRoutingModule { }
