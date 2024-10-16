import { Directive, HostListener } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { Observable } from 'rxjs';

@Directive()
export abstract class BasePendingChangesComponent extends BaseComponent {

	// we may overwrite these
	public DEACTIVATE_GUARD_MESSAGE: string = 'COMMONS.PENDING-FORM-CHANGES-DIALOG.MESSAGE';
	public DEACTIVATE_GUARD_CANCEL_BUTTON: string = 'COMMONS.PENDING-FORM-CHANGES-DIALOG.ACTIONS.NO';
	public DEACTIVATE_GUARD_CONFIRM_BUTTON: string = 'COMMONS.PENDING-FORM-CHANGES-DIALOG.ACTIONS.YES';
	public DEACTIVATE_GUARD_TITLE: string = undefined;

	protected constructor() { super(); }

	abstract canDeactivate(): boolean | Observable<boolean>;

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any) {
		if (!this.canDeactivate()) {
			$event.returnValue = true;
		}
	}
}