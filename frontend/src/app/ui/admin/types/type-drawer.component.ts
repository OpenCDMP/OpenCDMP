import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import type { TypeItem, TypeSectionConfig } from './type-section.component';

@Component({
	selector: 'app-type-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule],
	templateUrl: './type-drawer.component.html',
	styleUrl: './type-drawer.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class TypeDrawerComponent implements OnInit {
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly fb = inject(FormBuilder);

	/** Null opens the drawer for a new type. */
	readonly item = input<TypeItem | null>(null);
	readonly config = input.required<TypeSectionConfig>();
	readonly closed = output<void>();
	readonly saved = output<void>();

	protected readonly busy = signal(false);
	protected readonly confirmingDelete = signal(false);

	protected readonly form = this.fb.nonNullable.group({
		name: ['', [Validators.required, Validators.maxLength(250)]],
		code: ['', [Validators.required, Validators.maxLength(200)]],
		available: [true],
	});

	ngOnInit(): void {
		const item = this.item();
		if (item) this.form.setValue({ name: item.name ?? '', code: item.code ?? '', available: item.available });
		if (!this.config().canEdit) this.form.disable();
	}

	protected close(): void {
		this.closed.emit();
	}

	protected save(): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		const value = this.form.getRawValue();
		const item = this.item();
		this.run(this.config().save({
			id: item?.id,
			hash: item?.hash,
			ordinal: item?.ordinal,
			name: value.name.trim(),
			code: value.code.trim(),
			available: value.available,
			shared: false,
			uses: item?.uses ?? 0,
		}));
	}

	protected delete(): void {
		this.run(this.config().delete(this.item().id));
	}

	private run(call: ReturnType<TypeSectionConfig['delete']>): void {
		this.busy.set(true);
		call.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.busy.set(false);
				this.saved.emit();
			},
			error: error => {
				this.busy.set(false);
				this.confirmingDelete.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}
}
