import { ClipboardModule } from '@angular/cdk/clipboard';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { DepositAuthMethod } from '@app/core/common/enum/deposit-auth-method';
import { DepositFileKind } from '@app/core/common/enum/deposit-file-kind';
import { DepositFile } from '@app/core/model/deposit/deposit-file';
import FileSaver from 'file-saver';
import { ResponseErrorCode } from '@app/core/common/enum/respone-error-code';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { DepositPreviewModel, DepositPreviewResult } from '@app/core/model/deposit/deposit-preview';
import { DepositRequest } from '@app/core/model/deposit/deposit-request';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DepositHttpService } from '@app/core/services/deposit/deposit.http.service';
import { DepositOauth2DialogService } from '@app/ui/misc/deposit-oauth2-dialog/service/deposit-oauth2-dialog.service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { skip, take } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DepositReviewComponent } from './review/deposit-review.component';

export interface PublishDialogData {
	repo: DepositConfiguration;
	planId: Guid;
	repositoryLabel: string;
}

type PublishStep = 'account' | 'review' | 'done' | 'failed' | 'review-failed';
type PublishAs = 'org' | 'own';
type OwnAccountMode = 'saved' | 'signin' | 'token';

@Component({
	selector: 'app-publish-dialog',
	templateUrl: './publish-dialog.component.html',
	styleUrls: ['./publish-dialog.component.scss'],
	changeDetection: ChangeDetectionStrategy.OnPush,
	providers: [DepositOauth2DialogService],
	imports: [
		ReactiveFormsModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatRadioModule,
		MatFormFieldModule,
		MatInputModule,
		MatCheckboxModule,
		MatProgressSpinnerModule,
		ClipboardModule,
		TranslateModule,
		DepositReviewComponent,
	]
})
export class PublishDialogComponent implements OnInit {
	readonly data = inject<PublishDialogData>(MAT_DIALOG_DATA);
	private dialogRef = inject<MatDialogRef<PublishDialogComponent, EntityDoi>>(MatDialogRef);
	private depositHttpService = inject(DepositHttpService);
	private oauth2DialogService = inject(DepositOauth2DialogService);
	private configurationService = inject(ConfigurationService);
	private destroyRef = inject(DestroyRef);

	readonly repo = this.data.repo;
	readonly repositoryLabel = this.data.repositoryLabel;
	readonly steps: PublishStep[] = ['account', 'review', 'done'];

	readonly step = signal<PublishStep>('account');
	readonly loading = signal(true);
	readonly busy = signal(false);
	readonly loadError = signal(false);
	readonly methods = signal<DepositAuthMethod[]>([]);
	readonly hasSaved = signal(false);
	readonly publishAs = signal<PublishAs>(null);
	readonly ownMode = signal<OwnAccountMode>(null);
	readonly preview = signal<DepositPreviewModel>(null);
	readonly reviewLoaded = signal(false);
	readonly signInCancelled = signal(false);
	readonly doi = signal<EntityDoi>(null);
	readonly error = signal<string>(null);
	readonly copied = signal(false);
	readonly organisationAccountName = signal<string>(null);
	readonly files = signal<DepositFile[]>(null);
	readonly downloading = signal<DepositFileKind>(null);
	readonly fileKinds = DepositFileKind;
	readonly failure = signal<'sign-in' | 'credentials' | 'other'>('other');
	readonly shown = signal<Set<string>>(new Set());
	readonly saveCredentials = signal(true);

	readonly review = viewChild(DepositReviewComponent);

	readonly credentialFields = (this.repo.userConfigurationFields ?? []).filter(field => field.authInfo);
	readonly credentials = new FormGroup<Record<string, FormControl<string>>>(
		Object.fromEntries(this.credentialFields.map(field => [field.code, new FormControl<string>('', Validators.required)]))
	);

	readonly canOrg = computed(() => this.methods().includes(DepositAuthMethod.PluginDefault));
	readonly canSignIn = computed(() => this.methods().includes(DepositAuthMethod.oAuth2Flow));
	readonly canProfile = computed(() => this.methods().includes(DepositAuthMethod.AuthInfoFromUserProfile));
	readonly canToken = computed(() => this.canProfile() && this.credentialFields.length > 0);
	readonly canOwn = computed(() => this.canSignIn() || this.canToken() || this.hasSaved());
	readonly choices = computed(() => (this.canOrg() ? 1 : 0) + (this.canOwn() ? 1 : 0));
	readonly stepIndex = computed(() => {
		const step = this.step();
		return step === 'done' ? 2 : step === 'failed' || step === 'review-failed' ? 1 : this.steps.indexOf(step);
	});

