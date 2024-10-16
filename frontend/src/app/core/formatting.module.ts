import { DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { ColumnClassPipe } from "@app/core/pipes/column-class.pipe";
import { FieldValuePipe } from "@app/core/pipes/field-value.pipe";
import { PipeService } from '@common/formatting/pipe.service';
import { AppTitleCaseFormatPipe } from '@common/formatting/pipes/app-title-case-format.pipe';
import { CultureInfoDisplayPipe } from './pipes/culture-info-display.pipe';
import { DataTableDateTimeFormatPipe, DateTimeFormatPipe } from './pipes/date-time-format.pipe';
import { JsonParserPipe } from './pipes/json-parser.pipe';
import { NgForLimitPipe } from './pipes/ng-for-limit.pipe';
import { RemoveHtmlTagsPipe } from './pipes/remove-html-tags.pipe';
import { SumarizeTextPipe } from './pipes/sumarize-text.pipe';
import { TimezoneInfoDisplayPipe } from './pipes/timezone-info-display.pipe';
import { EnumUtils } from './services/utilities/enum-utils.service';

//
//
// This is shared module that provides all formatting utils. Its imported only once on the AppModule.
//
//

@NgModule({
	declarations: [
		AppTitleCaseFormatPipe,
		NgForLimitPipe,
		SumarizeTextPipe,
		RemoveHtmlTagsPipe,
		TimezoneInfoDisplayPipe,
		CultureInfoDisplayPipe,
		DateTimeFormatPipe,
		DataTableDateTimeFormatPipe,
		JsonParserPipe,
		FieldValuePipe,
		ColumnClassPipe,
	],
	exports: [
		AppTitleCaseFormatPipe,
		NgForLimitPipe,
		SumarizeTextPipe,
		RemoveHtmlTagsPipe,
		TimezoneInfoDisplayPipe,
		CultureInfoDisplayPipe,
		DateTimeFormatPipe,
		DataTableDateTimeFormatPipe,
		JsonParserPipe,
		FieldValuePipe,
		ColumnClassPipe,
	],
	providers: [
		EnumUtils,
		DatePipe,
		PipeService,
		AppTitleCaseFormatPipe,
		NgForLimitPipe,
		SumarizeTextPipe,
		RemoveHtmlTagsPipe,
		TimezoneInfoDisplayPipe,
		CultureInfoDisplayPipe,
		DateTimeFormatPipe,
		DataTableDateTimeFormatPipe,
		JsonParserPipe,
		FieldValuePipe,
		ColumnClassPipe,
	]
})
export class FormattingModule { }
