import { GuidedTourService } from './guided-tour.service';
import { GuidedTourComponent } from './guided-tour.component';
import { NgModule, ErrorHandler, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WindowRefService } from './windowref.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
    declarations: [GuidedTourComponent],
    imports: [CommonModule, TranslateModule, MatButtonModule],
    providers: [WindowRefService],
    exports: [GuidedTourComponent]
})
export class GuidedTourModule {
  public static forRoot(): ModuleWithProviders<GuidedTourModule> {
    return {
      ngModule: GuidedTourModule,
      providers: [ErrorHandler, GuidedTourService],
    };
  }
}
