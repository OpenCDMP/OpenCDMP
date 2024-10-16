import { HostListener, Directive } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';

@Directive()
export abstract class CheckDeactivateBaseComponent extends BaseComponent {

	protected constructor() { super(); }

	abstract canDeactivate(): boolean;

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any) {
		if (!this.canDeactivate()) {
			$event.returnValue = true;
		}
	}
}
