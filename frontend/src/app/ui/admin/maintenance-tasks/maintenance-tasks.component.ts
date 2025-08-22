import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { MaintenanceService } from '@app/core/services/maintenance/maintenance.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-maintenance-tasks',
    templateUrl: './maintenance-tasks.component.html',
    styleUrls: ['./maintenance-tasks.component.scss'],
    standalone: false
})
export class MaintenanceTasksComponent extends BaseComponent implements OnInit {

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		private maintenanceService: MaintenanceService,
		private uiNotificationService: UiNotificationService,
		private translate: TranslateService,
		private router: Router,
		private routerUtils: RouterUtilsService,
		public configurationService: ConfigurationService
	) {
		super();
	}

	ngOnInit(): void {
	}

	generateIndex(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doGenerateIndex(ev);
				}
			});
	}

	private doGenerateIndex(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.generateElasticIndex().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	clearIndex(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doClearIndex(ev);
				}
			});
	}

	private doClearIndex(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.clearElasticIndex().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendUserTouchEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendUserTouchEvents(ev);
				}
			});
	}

	private doSendUserTouchEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendUserTouchEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendTenantTouchEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendTenantTouchEvents(ev);
				}
			});
	}

	private doSendTenantTouchEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendTenantTouchEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendPlanTouchEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendPlanTouchEvents(ev);
				}
			});
	}

	private doSendPlanTouchEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendPlanTouchEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendDescriptionTouchEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendDescriptionTouchEvents(ev);
				}
			});
	}

	private doSendDescriptionTouchEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendDescriptionTouchEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendPlanAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendPlanAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendPlanAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendPlanAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendDescriptionAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendDescriptionAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendDescriptionAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendDescriptionAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendBlueprintAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendBlueprintAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendBlueprintAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendBlueprintAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendDescriptionTemplateAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendDescriptionTemplateAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendDescriptionTemplateAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendDescriptionTemplateAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendDescriptionTemplateTypeAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendDescriptionTemplateTypeAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendDescriptionTemplateTypeAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendDescriptionTemplateTypeAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendPrefillingSourceAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendPrefillingSourceAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendPrefillingSourceAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendPrefillingSourceAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendReferenceTypeAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendReferenceTypeAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendReferenceTypeAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendReferenceTypeAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendPlanStatusAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendPlanStatusAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendPlanStatusAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendPlanStatusAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendDescriptionStatusAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendDescriptionStatusAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendDescriptionStatusAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendDescriptionStatusAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendUserAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendUserAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendUserAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendUserAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendReferenceAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendReferenceAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendReferenceAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendReferenceAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendLanguageAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendLanguageAccountingEntriesEvents(ev);
				}
			});
	}

	private doSendLanguageAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendLanguageAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorCreateEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorCreateEvents(ev);
				}
			});
	}

	private doSendIndicatorCreateEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorCreateEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorResetEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorResetEvents(ev);
				}
			});
	}

	private doSendIndicatorResetEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorResetEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorAccessEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorAccessEvents(ev);
				}
			});
	}

	

	private doSendIndicatorAccessEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorAccessEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorPlanPointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorPlanPointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorPlanPointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorPlanPointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorDescriptionPointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorDescriptionPointEvents(ev);
				}
			});
	}

	private doSendIndicatorDescriptionPointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorDescriptionPointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorReferencePointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorReferencePointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorReferencePointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorReferencePointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorUserPointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorUserPointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorUserPointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorUserPointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}
	
	sendIndicatorPlanBlueprintPointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorPlanBlueprintPointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorPlanBlueprintPointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorPlanBlueprintPointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorDescriptionTemplatePointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorDescriptionTemplatePointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorDescriptionTemplatePointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorDescriptionTemplatePointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendIndicatorTenantPointEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSendIndicatorTenantPointEvents(ev);
				}
			});
	}

	

	private doSendIndicatorTenantPointEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendIndicatorTenantPointEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}
	







	
	sendEvaluationPlanAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.dosendEvaluationPlanAccountingEntriesEvents(ev);
				}
			});
	}

	private dosendEvaluationPlanAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendEvaluationPlanAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	sendEvaluationDescriptionAccountingEntriesEvents(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.dosendEvaluationDescriptionAccountingEntriesEvents(ev);
				}
			});
	}

	private dosendEvaluationDescriptionAccountingEntriesEvents(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.sendEvaluationDescriptionAccountingEntriesEvents().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	setPlanBlueprintCanEditDescriptionTemplates(ev: Event) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('MAINTENANCE-TASKS.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {
					this.doSetPlanBlueprintCanEditDescriptionTemplates(ev);
				}
			});
	}

	private doSetPlanBlueprintCanEditDescriptionTemplates(ev: Event) {
		(ev.target as HTMLButtonElement).disabled = true;
		this.maintenanceService.setPlanBlueprintCanEditDescriptionTemplates().pipe(takeUntil(this._destroyed)).subscribe(
			_ => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackSuccess();
			},
			error => {
				(ev.target as HTMLButtonElement).disabled = false;
				this.onCallbackError(error);
			}
		);
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.translate.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.router.navigate(['/reload']).then(() => this.router.navigate([this.routerUtils.generateUrl('/maintenance-tasks')]));
	}

	onCallbackError(error: any) {
		this.uiNotificationService.snackBarNotification(error, SnackBarNotificationLevel.Error);
	}
}
