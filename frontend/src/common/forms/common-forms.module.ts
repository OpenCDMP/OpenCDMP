import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { FormService } from './form-service';
import { ReactiveAsteriskDirective } from './reactive-asterisk-directive';
import { TextareaAutoresizeDirective } from './textarea-autoresize-directive';

@NgModule({
	imports: [
		FormsModule,
		ReactiveFormsModule,
	],
	declarations:[
		ReactiveAsteriskDirective,
		TextareaAutoresizeDirective
	],
	exports: [
		FormsModule,
		ReactiveFormsModule,
		ReactiveAsteriskDirective,
		TextareaAutoresizeDirective
	],
	providers: [
		FormService,
		PendingChangesGuard,
		ReactiveAsteriskDirective,
		TextareaAutoresizeDirective
	]
})
export class CommonFormsModule { }
