import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanWebSocketService } from '@app/core/services/websocket/plan-websocket.service';
import { KeycloakService } from 'keycloak-angular';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    templateUrl: "./logout.component.html",
    styleUrls: ["./logout.component.scss"],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TranslatePipe]
})
export class LogoutComponent implements OnInit {
	constructor(
		private keycloak: KeycloakService,
		private authService: AuthService,
		private planWebSocketService: PlanWebSocketService
	) { }

	ngOnInit() {
		this.authService.clear();
		this.keycloak.logout(location.origin).then(() => {
			localStorage.clear();
			this.planWebSocketService.closeConnection();
		});
	}
}
