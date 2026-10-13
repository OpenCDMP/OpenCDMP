import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

/**
 * "Shared" marker for admin listing rows that belong to the default tenant while another tenant is
 * selected: such rows can be opened (and sometimes cloned) here but not edited. Renders nothing for
 * the tenant's own rows, so it can sit next to every title unconditionally.
 */
@Component({
	selector: 'app-tenant-shared-chip',
	standalone: true,
	imports: [MatTooltipModule, TranslateModule],
	changeDetection: ChangeDetectionStrategy.Eager,
	template: `
		@if (item()?.belongsToCurrentTenant === false) {
			<span class="status-chip status-chip-shared" [matTooltip]="'COMMONS.TENANT-SHARED.TOOLTIP' | translate" tabindex="0">
				{{ 'COMMONS.TENANT-SHARED.CHIP' | translate }}
			</span>
		}
	`,
})
export class TenantSharedChipComponent {
	readonly item = input<{ belongsToCurrentTenant?: boolean } | null | undefined>();
}