	ngOnInit(): void {
		this.depositHttpService.getAvailableAuthMethods(this.repo.repositoryId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				const methods = result?.depositAuthInfoTypes ?? [];
				this.methods.set(methods);
				this.hasSaved.set(!!result?.hasMyAccountValue && methods.includes(DepositAuthMethod.AuthInfoFromUserProfile));
				this.organisationAccountName.set(result?.organisationAccountName || null);
				// saved credentials always start on the user's own account; otherwise the organisation's preselection decides
				const own = this.hasSaved() || (result?.preselected !== 'organisation' && this.canOwn());
				this.publishAs.set(own || !this.canOrg() ? 'own' : 'org');
				this.ownMode.set(this.hasSaved() ? 'saved' : this.canSignIn() ? 'signin' : 'token');
				this.loading.set(false);
			},
			error: () => { this.loadError.set(true); this.loading.set(false); }
		});
	}

	setOwnMode(mode: OwnAccountMode): void {
		this.ownMode.set(mode);
		this.publishAs.set('own');
	}

	toggleShown(code: string): void {
		const shown = new Set(this.shown());
		if (shown.has(code)) shown.delete(code); else shown.add(code);
		this.shown.set(shown);
	}

	continue(): void {
		if (this.publishAs() === 'own' && this.ownMode() === 'token' && this.credentials.invalid) {
			this.credentials.markAllAsTouched();
			return;
		}
		this.signInCancelled.set(false);
		this.loadFiles();
		if (this.reviewLoaded() || !this.repo.supportsPreview) {
			this.step.set('review');
			return;
		}
		this.busy.set(true);
		this.depositHttpService.preview({ repositoryId: this.repo.repositoryId, planId: this.data.planId }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: preview => {
				this.preview.set(preview?.groups?.length ? preview : null);
				this.reviewLoaded.set(true);
				this.busy.set(false);
				this.step.set('review');
			},
			error: error => {
				this.busy.set(false);
				this.error.set(error?.error?.error || error?.error?.message || null);
				this.step.set('review-failed');
			}
		});
	}

	publish(): void {
		const review = this.review();
		const edits = review ? review.result() : null;
		if (review && !edits) return;
		this.signInCancelled.set(false);

		if (this.publishAs() === 'org') {
			this.send(this.request(DepositAuthMethod.PluginDefault, edits));
			return;
		}
		switch (this.ownMode()) {
			case 'saved':
				this.send(this.request(DepositAuthMethod.AuthInfoFromUserProfile, edits));
				break;
			case 'token': {
				const request = this.request(DepositAuthMethod.AuthInfoFromUserProfile, edits);
				request.authFields = this.credentialFields.map(field => ({ code: field.code, textValue: this.credentials.controls[field.code].value }));
				request.saveAuthFields = this.saveCredentials();
				this.send(request);
				break;
			}
			case 'signin':
				// the pop-up has to open from the click itself, or the browser blocks it
				// login() first replays an empty value, then emits the code (or nothing) once the pop-up closes
				this.oauth2DialogService.login(this.oauth2DialogService.getLoginUrl(this.repo)).pipe(skip(1), take(1), takeUntilDestroyed(this.destroyRef)).subscribe(code => {
					if (!code) { this.signInCancelled.set(true); return; }
					const request = this.request(DepositAuthMethod.oAuth2Flow, edits);
					request.authorizationCode = code;
					this.send(request);
				});
				break;
		}
	}

	useOrganisationAccount(): void {
		this.publishAs.set('org');
		this.step.set('review');
		this.publish();
	}

	retry(): void {
		if (!this.reviewLoaded() && this.repo.supportsPreview) {
			this.step.set('account');
			this.continue();
			return;
		}
		this.step.set('review');
		this.publish();
	}

	backToAccount(): void {
		this.error.set(null);
		this.step.set('account');
	}

	backToReview(): void {
		this.error.set(null);
		this.step.set('review');
	}

	close(): void {
		this.dialogRef.close(this.doi() ?? undefined);
	}

	recordUrl(): string {
		const doi = this.doi()?.doi;
		return doi ? this.repo.repositoryRecordUrl?.replace(this.configurationService.depositRecordUrlIdPlaceholder, doi) ?? null : null;
	}

	download(file: DepositFile): void {
		this.downloading.set(file.kind);
		this.depositHttpService.getFile(this.repo.repositoryId, this.data.planId, file.kind).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: response => {
				this.downloading.set(null);
				FileSaver.saveAs(new Blob([response.body], { type: response.body.type }), file.filename);
			},
			error: () => this.downloading.set(null)
		});
	}

	private loadFiles(): void {
		if (this.files()) return;
		this.depositHttpService.getFiles(this.repo.repositoryId, this.data.planId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: files => this.files.set(files ?? []),
			error: () => this.files.set([])
		});
	}

	private request(method: DepositAuthMethod, edits: DepositPreviewResult): DepositRequest {
		return {
			repositoryId: this.repo.repositoryId,
			planId: this.data.planId,
			authorizationCode: null,
			depositAuthInfoType: method,
			project: {
				fields: [
					nameof<EntityDoi>(x => x.id),
					nameof<EntityDoi>(x => x.repositoryId),
					nameof<EntityDoi>(x => x.doi),
					nameof<EntityDoi>(x => x.entityId),
					nameof<EntityDoi>(x => x.isActive),
				]
			},
			storedFields: edits?.storedFields?.length ? edits.storedFields : null,
			storedGroups: edits?.storedGroups?.length ? edits.storedGroups : null
		};
	}

	private send(request: DepositRequest): void {
		this.busy.set(true);
		this.error.set(null);
		this.depositHttpService.deposit(request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: doi => {
				this.busy.set(false);
				if (!doi?.doi) { this.fail(null); return; }
				this.doi.set(doi);
				this.step.set('done');
			},
			error: error => this.fail(error)
		});
	}

	private fail(error: any): void {
		this.busy.set(false);
		const code = error?.error?.code;
		this.failure.set(code === ResponseErrorCode.depositSignInFailed ? 'sign-in' : code === ResponseErrorCode.depositCredentialsMissing ? 'credentials' : 'other');
		this.error.set(error?.error?.error || error?.error?.message || null);
		this.step.set('failed');
	}
}
