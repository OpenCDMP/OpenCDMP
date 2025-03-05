import { HttpClient } from '@angular/common/http';
import { Component, computed, HostBinding, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { PlanUploadDialogComponent } from '../upload-dialogue/plan-upload-dialog.component';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';

@Component({
    selector: 'app-start-new-plan',
    templateUrl: './start-new-plan-dialog.component.html',
    styleUrls: ['./start-new-plan-dialog.component.scss'],
    standalone: false
})
export class StartNewPlanDialogComponent extends BaseComponent {
   

	public isDialog: boolean = false;

	constructor(
		public dialogRef: MatDialogRef<StartNewPlanDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private router: Router,
		public dialog: MatDialog,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private planService: PlanService,
		private httpClient: HttpClient,
		private analyticsService: AnalyticsService,
		private routerUtils: RouterUtilsService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
		this.isDialog = data.isDialog;
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.StartNewPlanDialog);
	}

	cancel() {
		this.dialogRef.close();
	}

	send() {
		this.dialogRef.close(this.data);
	}

	close() {
		this.dialogRef.close(false);
	}

	startWizard() {
		this.router.navigate([this.routerUtils.generateUrl('/plans/new')]);
		this.close();
	}

	uploadFile(event) {
		const dialogRef = this.dialog.open(PlanUploadDialogComponent, {
			maxWidth: '528px',
			data: {
				fileList: FileList,
				success: Boolean,
				planTitle: String
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result && result.success) {
				const file = result.fileList[0] as File;
				if (file?.type.includes('/xml')){
					this.planService.uploadXml(result.fileList[0], result.planTitle)
						.pipe(takeUntil(this._destroyed))
						.subscribe(
							(complete) => {
								this.onCallbackImportComplete();
								this.dialog.closeAll();
							},
							(error) => this.httpErrorHandlingService.handleBackedRequestError(error)
						);
				} else if (file?.type.includes('/json') && result.planCommonModelConfig){
					this.planService.uploadJson(result.planCommonModelConfig)	
						.pipe(takeUntil(this._destroyed))
						.subscribe(
							(complete) => {
								this.onCallbackImportComplete();
								this.dialog.closeAll();
							},
							(error) => this.onCallbackImportFail(error.error)
						);
				}
			}
		});
	}

	private onCallbackImportComplete() {
		this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-UPLOAD.UPLOAD-SUCCESS'), SnackBarNotificationLevel.Success);
		this.router.navigate(['/reload']).then(() => this.router.navigate([this.routerUtils.generateUrl('/plans')]));
	}

	private onCallbackImportFail(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.error), SnackBarNotificationLevel.Error);
	}

}
