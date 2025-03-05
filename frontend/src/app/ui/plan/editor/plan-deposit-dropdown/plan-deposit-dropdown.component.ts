import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { DepositConfigurationStatus } from '@app/core/common/enum/deposit-configuration-status';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { DepositAuthenticateRequest, DepositRequest, DepositRequestFields} from '@app/core/model/deposit/deposit-request';
import { Plan } from '@app/core/model/plan/plan';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import {
	SnackBarNotificationLevel,
	UiNotificationService
} from '@app/core/services/notification/ui-notification-service';
import { DepositOauth2DialogService } from '@app/ui/misc/deposit-oauth2-dialog/service/deposit-oauth2-dialog.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { MultipleChoiceDialogComponent } from '@common/modules/multiple-choice-dialog/multiple-choice-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-plan-deposit-dropdown',
    templateUrl: './plan-deposit-dropdown.component.html',
    styleUrls: ['./plan-deposit-dropdown.component.scss'],
    standalone: false
})
export class PlanDepositDropdown extends BaseComponent implements OnInit {
	@Input() inputRepos: DepositConfiguration[];
	@Input() plan: Plan;
    @Input() disabled: boolean = false;
	outputRepos = [];
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	@Output() outputReposEmitter: EventEmitter<EntityDoi[]> = new EventEmitter<EntityDoi[]>();
	private oauthLock: boolean;

	constructor(
		private depositRepositoriesService: DepositService,
		private dialog: MatDialog,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private depositOauth2DialogService: DepositOauth2DialogService,
		private sanitizer: DomSanitizer,
		private httpErrorHandlingService: HttpErrorHandlingService,
	) {
		super();
	}

	hasDoi(repo, dois, i) {
		return repo.repositoryId !== dois[i].repositoryId;
	}

	ngOnInit(): void {
		for (var i = 0; i < this.plan?.entityDois?.length; i++) {
			this.inputRepos = this.inputRepos.filter(r => this.hasDoi(r, this.plan.entityDois, i));
		}
		this.inputRepos.forEach(repo => {
			if (repo.hasLogo) {
				this.depositRepositoriesService.getLogo(repo.repositoryId).subscribe(logo => {
					this.logos.set(repo.repositoryId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + logo.body));
				})
			}
		})
	}

	deposit(repo: DepositConfiguration) {

		if (repo.depositType == DepositConfigurationStatus.BothSystemAndUser) {

			const dialogRef = this.dialog.open(MultipleChoiceDialogComponent, {
				maxWidth: '600px',
				restoreFocus: false,
				data: {
					message: this.language.instant('PLAN-OVERVIEW.DEPOSIT.ACCOUNT-LOGIN'),
					titles: [this.language.instant('PLAN-OVERVIEW.DEPOSIT.LOGIN', { 'repository': repo.repositoryId }), this.language.instant('PLAN-OVERVIEW.MULTIPLE-DIALOG.USE-DEFAULT')]
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				switch (result) {
					case 0:
						this.showOauth2Dialog(this.depositOauth2DialogService.getLoginUrl(repo), repo, this.plan);
						break;
					case 1:
						const depositRequest: DepositRequest = {
							repositoryId: repo.repositoryId,
							planId: this.plan.id,
							authorizationCode: null,
							project: this.EntityDoiFields()
						};
						this.depositRepositoriesService.deposit(depositRequest)
							.pipe(takeUntil(this._destroyed))
							.subscribe(doi => {
								this.onDOICallbackSuccess();
								this.outputRepos.push(doi);
								this.outputReposEmitter.emit(this.outputRepos);
							}, error => this.onDOICallbackError(error));
						break;
				}
			});

		} else if (repo.depositType == DepositConfigurationStatus.System) {
			const depositRequest: DepositRequest = {
				repositoryId: repo.repositoryId,
				planId: this.plan.id,
				authorizationCode: null,
				project: this.EntityDoiFields()
			};
			this.depositRepositoriesService.deposit(depositRequest)
				.pipe(takeUntil(this._destroyed))
				.subscribe(doi => {
					this.onDOICallbackSuccess();
					this.outputRepos.push(doi);
					this.outputReposEmitter.emit(this.outputRepos);
				}, error => this.onDOICallbackError(error));
		}
	}

	onDOICallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.SNACK-BAR.SUCCESSFUL-DOI'), SnackBarNotificationLevel.Success);
	}

	onDOICallbackError(error) {
		this.uiNotificationService.snackBarNotification(error.error.message ? error.error.message : this.language.instant('PLAN-EDITOR.SNACK-BAR.UNSUCCESSFUL-DOI'), SnackBarNotificationLevel.Error);
	}

	showOauth2Dialog(url: string, repo: DepositConfiguration, plan: Plan) {
		this.depositOauth2DialogService.login(url)
			.pipe(takeUntil(this._destroyed))
			.subscribe(code => {
				if (code !== undefined) {
					const depositRequest: DepositRequest = {
						repositoryId: repo.repositoryId,
						planId: plan.id,
						authorizationCode: code,
						project: this.EntityDoiFields()
					};
					this.depositRepositoriesService.deposit(depositRequest)
						.pipe(takeUntil(this._destroyed))
						.subscribe(doi => {
							this.onDOICallbackSuccess();
							this.outputRepos.push(doi);
							this.outputReposEmitter.emit(this.outputRepos);
						}, error => this.onDOICallbackError(error));
					this.oauthLock = true;
				} else {
					this.oauthLock = false;
				}
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	private EntityDoiFields(): DepositRequestFields{
		return {
			fields: [
				[nameof<EntityDoi>(x => x.id)].join('.'),
				[nameof<EntityDoi>(x => x.repositoryId)].join('.'),
				[nameof<EntityDoi>(x => x.doi)].join('.'),
				[nameof<EntityDoi>(x => x.entityId)].join('.'),
				[nameof<EntityDoi>(x => x.isActive)].join('.'),
			]
		} as DepositRequestFields;
	}

}
