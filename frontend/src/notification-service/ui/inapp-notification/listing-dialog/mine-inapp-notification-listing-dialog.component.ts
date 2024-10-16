import { fromEvent, Observable, Subscription } from "rxjs";
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { InAppNotificationLookup } from '@notification-service/core/query/inapp-notification.lookup';
import { InAppNotificationService } from '@notification-service/services/http/inapp-notification.service';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationInAppTracking } from "@notification-service/core/enum/notification-inapp-tracking.enum";
import { InAppNotification } from "@notification-service/core/model/inapp-notification.model";
import { RouterUtilsService } from "@app/core/services/router/router-utils.service";

@Component({
	selector: 'app-mine-inapp-notification-listing-dialog',
	templateUrl: './mine-inapp-notification-listing-dialog.component.html',
	styleUrls: ['./mine-inapp-notification-listing-dialog.component.scss']
})
export class MineInAppNotificationListingDialogComponent extends BaseComponent implements OnInit, OnDestroy {
	public inappNotifications = new Array<InAppNotification>();
	public notificationInAppTrackingEnum = NotificationInAppTracking;

	resizeObservable: Observable<Event>;
	resizeSubscription: Subscription;
	onReadAll = new EventEmitter();

	constructor(
		public dialogRef: MatDialogRef<MineInAppNotificationListingDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private inappNotificationService: InAppNotificationService,
		private router: Router,
		private routerUtils: RouterUtilsService,
		private uiNotificationService: UiNotificationService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		public authService: AuthService,
	) {
		super();
	}

	ngOnInit() {
		
		const lookup: InAppNotificationLookup = this.getLookup();
		this.fetchNotifications(lookup);

		this.resizeObservable = fromEvent(window, 'resize');
		this.resizeSubscription = this.resizeObservable
			.subscribe(evt =>{
				this.dialogRef.close();
			});

	}

	ngOnDestroy(): void {
		this.resizeSubscription.unsubscribe();
	}

	goToNotification(item: InAppNotification) {
		if (item.trackingState === NotificationInAppTracking.Stored) {
			this.inappNotificationService.read(item.id)
				.pipe(takeUntil(this._destroyed))
				.subscribe(
					data => {
						this.dialogRef.close();
						this.router.navigate([this.routerUtils.generateUrl(['/mine-notifications/dialog', item.id.toString()], '/')]);
					},
					error => {
						this.dialogRef.close();
						this.router.navigate([this.routerUtils.generateUrl(['/mine-notifications/dialog', item.id.toString()], '/')]);
					},
				);
		} else {
			this.dialogRef.close();
			this.router.navigate([this.routerUtils.generateUrl(['/mine-notifications/dialog', item.id.toString()], '/')]);
		}
	}

	goToNotifications() {
		this.router.navigate([this.routerUtils.generateUrl(['/mine-notifications'])]);
		this.dialogRef.close();
	}

	readAllNotifications() {
		this.inappNotificationService.readAll()
		.pipe(takeUntil(this._destroyed))
		.subscribe( //TODO HANDLE-ERRORS
			readAllStatus => {
				if (readAllStatus) {
					const lookup: InAppNotificationLookup = this.getLookup();
					this.fetchNotifications(lookup);
					this.onReadAll.emit();
				} 
			},
		);	
	}

	private getLookup(): InAppNotificationLookup {
		const lookup = new InAppNotificationLookup();
		lookup.project = {
			fields: [
				nameof<InAppNotification>(x => x.id),
				nameof<InAppNotification>(x => x.subject),
				nameof<InAppNotification>(x => x.createdAt),
				nameof<InAppNotification>(x => x.trackingState),
			]
		};
		lookup.page = { offset: 0, size: 5 };
		lookup.order = { items: ['-' + nameof<InAppNotification>(x => x.createdAt)] };
		lookup.isActive = [IsActive.Active];
		return lookup;
	}
	
	private fetchNotifications(lookup: InAppNotificationLookup): void {
		this.inappNotificationService.query(lookup)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => {
					this.inappNotifications = data.items;
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error)
			);
	}
}
