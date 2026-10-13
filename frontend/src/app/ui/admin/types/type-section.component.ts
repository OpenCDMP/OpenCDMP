import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { concat, Observable } from 'rxjs';
import { last } from 'rxjs/operators';
import { TypeDrawerComponent } from './type-drawer.component';

export interface TypeItem {
	id?: Guid;
	hash?: string;
	name: string;
	code: string;
	ordinal?: number;
	available: boolean;
	shared: boolean;
	uses: number;
}

export interface TypeSectionConfig {
	titleKey: string;
	descriptionKey: string;
	usesKey: string;
	reorderable: boolean;
	canEdit: boolean;
	canDelete: boolean;
	load: () => Observable<TypeItem[]>;
	save: (item: TypeItem) => Observable<unknown>;
	delete: (id: Guid) => Observable<unknown>;
	/** Router link and lookup of the listing filtered to one type. */
	listLink: (id: Guid) => { route: string, lookup: string };
}

@Component({
	selector: 'app-type-section',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, DragDropModule, TypeDrawerComponent],
	templateUrl: './type-section.component.html',
	styleUrl: './type-section.component.scss',
})
export class TypeSectionComponent implements OnInit {
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	readonly config = input.required<TypeSectionConfig>();

	protected readonly items = signal<TypeItem[]>([]);
	protected readonly loading = signal(true);
	protected readonly reordering = signal(false);
	/** The drawer's type: undefined while closed, null for a new one. */
	protected readonly opened = signal<TypeItem | null | undefined>(undefined);
	protected readonly own = computed(() => this.items().filter(x => !x.shared));
	protected readonly shared = computed(() => this.items().filter(x => x.shared));

	ngOnInit(): void {
		this.load();
	}

	protected load(): void {
		this.loading.set(true);
		this.config().load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: items => {
				this.items.set(items);
				this.loading.set(false);
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected drop(event: CdkDragDrop<TypeItem[]>): void {
		this.move(event.previousIndex, event.currentIndex);
	}

	protected move(from: number, to: number): void {
		const own = [...this.own()];
		if (to < 0 || to >= own.length || from === to) return;
		moveItemInArray(own, from, to);
		const changed = own.map((x, i) => ({ ...x, ordinal: i + 1 })).filter((x, i) => own[i].ordinal !== i + 1);
		this.items.set([...own.map((x, i) => ({ ...x, ordinal: i + 1 })), ...this.shared()]);
		if (changed.length === 0) return;
		this.reordering.set(true);
		concat(...changed.map(x => this.config().save(x))).pipe(last(), takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.reordering.set(false);
				this.load();
			},
			error: error => {
				this.reordering.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
				this.load();
			},
		});
	}

	protected onSaved(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.opened.set(undefined);
		this.load();
	}
}
