import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { Plan } from '@app/core/model/plan/plan';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { PublishDialogComponent, PublishDialogData } from '@app/ui/plan/publish-dialog/publish-dialog.component';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { MatMenuItem, MatMenuTrigger, MatMenu } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-plan-deposit-dropdown',
    templateUrl: './plan-deposit-dropdown.component.html',
    styleUrls: ['./plan-deposit-dropdown.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatMenuItem, MatMenuTrigger, MatTooltip, MatIcon, MatButton, MatMenu, TranslatePipe]
})
export class PlanDepositDropdown extends BaseComponent implements OnInit {
	@Input() inputRepos: DepositConfiguration[];
	@Input() plan: Plan;
    @Input() disabled: boolean = false;
    // FE: render as a mat-menu-item (with its repo submenu) instead of a standalone stroked button,
    // so the plan editor can host Deposit inside its "More actions" overflow menu.
    @Input() asMenuItem: boolean = false;
	@Input() outputRepos: EntityDoi[]= [];
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	@Output() outputReposEmitter: EventEmitter<EntityDoi[]> = new EventEmitter<EntityDoi[]>();
	constructor(
		private depositService: DepositService,
		private dialog: MatDialog,
		private language: TranslateService,
		private sanitizer: DomSanitizer,
		private configurationService: ConfigurationService
	) {
		super();
	}

	ngOnInit(): void {
		[...(this.inputRepos ?? []), ...this.publishedRepos.map(x => x.repo)].forEach(repo => {
			if (repo.hasLogo && !this.logos.has(repo.repositoryId)) {
				this.depositService.getLogo(repo.repositoryId).subscribe(logo => {
					this.logos.set(repo.repositoryId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + logo.body));
				})
			}
		})
	}

	get publishedRepos(): { repo: DepositConfiguration, doi: EntityDoi }[] {
		return (this.plan?.entityDois ?? [])
			.map(doi => ({ repo: this.depositService.availableDeposits()?.find(repo => repo.repositoryId === doi.repositoryId), doi }))
			.filter(x => !!x.repo);
	}

	deposit(repo: DepositConfiguration) {
		const data: PublishDialogData = { repo, planId: this.plan.id, planGroupId: Guid.parse(this.plan.groupId), repositoryLabel: this.getRepostioryDepositSourceLabel(repo.repositoryId) };
		this.dialog.open<PublishDialogComponent, PublishDialogData, EntityDoi>(PublishDialogComponent, { width: 'min(680px, 95vw)', maxWidth: '95vw', autoFocus: 'dialog', data })
			.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(doi => {
				if (!doi) return;
				this.outputRepos.push(doi);
				this.outputReposEmitter.emit(this.outputRepos);
			});
	}

	recordUrl(repo: DepositConfiguration, doi: EntityDoi): string {
		return repo.repositoryRecordUrl?.replace(this.configurationService.depositRecordUrlIdPlaceholder, doi.doi) ?? null;
	}

	// the record address tells two connections of the same repository apart
	host(repo: DepositConfiguration): string {
		try {
			return repo.repositoryRecordUrl ? new URL(repo.repositoryRecordUrl.replace(this.configurationService.depositRecordUrlIdPlaceholder, 'x')).host : null;
		} catch {
			return null;
		}
	}

	getRepostioryDepositSourceLabel(repositoryId: string): string {
		const translationKey = `PLAN-LISTING.DEPOSIT-DROPDOWN.${repositoryId.toUpperCase()}`;
		const translated = this.language.instant(translationKey);
		if (translated !== translationKey) return translated;
		return repositoryId;
	}

}
