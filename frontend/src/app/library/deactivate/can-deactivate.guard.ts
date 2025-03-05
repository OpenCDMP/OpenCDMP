import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CanDeactivate } from '@angular/router';
import { CheckDeactivateBaseComponent } from '@app/library/deactivate/deactivate.component';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class CanDeactivateGuard extends BaseComponent implements CanDeactivate<CheckDeactivateBaseComponent> {

	constructor(
		private dialog: MatDialog,
		public language: TranslateService
	) {
		super();
	}

	canDeactivate(component: CheckDeactivateBaseComponent): boolean | Observable<boolean> {

		if (component.canDeactivate()) {
			return true;
		} else {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: 'min(700px, 90vw)',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.LEAVE-PAGE'),
					warning: this.language.instant('GENERAL.CONFIRMATION-DIALOG.LEAVE-WARNING'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.LEAVE'),
				}
			});
			return dialogRef.afterClosed().pipe(map(x => x ? true : false));
		}
	}
}
