import { Component, OnInit } from "@angular/core";
import { MatButtonToggleChange } from "@angular/material/button-toggle";
import { Tenant } from "@app/core/model/tenant/tenant";
import { AuthService } from "@app/core/services/auth/auth.service";
import { PrincipalService } from "@app/core/services/http/principal.service";
import { TenantHandlingService } from "@app/core/services/tenant/tenant-handling.service";
import { BaseComponent } from "@common/base/base.component";
import { BaseHttpParams } from "@common/http/base-http-params";
import { InterceptorType } from "@common/http/interceptors/interceptor-type";
import { Observable } from "rxjs";

@Component({
    selector: 'app-tenant-switch',
    templateUrl: 'tenant-switch.component.html',
    styleUrls: ['tenant-switch.component.scss'],
    standalone: false
})
export class TenantSwitchComponent extends BaseComponent implements OnInit {
	tenants: Observable<Array<Tenant>>;

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
		this.tenants = this.loadUserTenants();
	}

	loadUserTenants(): Observable<Array<Tenant>> {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		return this.principalService.myTenants({ params: params });
	}

	onTenantSelected(selectedTenant: MatButtonToggleChange) {
		if (selectedTenant.value === undefined || selectedTenant.value === '') return;

		this.authService.selectedTenant(selectedTenant.value);
		window.location.href = this.tenantHandlingService.getCurrentUrlEnrichedWithTenantCode(selectedTenant.value, true);
	}
}
