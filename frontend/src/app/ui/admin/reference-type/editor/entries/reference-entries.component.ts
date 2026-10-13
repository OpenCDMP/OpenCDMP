import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { Reference, ReferenceDefinition, ReferenceField, ReferencePersist } from '@app/core/model/reference/reference';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

type SourceFilter = 'any' | 'external' | 'internal';

/** The values a reference type holds: fetched from its sources or typed in by people, with where each one is used. */
@Component({
	selector: 'app-reference-entries',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule],
	templateUrl: './reference-entries.component.html',
	styleUrl: './reference-entries.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class ReferenceEntriesComponent implements OnInit {
	private readonly referenceService = inject(ReferenceService);
	private readonly referenceTypeService = inject(ReferenceTypeService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly filterService = inject(FilterService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);
	private readonly authService = inject(AuthService);
	protected readonly routerUtils = inject(RouterUtilsService);

	readonly typeId = input.required<Guid>();

	protected readonly canEdit = this.authService.hasPermission(AppPermission.EditReference);
	protected readonly canDelete = this.authService.hasPermission(AppPermission.DeleteReference);
	protected readonly internal = ReferenceSourceType.Internal;

	protected readonly items = signal<Reference[]>([]);
	protected readonly count = signal(0);
	protected readonly loading = signal(true);
	protected readonly pageIndex = signal(0);
	protected readonly pageSize = signal(25);
	protected readonly search = signal('');
	protected readonly source = signal<SourceFilter>('any');
	protected readonly usedOnly = signal(false);
	protected readonly selected = signal<Reference | null>(null);
	protected readonly selectedFields = computed<ReferenceField[]>(() => (this.selected()?.definition?.fields ?? []).filter(x => !!x.value));
	protected readonly editing = signal(false);
	protected readonly saving = signal(false);
	protected form: UntypedFormGroup = null;
	/** The type's own fields, loaded the first time an entry is edited. */
	protected typeFields: ReferenceTypeField[] = null;
	protected labelTemplate: string = null;
	private readonly searchChanged = new Subject<string>();

	ngOnInit(): void {
		this.searchChanged.pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef)).subscribe(term => {
			this.search.set(term);
			this.pageIndex.set(0);
			this.load();
		});
		this.load();
	}

	protected onSearch(term: string): void {
		this.searchChanged.next(term);
	}

	protected onSource(source: SourceFilter): void {
		this.source.set(source);
		this.pageIndex.set(0);
		this.load();
	}

	protected onUsedOnly(value: boolean): void {
		this.usedOnly.set(value);
		this.pageIndex.set(0);
		this.load();
	}

	protected onPage(event: PageEvent): void {
		this.pageIndex.set(event.pageIndex);
		this.pageSize.set(event.pageSize);
		this.load();
	}

	private load(reselect?: Guid): void {
		const lookup = new ReferenceLookup();
		lookup.typeIds = [this.typeId()];
		lookup.isActive = [IsActive.Active];
		lookup.like = this.filterService.transformLike(this.search());
		if (this.usedOnly()) lookup.isUsed = true;
		if (this.source() !== 'any') lookup.sourceTypes = [this.source() === 'internal' ? ReferenceSourceType.Internal : ReferenceSourceType.External];
		lookup.metadata = { countAll: true };
		lookup.page = { offset: this.pageIndex() * this.pageSize(), size: this.pageSize() };
		lookup.order = { items: ['-' + nameof<Reference>(x => x.updatedAt)] };
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.label),
				nameof<Reference>(x => x.reference),
				nameof<Reference>(x => x.abbreviation),
				nameof<Reference>(x => x.description),
				nameof<Reference>(x => x.source),
				nameof<Reference>(x => x.sourceType),
				nameof<Reference>(x => x.updatedAt),
				nameof<Reference>(x => x.planCount),
				nameof<Reference>(x => x.descriptionCount),
				nameof<Reference>(x => x.belongsToCurrentTenant),
				nameof<Reference>(x => x.hash),
				[nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.code)].join('.'),
				[nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.dataType)].join('.'),
				[nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.value)].join('.'),
			]
		};
		this.loading.set(true);
		this.referenceService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				this.items.set(result.items);
				this.count.set(result.count);
				this.loading.set(false);
				if (reselect) this.selected.set(result.items.find(x => x.id === reselect) ?? null);
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected open(item: Reference): void {
		this.editing.set(false);
		this.selected.set(item);
	}

	protected close(): void {
		this.editing.set(false);
		this.selected.set(null);
	}

	protected startEdit(item: Reference): void {
		if (this.typeFields) {
			this.buildForm(item);
			return;
		}
		this.referenceTypeService.getSingle(this.typeId(), [
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.labelTemplate)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.dataType)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.required)].join('.'),
		]).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: type => {
				this.typeFields = type.definition?.fields ?? [];
				this.labelTemplate = type.definition?.labelTemplate?.trim() || null;
				this.buildForm(item);
			},
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
		});
	}

	// With a name template the name is built from the fields, as in the manual-entry form, so it isn't asked for.
	private buildForm(item: Reference): void {
		const templateCodes = Array.from(this.labelTemplate?.matchAll(/\{([\w-]+)\}/g) ?? []).map(m => m[1]);
		const form = new UntypedFormGroup({ description: new FormControl(item.description ?? null) });
		if (!this.labelTemplate) form.addControl('label', new FormControl(item.label ?? null, Validators.required));
		this.typeFields.forEach(field => {
			const value = item.definition?.fields?.find(x => x.code === field.code)?.value ?? null;
			form.addControl(field.code, new FormControl(value, field.required || templateCodes.includes(field.code) ? Validators.required : null));
		});
		this.form = form;
		this.editing.set(true);
	}

	protected isRequired(code: string): boolean {
		return this.form?.get(code)?.hasValidator(Validators.required) ?? false;
	}

	protected save(item: Reference): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		const value = this.form.getRawValue();
		const typeCodes = this.typeFields.map(x => x.code);
		const persist: ReferencePersist = {
			id: item.id,
			hash: item.hash,
			label: this.labelTemplate
				? this.labelTemplate.replace(/\{([\w-]+)\}/g, (_m, code) => value[code]?.toString().trim() ?? '').replace(/\s+/g, ' ').trim()
				: value.label,
			description: value.description,
			typeId: this.typeId(),
			reference: item.reference,
			abbreviation: item.abbreviation,
			source: item.source,
			sourceType: item.sourceType,
			definition: {
				fields: [
					...this.typeFields.map(field => ({ code: field.code, dataType: field.dataType, value: value[field.code] })),
					// fields the type no longer defines are kept as they are
					...(item.definition?.fields ?? []).filter(x => !typeCodes.includes(x.code)).map(x => ({ code: x.code, dataType: x.dataType, value: x.value })),
				]
			},
		};
		this.saving.set(true);
		this.referenceService.persist(persist).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.saving.set(false);
				this.editing.set(false);
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.load(item.id);
			},
			error: error => {
				this.saving.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected isManual(item: Reference): boolean {
		return item?.sourceType === ReferenceSourceType.Internal;
	}

	protected usage(item: Reference): string {
		const plans = item.planCount ?? 0;
		const descriptions = item.descriptionCount ?? 0;
		if (!plans && !descriptions) return this.language.instant('REFERENCE-ENTRIES.UNUSED');
		return this.language.instant('REFERENCE-ENTRIES.USED-IN', { plans, descriptions });
	}

	protected delete(item: Reference): void {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				isDeleteConfirmation: true,
				message: this.language.instant('REFERENCE-ENTRIES.DELETE-CONFIRM', { name: item.label }),
				confirmButton: this.language.instant('REFERENCE-ENTRIES.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(confirmed => {
			if (!confirmed) return;
			this.referenceService.delete(item.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: () => {
					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
					this.close();
					this.load();
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
		});
	}
}
