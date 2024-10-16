import { Directive, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

@Directive()
export abstract class BasePipe implements OnDestroy {

	protected _destroyed: Subject<boolean> = new Subject();

	protected constructor() { }

	ngOnDestroy(): void {
		this._destroyed.next(true);
		this._destroyed.complete();
	}
}
