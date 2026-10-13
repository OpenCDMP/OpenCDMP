import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { SupportiveMaterial, SupportiveMaterialPersist } from '@app/core/model/supportive-material/supportive-material';
import { SupportiveMaterialLookup } from '@app/core/query/supportive-material.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterLink } from '@angular/router';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { catchError, filter, throwError } from 'rxjs';
import { nameof } from 'ts-simple-nameof';

type CellState = 'CUSTOMISED' | 'SHARED' | 'BUILT-IN';

interface PageRow {
	type: SupportiveMaterialFieldType;
	name: string;
	url: string;
}

interface LanguageColumn {
	code: string;
	name: string;
}

interface OpenCell {
	row: PageRow;
	language: LanguageColumn;
}

const PAGE_PATHS: [SupportiveMaterialFieldType, string][] = [
	[SupportiveMaterialFieldType.About, '/about'],
	[SupportiveMaterialFieldType.Faq, '/faq'],
	[SupportiveMaterialFieldType.UserGuide, '/user-guide'],
	[SupportiveMaterialFieldType.Glossary, '/glossary'],
	[SupportiveMaterialFieldType.TermsOfService, '/terms-and-conditions'],
	[SupportiveMaterialFieldType.CookiePolicy, '/cookies-policy'],
];

@Component({
	selector: 'app-help-pages',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, RichTextEditorModule, RouterLink],
	templateUrl: './help-pages.component.html',
	styleUrl: './help-pages.component.scss',
	host: { '(document:keydown.escape)': 'onEscape()' },
})
export class HelpPagesComponent {
	private readonly supportiveMaterialService = inject(SupportiveMaterialService);
	private readonly languageHttpService = inject(LanguageHttpService);
	private readonly languageService = inject(LanguageService);
	private readonly authService = inject(AuthService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);
	private readonly enumUtils = inject(EnumUtils);
	private readonly sanitizer = inject(DomSanitizer);
	protected readonly routerUtils = inject(RouterUtilsService);

	protected readonly canEdit = this.authService.hasPermission(AppPermission.EditSupportiveMaterial);
	protected readonly canDelete = this.authService.hasPermission(AppPermission.DeleteSupportiveMaterial);

	protected readonly rows: PageRow[] = PAGE_PATHS.map(([type, path]) => ({
		type,
		name: this.enumUtils.toSupportiveMaterialTypeString(type),
		url: this.routerUtils.generateUrl(path),
	}));
	protected readonly onboardingUrl = this.routerUtils.generateUrl('/tenant-configuration/onboarding');

	protected readonly languages = signal<LanguageColumn[]>([]);
	private readonly selectedCode = signal<string | null>(null);
	/** One language at a time: 17 side by side do not fit next to the settings menu. */
	protected readonly selectedLanguage = computed(() => {
		const languages = this.languages();
		const code = this.selectedCode() ?? this.language.currentLang;
		return languages.find(x => x.code === code) ?? languages.find(x => x.code === 'en') ?? languages[0] ?? null;
	});
	private readonly records = signal<SupportiveMaterial[]>([]);
	protected readonly openCell = signal<OpenCell | null>(null);
	protected readonly busy = signal(false);
	protected readonly dirty = signal(false);
	protected readonly editing = signal(false);
	private readonly readOnlyText = signal('');
	// the frame is sandboxed without scripts, and the built-in files are whole HTML documents with their own styles
	protected readonly readOnlyHtml = computed<SafeHtml>(() => this.sanitizer.bypassSecurityTrustHtml(this.readOnlyText()));

	protected readonly payload = new FormControl<string>('');

	protected readonly record = computed(() => {
		const cell = this.openCell();
		return cell ? this.recordOf(cell.row.type, cell.language.code) : null;
	});
	protected readonly state = computed(() => this.stateOf(this.record()));

	constructor() {
		this.payload.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.dirty.set(this.payload.dirty));
		if (!this.canEdit) this.payload.disable();

