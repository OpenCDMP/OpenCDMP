import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from "@app/core/services/router/router-utils.service";
import { UserService } from "@app/core/services/user/user.service";
import { BaseComponent } from '@common/base/base.component';
import { HttpError, HttpErrorHandlingService } from "@common/modules/errors/error-handling/http-error-handling.service";
import { Guid } from "@common/types/guid";
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from "rxjs/operators";
import { MergeEmailLoaderDialogComponent } from "./merge-email-loader-dialog/merge-email-loader-dialog.component";

@Component({
	selector: 'app-email-confirmation-component',
	templateUrl: './merge-email-confirmation.component.html',
	styleUrls: ['./merge-email-confirmation.component.scss']
})
export class MergeEmailConfirmation extends BaseComponent implements OnInit {

	isTokenValid: boolean = false;

	private token: Guid;

	get showForm(): boolean {
		return this.token != null;
	}

	constructor(
		private userService: UserService,
		private route: ActivatedRoute,
		private router: Router,
		private language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private routerUtils: RouterUtilsService,
		private dialog: MatDialog,
		private uiNotificationService: UiNotificationService,
	) { super(); }

	ngOnInit() {
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe(params => {
				const token = params['token']
				if (token != null) {
					this.userService.getUserTokenPermission(token)
						.subscribe(result => {
							this.isTokenValid = result
							this.token = token;
						}, error => {
							this.token = Guid.createEmpty();
							this.onCallbackError(error);
						});
				}
			});
	}

	onConfirm(): void {
		if (this.showForm === false) return;

		let confirmMergeAccountObservable = this.userService.confirmMergeAccount(this.token);

		const dialogRef = this.dialog.open(MergeEmailLoaderDialogComponent, {
			maxWidth: '600px', 
			disableClose: true,
			data: {
				confirmMergeAccountObservable: confirmMergeAccountObservable,
			}
		});

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result.result) {
				this.onCallbackEmailConfirmationSuccess();
			} else {
				if (result.error) this.onCallbackError(result.error);
				else this.onCallbackError();
			}
		});
	}

	onCallbackEmailConfirmationSuccess() {
		this.router.navigate([this.routerUtils.generateUrl('home')])
			.then(() => {
				localStorage.setItem('refreshPage', null);
				localStorage.setItem('refreshPage', 'true');
				window.location.reload();
			});
	}

	onCallbackError(errorResponse?: HttpErrorResponse) {

		if (!errorResponse) {
			this.uiNotificationService.snackBarNotification('GENERAL.SNACK-BAR.GENERIC-ERROR', SnackBarNotificationLevel.Error);
			return;
		}

		const errorOverrides = new Map<number, string>();
		errorOverrides.set(302, this.language.instant('EMAIL-CONFIRMATION.EMAIL-FOUND'));
		errorOverrides.set(403, this.language.instant('EMAIL-CONFIRMATION.EXPIRED-EMAIL'));
		
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error)

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 302) {
			this.router.navigate([this.routerUtils.generateUrl('home')]);
		}
	}
}
