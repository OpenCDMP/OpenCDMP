import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { BaseFieldSet } from '@app/core/model/description-template/description-template';
import { FormFocusDirective } from '../../../../../../../../common/forms/form-focus.directive';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIconButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-form-field-set-title',
    templateUrl: './field-set-title.component.html',
    styleUrls: ['./field-set-title.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormFocusDirective, MatIcon, MatTooltip, MatIconButton, TranslatePipe]
})
export class FormFieldSetTitleComponent implements OnInit {

	@Input() fieldSet: BaseFieldSet;
	@Input() isChild: boolean = false;
	@Input() path: string;
	@Input() hideTitle: boolean = false;
	@Input() hideLink: boolean = false;
	@Input() isAnchor: boolean = false;
	/** Renders the required asterisk next to the title; the heading is the one place it is shown. */
	@Input() required: boolean = false;
	@Output() copyLinkEvent: EventEmitter<any> = new EventEmitter<any>(); 

	public showExtendedDescription: boolean = false;

	constructor() { }

	ngOnInit() {
	}

	copyLink(): void {
		this.copyLinkEvent.next(true);
	}
}
