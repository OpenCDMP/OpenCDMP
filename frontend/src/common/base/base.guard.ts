import { OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

export abstract class BaseGuard implements OnDestroy {

	protected _destroyed: Subject<boolean> = new Subject();

	protected constructor() { }

	ngOnDestroy(): void {
		this._destroyed.next(true);
		this._destroyed.complete();
	}
}
