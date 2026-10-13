import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

/**
 * Explains a disabled admin editor: the item is managed by the default tenant and is read-only on
 * the selected tenant. Same pattern as the plan editor's "limited access" notice. Renders nothing
 * unless `readOnly` is true, so editors can include it unconditionally.
 */
@Component({
	selector: 'app-tenant-readonly-banner',
	standalone: true,
	imports: [MatIconModule, TranslateModule],
	template: `
		@if (readOnly()) {
			<div class="tenant-readonly-banner" role="note">
				<mat-icon class="material-symbols-outlined" aria-hidden="true">lock</mat-icon>
				<span>{{ 'COMMONS.TENANT-SHARED.EDITOR-BANNER' | translate }}</span>
			</div>
		}
	`,
	changeDetection: ChangeDetectionStrategy.Eager,
	styles: [`
		.tenant-readonly-banner {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			padding: 0.75rem 1rem;
			border-radius: 0.5rem;
			background: var(--mat-sys-surface-container-high);
			color: var(--mat-sys-on-surface-variant);
			font-size: var(--mat-sys-body-medium-size);
		}
		mat-icon { flex: 0 0 auto; }
	`],
})
export class TenantReadonlyBannerComponent {
	readonly readOnly = input<boolean>(false);
}
