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
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DepositAuthMethod } from '@app/core/common/enum/deposit-auth-method';
import { EnqueueService } from '@app/core/services/enqueue.service';

@Component({
    selector: 'app-plan-deposit-dropdown',
    templateUrl: './plan-deposit-dropdown.component.html',
    styleUrls: ['./plan-deposit-dropdown.component.scss'],
    standalone: false,
    providers: [EnqueueService]
})
export class PlanDepositDropdown extends BaseComponent implements OnInit {
	@Input() inputRepos: DepositConfiguration[];
	@Input() plan: Plan;
    @Input() disabled: boolean = false;
	@Input() outputRepos: EntityDoi[]= [];
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	@Output() outputReposEmitter: EventEmitter<EntityDoi[]> = new EventEmitter<EntityDoi[]>();
	private oauthLock: boolean;
    private isLoading = this.enqueueService.exhaustPipelineBusy;
	constructor(
		private depositService: DepositService,
		private dialog: MatDialog,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private depositOauth2DialogService: DepositOauth2DialogService,
		private sanitizer: DomSanitizer,
		private httpErrorHandlingService: HttpErrorHandlingService,
        private enqueueService: EnqueueService
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
				this.depositService.getLogo(repo.repositoryId).subscribe(logo => {
					this.logos.set(repo.repositoryId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + logo.body));
				})
			}
		})
	}

	deposit(repo: DepositConfiguration) {

		if (repo.depositType == DepositConfigurationStatus.BothSystemAndUser) {
            this.enqueueService.enqueueExhaustChannel(
                this.depositService.getAvailableAuthMethods(repo.repositoryId).pipe(tap((authMetodResult) => {
    
                    if (authMetodResult && authMetodResult.depositAuthInfoTypes?.length) {
                        const methods = authMetodResult.depositAuthInfoTypes;
                    
                        if (methods.length === 1) {
                            this.applyDepositMethod(methods[0], repo)
                            return; 
                        }
                    
                        let titles: any[] = [];
                        methods.forEach(x => {
                            if (x === DepositAuthMethod.oAuth2Flow) {
                                titles.push({
                                    title: this.language.instant('PLAN-OVERVIEW.DEPOSIT.LOGIN', { 'repository': repo.repositoryId }),
                                });
                            } else if (x === DepositAuthMethod.AuthInfoFromUserProfile) {
                                titles.push({
                                    title: this.language.instant('PLAN-OVERVIEW.MULTIPLE-DIALOG.USE-YOURS'),
                                    buttonDisabled: !authMetodResult.hasMyAccountValue,
                                    matTooltipDisabledMessage: this.language.instant('PLAN-OVERVIEW.MULTIPLE-DIALOG.TOOLTIP-MESSAGE', { 'repository': repo.repositoryId })
                                });
                            } else if (x === DepositAuthMethod.PluginDefault) {
                                titles.push({
                                    title: this.language.instant('PLAN-OVERVIEW.MULTIPLE-DIALOG.USE-DEFAULT'),
                                });
                            }
                        });
    
                        const dialogRef = this.dialog.open(MultipleChoiceDialogComponent, {
                            maxWidth: '600px',
                            restoreFocus: false,
                            data: {
                                message: this.language.instant('PLAN-OVERVIEW.DEPOSIT.ACCOUNT-LOGIN'),
                                titles: titles
                            }
                        });
                        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
                            const selectedMethod = methods[result];
                            this.applyDepositMethod(selectedMethod, repo)
                        });
                    } else {
                        // No available auth methods
                        this.dialog.open(MultipleChoiceDialogComponent, {
                            maxWidth: '600px',
                            restoreFocus: false,
                            data: {
                                message: this.language.instant('PLAN-OVERVIEW.DEPOSIT.ACCOUNT-LOGIN'),
                                errorMessage: this.language.instant('PLAN-OVERVIEW.MULTIPLE-DIALOG.CONTACT-SUPPORT', { 'repository': repo.repositoryId })
                            }
                        });
                    }
                }))
            )
		}
			
	}

	applyDepositMethod( selectedMethod: DepositAuthMethod, repo: DepositConfiguration) {

		const depositRequest: DepositRequest = {
			repositoryId: repo.repositoryId,
			planId: this.plan.id,
			authorizationCode: null,
			depositAuthInfoType: selectedMethod,
			project: this.EntityDoiFields()
		};

		switch (selectedMethod) {
			case DepositAuthMethod.oAuth2Flow:
				this.showOauth2Dialog(this.depositOauth2DialogService.getLoginUrl(repo), repo, this.plan);
				break;
			case DepositAuthMethod.AuthInfoFromUserProfile:
				this.depositService.deposit(depositRequest)
					.pipe(takeUntil(this._destroyed))
					.subscribe(doi => {
						this.onDOICallbackSuccess();
						this.outputRepos.push(doi);
						this.outputReposEmitter.emit(this.outputRepos);
					}, error => this.onDOICallbackError(error));
				break;
			case DepositAuthMethod.PluginDefault:
				this.depositService.deposit(depositRequest)
					.pipe(takeUntil(this._destroyed))
					.subscribe(doi => {
						this.onDOICallbackSuccess();
						this.outputRepos.push(doi);
						this.outputReposEmitter.emit(this.outputRepos);
					}, error => this.onDOICallbackError(error));
				break;
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
						depositAuthInfoType: DepositAuthMethod.oAuth2Flow,
						project: this.EntityDoiFields()
					};
					this.depositService.deposit(depositRequest)
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
