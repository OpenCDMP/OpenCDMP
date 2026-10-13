import { GuidedTourService } from './guided-tour.service';
import { GuidedTourComponent } from './guided-tour.component';
import { NgModule, ErrorHandler, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WindowRefService } from './windowref.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { A11yModule } from '@angular/cdk/a11y';

@NgModule({
    imports: [CommonModule, TranslateModule, MatButtonModule, MatIconModule, A11yModule, GuidedTourComponent],
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