		this.languageHttpService.query(this.languageHttpService.buildAutocompleteLookup())
			.pipe(takeUntilDestroyed())
			.subscribe(data => this.languages.set((data.items ?? []).map(x => ({ code: x.code, name: this.languageService.getLanguageName(x.code) }))));
		this.loadRecords();
	}

	protected selectLanguage(code: string): void {
		this.selectedCode.set(code);
	}

	protected customisedIn(code: string): boolean {
		return this.rows.some(row => this.cellState(row.type, code) !== 'BUILT-IN');
	}

	protected cellState(type: SupportiveMaterialFieldType, code: string): CellState {
		return this.stateOf(this.recordOf(type, code));
	}

	protected open(row: PageRow, language: LanguageColumn): void {
		const current = this.openCell();
		if (current?.row.type === row.type && current?.language.code === language.code) return;
		this.confirmDiscard(() => {
			this.openCell.set({ row, language });
			this.fill();
		});
	}

	protected close(): void {
		this.confirmDiscard(() => this.openCell.set(null));
	}

	protected customise(): void {
		// the editor renders inside the app, so the document's own <style> blocks would restyle the whole page
		const doc = new DOMParser().parseFromString(this.readOnlyText(), 'text/html');
		doc.querySelectorAll('style, script, link').forEach(x => x.remove());
		this.payload.reset(doc.body.innerHTML);
		this.payload.markAsDirty();
		this.dirty.set(true);
		this.editing.set(true);
	}

	protected onEscape(): void {
		if (this.openCell() && this.dialog.openDialogs.length === 0) this.close();
	}

	protected save(): void {
		const cell = this.openCell();
		const record = this.record();
		const persist: SupportiveMaterialPersist = {
			type: cell.row.type,
			languageCode: cell.language.code,
			payload: this.payload.value,
		};
		// a shared default-tenant row stays as it is; saving gives this organisation its own copy
		if (record?.belongsToCurrentTenant !== false) {
			persist.id = record?.id;
			persist.hash = record?.hash;
		}
		this.busy.set(true);
		this.supportiveMaterialService.persist(persist).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.payload.markAsPristine();
				this.dirty.set(false);
				this.loadRecords();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected reset(): void {
		const record = this.record();
		if (!record?.id) return;
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('HELP-PAGES.RESET-CONFIRM'),
				confirmButton: this.language.instant('HELP-PAGES.ACTIONS.RESET'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
			this.busy.set(true);
			this.supportiveMaterialService.delete(record.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: () => {
					this.records.update(list => list.filter(x => x.id !== record.id));
					this.busy.set(false);
					this.fill();
				},
				error: error => {
					this.busy.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
		});
	}

	private recordOf(type: SupportiveMaterialFieldType, code: string): SupportiveMaterial | null {
		const matches = this.records().filter(r => r.type === type && r.languageCode === code);
		return matches.find(r => r.belongsToCurrentTenant !== false) ?? matches[0] ?? null;
	}

	private stateOf(record: SupportiveMaterial | null): CellState {
		if (!record) return 'BUILT-IN';
		return record.belongsToCurrentTenant === false ? 'SHARED' : 'CUSTOMISED';
	}

	private fill(): void {
		const cell = this.openCell();
		if (!cell) return;
		const record = this.record();
		const customised = this.state() === 'CUSTOMISED';
		this.editing.set(customised);
		this.payload.reset(customised ? record.payload : '');
		this.dirty.set(false);
		this.readOnlyText.set(customised ? '' : record?.payload ?? '');
		if (record) return;
		// built-in texts ship in English only for most languages; show the English one rather than an error
		this.supportiveMaterialService.getPayloadFromFile(cell.row.type, cell.language.code)
			.pipe(
				catchError(error => cell.language.code !== 'en' ? this.supportiveMaterialService.getPayloadFromFile(cell.row.type, 'en') : throwError(() => error)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe({
				next: response => {
					if (this.openCell() !== cell || this.record() || this.editing()) return;
					this.readOnlyText.set(response.body ?? '');
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
	}

	private loadRecords(): void {
		const lookup = new SupportiveMaterialLookup();
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 1000 };
		lookup.order = { items: ['-' + nameof<SupportiveMaterial>(x => x.updatedAt)] };
		lookup.project = {
			fields: [
				nameof<SupportiveMaterial>(x => x.id),
				nameof<SupportiveMaterial>(x => x.type),
				nameof<SupportiveMaterial>(x => x.languageCode),
				nameof<SupportiveMaterial>(x => x.payload),
				nameof<SupportiveMaterial>(x => x.hash),
				nameof<SupportiveMaterial>(x => x.belongsToCurrentTenant),
			],
		};
		this.supportiveMaterialService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				this.records.set(result.items ?? []);
				this.busy.set(false);
				if (!this.payload.dirty) this.fill();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private confirmDiscard(proceed: () => void): void {
		if (!this.payload.dirty) {
			proceed();
			return;
		}
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('HELP-PAGES.DISCARD-CONFIRM'),
				confirmButton: this.language.instant('HELP-PAGES.ACTIONS.DISCARD'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => proceed());
	}
}
