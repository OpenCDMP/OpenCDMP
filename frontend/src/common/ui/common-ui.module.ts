import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormattingModule } from '@app/core/formatting.module';
import { NavigationBreadcrumbComponent } from '@app/ui/misc/breadcrumb/navigation-breadcrumb.component';
import { SecureImagePipe } from '@common/http/image/secure-image.pipe';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
	imports: [
		CommonModule,
		MaterialModule,
		TranslateModule,
		FormattingModule,
		RouterModule
	],
	declarations: [
		SecureImagePipe,
		NavigationBreadcrumbComponent
	],
	exports: [
		CommonModule,
		MaterialModule,
		TranslateModule,
		SecureImagePipe,
		NavigationBreadcrumbComponent
	]
})
export class CommonUiModule { }
