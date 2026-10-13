import { Component, Inject, OnDestroy, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth/auth.service';
import { Observable, Subscription, fromEvent } from 'rxjs';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { MatActionList, MatListItem, MatDivider } from '@angular/material/list';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-user-dialog-component',
    templateUrl: 'user-dialog.component.html',
    styleUrls: ['user-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatActionList, MatListItem, RouterLink, MatIcon, MatDivider, TranslatePipe]
})
export class UserDialogComponent implements OnInit, OnDestroy  {
	public formGroup: UntypedFormGroup;

	resizeObservable: Observable<Event>;
	resizeSubscription: Subscription;

	constructor(
		private authentication: AuthService,
		private router: Router,
		public dialogRef: MatDialogRef<UserDialogComponent>,
		protected routerUtils: RouterUtilsService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) { }

	ngOnInit(): void {
		this.resizeObservable = fromEvent(window, 'resize');
		this.resizeSubscription = this.resizeObservable
			.subscribe(evt =>{
				this.dialogRef.close();
			});
	}

	ngOnDestroy(): void {
		this.resizeSubscription.unsubscribe();
	}

	public logout(): void {
		this.dialogRef.close();
		this.router.navigate(['/logout']);
	}

	public getPrincipalName(): string {
		return this.authentication.getPrincipalName() ?? '';
	}

	public getPrincipalEmail(): string {
		return this.authentication.getUserProfileEmail() ?? '';
	}

	public principalHasAvatar(): boolean {
		return this.authentication.getUserProfileAvatarUrl() && this.authentication.getUserProfileAvatarUrl().length > 0;
	}
}
