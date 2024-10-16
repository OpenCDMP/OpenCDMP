import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CanDeactivate } from '@angular/router';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { from, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class PendingChangesGuard implements CanDeactivate<BasePendingChangesComponent>   {

	constructor(
		private dialog: MatDialog,
		private language: TranslateService
	) {
	}

	canDeactivate(component: BasePendingChangesComponent): boolean | Observable<boolean> {
		if (component.canDeactivate()) {
			return from([true]);
		} else {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					warning: this.language.instant('COMMONS.PENDING-FORM-CHANGES-DIALOG.TITLE'),
					message: this.language.instant('COMMONS.PENDING-FORM-CHANGES-DIALOG.MESSAGE'),
					cancelButton: this.language.instant('COMMONS.PENDING-FORM-CHANGES-DIALOG.ACTIONS.CANCEL'),
					confirmButton: this.language.instant('COMMONS.PENDING-FORM-CHANGES-DIALOG.ACTIONS.LEAVE')
				}
			});
			return dialogRef.afterClosed().pipe(map(x => x ? true : false));
		}
	}
}
