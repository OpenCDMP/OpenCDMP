import { NgModule } from '@angular/core';

import { MaintenanceTasksRoutingModule } from './maintenance-tasks.routing';
import { MaintenanceTasksComponent } from './maintenance-tasks.component';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';


@NgModule({
  declarations: [MaintenanceTasksComponent],
  imports: [
    CommonUiModule,
	  CommonFormsModule,
	  ConfirmationDialogModule,
    MaintenanceTasksRoutingModule
  ],
  providers: [
  ]
})
export class MaintenanceTasksModule { }