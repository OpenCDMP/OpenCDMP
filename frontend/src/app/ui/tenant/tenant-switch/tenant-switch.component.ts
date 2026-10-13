import { Component, Input, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { MatButtonToggleChange, MatButtonToggleGroup, MatButtonToggle } from "@angular/material/button-toggle";
import { Tenant } from "@app/core/model/tenant/tenant";
import { AuthService } from "@app/core/services/auth/auth.service";
import { PrincipalService } from "@app/core/services/http/principal.service";
import { TenantHandlingService } from "@app/core/services/tenant/tenant-handling.service";
import { BaseComponent } from "@common/base/base.component";
import { NgClass } from "@angular/common";
import { MatIcon } from "@angular/material/icon";

@Component({
    selector: 'app-tenant-switch',
    templateUrl: 'tenant-switch.component.html',
    styleUrls: ['tenant-switch.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButtonToggleGroup, MatButtonToggle, NgClass, MatIcon]
})
export class TenantSwitchComponent extends BaseComponent implements OnInit {
	@Input() tenants: Tenant[];

	protected readonly indicatorColorCount = 11;

	constructor(
		private principalService: PrincipalService,
		private authService: AuthService,
		private tenantHandlingService: TenantHandlingService
	) {
		super();
	}

	get currentTenant(): string {
		return this.authService.selectedTenant();
	}

	ngOnInit() {
	}

	onTenantSelected(selectedTenant: MatButtonToggleChange) {
		if (selectedTenant.value === undefined || selectedTenant.value === '') return;

		this.authService.selectedTenant(selectedTenant.value);
		window.location.href = this.tenantHandlingService.getCurrentUrlEnrichedWithTenantCode(selectedTenant.value, true);
	}
}
