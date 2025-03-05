import { Directive, effect, ElementRef, HostListener, input, Input } from '@angular/core';

@Directive({
  selector: '[appFor]',
  standalone: true
})
export class FormFocusDirective {
    appFor = input<string>();
    el: HTMLElement;
    constructor(elRef: ElementRef) {
        this.el = elRef.nativeElement;
        effect(() => this.el.setAttribute('for', this.appFor()))
    }
    @HostListener('click') onLabelClick(){
        if(this.appFor) {
            document.getElementById(this.appFor())?.focus();
            (document.getElementById(this.appFor())?.querySelector(".angular-editor-textarea") as HTMLElement)?.focus(); //for rich-text-editor 
            document.getElementById(`${this.appFor}-input`)?.focus(); //for any mat component that nests the <input> 
        }
    }
}
