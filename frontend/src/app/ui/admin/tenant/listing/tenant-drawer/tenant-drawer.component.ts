import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Tenant } from '@app/core/model/tenant/tenant';
import { TenantService } from '@app/core/services/tenant/tenant.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-tenant-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule],
	templateUrl: './tenant-drawer.component.html',
	styleUrl: './tenant-drawer.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class TenantDrawerComponent implements OnInit {
	private readonly tenantService = inject(TenantService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly fb = inject(FormBuilder);

	/** Null opens the drawer for a new organisation. */
	readonly tenantId = input<Guid | null>(null);
	readonly closed = output<void>();
	readonly saved = output<void>();

	protected readonly loading = signal(false);
	protected readonly busy = signal(false);
	private hash: string;

	protected readonly form = this.fb.nonNullable.group({
		name: ['', [Validators.required, Validators.maxLength(500)]],
		code: ['', [Validators.required, Validators.maxLength(200), Validators.pattern('^[a-z0-9_]+$')]],
		description: ['', Validators.required],
	});

	ngOnInit(): void {
		const id = this.tenantId();
		if (!id) return;
		this.form.controls.code.disable();
		this.loading.set(true);
		this.tenantService.getSingle(id, [
			nameof<Tenant>(x => x.id),
			nameof<Tenant>(x => x.name),
			nameof<Tenant>(x => x.code),
			nameof<Tenant>(x => x.description),
			nameof<Tenant>(x => x.hash),
		]).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: tenant => {
				this.hash = tenant.hash;
				this.form.patchValue({ name: tenant.name ?? '', code: tenant.code ?? '', description: tenant.description ?? '' });
				this.loading.set(false);
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
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
		this.busy.set(true);
		this.tenantService.persist({ id: this.tenantId(), hash: this.hash, name: value.name.trim(), code: value.code.trim(), description: value.description.trim() })
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: () => {
					this.busy.set(false);
					this.saved.emit();
					this.closed.emit();
				},
				error: error => {
					this.busy.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
	}
}
