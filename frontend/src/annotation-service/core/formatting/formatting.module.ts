

//
//
// This is shared module that provides all notification service formatting utils. Its imported only once.
//

import { CommonFormattingModule } from "@common/formatting/common-formatting.module";
import { IsActiveTypePipe } from "./pipes/is-active-type.pipe";
import { InternalStatusTypePipe } from "./pipes/internal-status-type.pipe";
import { PipeService } from "@common/formatting/pipe.service";
import { AnnotationServiceEnumUtils } from "./enum-utils.service";
import { NgModule } from "@angular/core";

//
@NgModule({
	imports: [
		CommonFormattingModule,
	],
	declarations: [
		IsActiveTypePipe,
		InternalStatusTypePipe
	],
	exports: [
		CommonFormattingModule,
		IsActiveTypePipe,
		InternalStatusTypePipe,
	],
	providers: [
		PipeService,
		IsActiveTypePipe,
		InternalStatusTypePipe,
		AnnotationServiceEnumUtils
	]
})
export class AnnotationServiceFormattingModule { }
