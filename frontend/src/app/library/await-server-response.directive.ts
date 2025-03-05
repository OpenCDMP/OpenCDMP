import { Directive, HostListener, Input } from '@angular/core';
import { exhaustMap, Subject, Observable } from 'rxjs';

@Directive({
  selector: '[appAwaitServerResponse]',
  standalone: true
})
export class AwaitServerResponseDirective {
    @Input() clickFn: (params?: any) => Observable<any>
    @Input() params: any
    @HostListener('click', ['$event']) onClick($event){
        this.awaitResponse$.next(true);
    }
    awaitResponse$: Subject<boolean> = new Subject();
    constructor() {
        this.awaitResponse$.pipe(
            exhaustMap(() => this.clickFn(this.params))
        ).subscribe(res => {});
    }
}
