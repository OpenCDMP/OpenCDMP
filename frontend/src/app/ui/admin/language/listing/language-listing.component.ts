import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Language } from '@app/core/model/language/language';
import { LanguageLookup } from '@app/core/query/language.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { BUNDLED_LANGUAGE_CODES, LanguageHttpService } from '@app/core/services/language/language.http.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { flattenTranslations } from '../editor/language-editor.model';
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { LanguageListingFiltersComponent } from './filters/language-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatTooltip } from '@angular/material/tooltip';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';

@Component({
    templateUrl: './language-listing.component.html',
    styleUrls: ['./language-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, RouterLink, MatIcon, AdminListingComponent, LanguageListingFiltersComponent, UserSettingsPickerComponent, TenantSharedChipComponent, MatTooltip, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, TranslatePipe, NullifyValuePipe]
})
export class LanguageListingComponent extends BaseListingComponent<Language, LanguageLookup> implements OnInit {
	publish = false;
	protected override readonly tenantScoped = true;
	userSettingsKey = { key: 'LanguageListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;
	@ViewChild('textsCell', { static: true }) textsCell?: TemplateRef<any>;

	protected englishTextCount = 0;
	private textCounts = new Map<string, { payload: string, count: number }>();

	private readonly lookupFields: string[] = [
		nameof<Language>(x => x.id),
		nameof<Language>(x => x.code),
		nameof<Language>(x => x.payload),
		nameof<Language>(x => x.updatedAt),
		nameof<Language>(x => x.hash),
		nameof<Language>(x => x.belongsToCurrentTenant),
		nameof<Language>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private languageV2Service: LanguageHttpService,
		public authService: AuthService,
		protected languageService: LanguageService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
		this.languageV2Service.getBundledTranslations('en')
			.pipe(takeUntil(this._destroyed))
			.subscribe(texts => this.englishTextCount = flattenTranslations(texts).length);
	}

	protected isBuiltIn(language: Language): boolean {
		return BUNDLED_LANGUAGE_CODES.includes(language?.code);
	}

	// Parsing a payload is not free (old rows hold a full copy of the file), so count each one once.
	protected textCount(language: Language): number {
		const cached = this.textCounts.get(language.id.toString());
		if (cached && cached.payload === language.payload) return cached.count;
		const count = language.payload ? flattenTranslations(JSON.parse(language.payload)).length : 0;
		this.textCounts.set(language.id.toString(), { payload: language.payload, count: count });
		return count;
	}

	/** Whether a search term or non-default filter is currently applied (drives the "no results" empty state). */
	public get hasActiveFilters(): boolean {
		return !!this.lookup?.like
			|| this.lookup?.isActive?.[0] === IsActive.Inactive;
	}

	/** Reset search & filters to their defaults while preserving column selection, ordering and page size. */
	public clearAllFilters(): void {
		const fresh = this.initializeLookup();
		fresh.order = this.lookup.order;
		fresh.project = this.lookup.project;
		fresh.page = { offset: 0, size: this.lookup.page.size };
		this.lookup = fresh;
		this.filterChanged(this.lookup);
	}

	protected canEdit(language: Language): boolean {
		return this.authService.hasPermission(AppPermission.EditLanguage) && language?.belongsToCurrentTenant && language?.isActive === IsActive.Active;
	}

	protected canDelete(language: Language): boolean {
		return this.authService.hasPermission(AppPermission.DeleteLanguage) && language?.belongsToCurrentTenant && language?.isActive === IsActive.Active;
	}

	protected initializeLookup(): LanguageLookup {
		const lookup = new LanguageLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		// Admin listings open on the current tenant's own rows; the shared (default-tenant) rows are one toggle away.
		this.applyScope(lookup, 'own');
		lookup.order = { items: [nameof<Language>(x => x.code)] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Language>(x => x.code),
			sortable: true,
			languageName: 'LANGUAGE-LISTING.FIELDS.LANGUAGE',
			cellTemplate: this.nameCell
		},
		{
			prop: nameof<Language>(x => x.payload),
			sortable: false,
			languageName: 'LANGUAGE-LISTING.FIELDS.TEXTS',
			cellTemplate: this.textsCell
		},
		{
			prop: nameof<Language>(x => x.createdAt),
			sortable: true,
			languageName: 'LANGUAGE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Language>(x => x.updatedAt),
			sortable: true,
			languageName: 'LANGUAGE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			alwaysShown: true,
			sortable: false,
			cellTemplate: this.actions,
			maxWidth: 120
		}
		]);
		this.propertiesAvailableForOrder = this.gridColumns.filter(x => x.sortable);
	}

	//
	// Listing Component functions
	//

	isDeleted(row: Language): boolean {
		return row?.isActive === IsActive.Inactive;
	}

	onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	private onColumnsChangedInternal(columns: string[]) {
		// Here are defined the projection fields that always requested from the api.
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop).filter(x => x != null).forEach(prop => {
			if (columns?.includes(prop as string)) { fields.add(prop as string); } else { fields.delete(prop as string); }
		});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	/** The same query for an arbitrary lookup: the admin listing counts the other tenant scope with it. */
	protected override loadListingWith(lookup: LanguageLookup): Observable<QueryResult<any>> {
		return this.languageV2Service.query(lookup);
	}

	protected loadListing(): Observable<QueryResult<Language>> {
		return this.languageV2Service.query(this.lookup);
	}

	public deleteType(id: Guid) {
		if (id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				data: {
					isDeleteConfirmation: true,
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.languageV2Service.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}
}
