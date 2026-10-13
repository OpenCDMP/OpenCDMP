import { DestroyRef, Injectable, Signal, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subject, concat, filter, switchMap, toArray } from 'rxjs';
import { TcEditorModel, TcPart } from './tc-part';

/** Anything a section saves with its one save bar: a tenant configuration part, a workflow, notification preferences. */
export interface TcSaveable {
	readonly dirty: Signal<boolean>;
	save(): Observable<boolean>;
	discard(): void;
}

/**
 * The state of one section of the tenant configuration page: its editable parts, how many have unsaved
 * changes, and saving / discarding / resetting them together. Provide it on the section component.
 */
@Injectable()
export class TcSectionService {
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly formService = inject(FormService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly authService = inject(AuthService);
	private readonly destroyRef = inject(DestroyRef);

	private readonly saveables = signal<TcSaveable[]>([]);
	readonly dirtyCount = computed(() => this.saveables().filter(x => x.dirty()).length);
	readonly saving = signal(false);
	/** Emits after a save or reset went through, for sections that show server-computed state. */
	readonly saved = new Subject<void>();
	readonly canEdit = this.authService.hasPermission(AppPermission.EditTenantConfiguration);
	readonly canReset = this.authService.hasPermission(AppPermission.DeleteTenantConfiguration);

	/** A tenant configuration type edited on this section, loaded right away unless `load` is false. */
	part(type: TenantConfigurationType, lookupFields: string[], createModel: () => TcEditorModel, options: { load?: boolean } = {}): TcPart {
		const part = new TcPart(
			{ tenantConfigurationService: this.tenantConfigurationService, formService: this.formService, httpErrorHandlingService: this.httpErrorHandlingService, destroyRef: this.destroyRef },
			type, [...lookupFields, 'belongsToCurrentTenant'], createModel, () => !this.canEdit);
		this.register(part);
		if (options.load !== false) part.load().subscribe();
		return part;
	}

	register(saveable: TcSaveable): void {
		this.saveables.update(list => [...list, saveable]);
	}

	/** Saves every part with unsaved changes, one after another; stops at the first invalid one. */
	saveAll(): void {
		const dirty = this.saveables().filter(x => x.dirty());
		if (dirty.length === 0 || this.saving()) return;
		this.saving.set(true);
		concat(...dirty.map(x => x.save())).pipe(
			toArray(),
			takeUntilDestroyed(this.destroyRef),
		).subscribe({
			next: results => {
				this.saving.set(false);
				if (results.every(x => x)) {
					this.saved.next();
					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				} else {
					this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-CONFIGURATION.SAVE-BAR.CHECK-FIELDS'), SnackBarNotificationLevel.Warning);
				}
			},
			error: error => {
				this.saving.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	discardAll(): void {
		this.saveables().filter(x => x.dirty()).forEach(x => x.discard());
	}

	/** Asks for confirmation, then removes the tenant's own value so the default applies again. */
	confirmReset(reset: () => Observable<void>): void {
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '360px',
			data: {
				message: this.language.instant('TENANT-CONFIGURATION.STATE.RESET-CONFIRM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(
			filter(Boolean),
			switchMap(() => reset()),
			takeUntilDestroyed(this.destroyRef),
		).subscribe({
			next: () => {
				this.saved.next();
				this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-CONFIGURATION.STATE.RESET-DONE'), SnackBarNotificationLevel.Success);
			},
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
		});
	}
}
