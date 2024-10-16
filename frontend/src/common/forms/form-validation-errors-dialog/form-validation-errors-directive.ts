import { Directive, ElementRef, OnInit } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
	selector: '[formControl], [formControlName]'
})
export class FormValidationErrorsDirective implements OnInit {
	get control() {
		return this.controlDir.control;
	}

	constructor(
		private controlDir: NgControl,
		private host: ElementRef<HTMLFormElement>) {
	}

	ngOnInit() {
		(this.controlDir.control as any).nativeElement = this.host.nativeElement;
	}
}
