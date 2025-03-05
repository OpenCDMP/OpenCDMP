import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DescriptionTemplateFieldSet } from '@app/core/model/description-template/description-template';

@Component({
    selector: 'app-description-form-field-set-title',
    templateUrl: './field-set-title.component.html',
    styleUrls: ['./field-set-title.component.scss'],
    standalone: false
})
export class DescriptionFormFieldSetTitleComponent implements OnInit {

	@Input() fieldSet: DescriptionTemplateFieldSet;
	@Input() isChild: Boolean = false;
	@Input() path: string;
	@Input() hideTitle: Boolean = false;
	@Input() hideLink: Boolean = false;
	@Input() isAnchor: Boolean = false;
	@Output() copyLinkEvent: EventEmitter<any> = new EventEmitter<any>(); 

	public showExtendedDescription: boolean = false;

	constructor() { }

	ngOnInit() {
	}

	copyLink(): void {
		this.copyLinkEvent.next(true);
	}
}
