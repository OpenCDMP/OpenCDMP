import { Injectable, OnDestroy } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { BaseEditor } from '@common/base/base-editor';
import { Subject } from 'rxjs';

@Injectable()
export abstract class BaseEditorResolver implements OnDestroy {

	protected _destroyed = new Subject<boolean>();
	ngOnDestroy(): void {
		this._destroyed.next(true);
		this._destroyed.complete();
	}

	public static lookupFields(): string[] {
		return [...BaseEditor.commonFormFieldNames()];
	}
	abstract resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot);
}
