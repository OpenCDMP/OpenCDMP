import { Component, OnInit } from '@angular/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanWebSocketService } from '@app/core/services/websocket/plan-websocket.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
    templateUrl: "./logout.component.html",
    styleUrls: ["./logout.component.scss"],
    standalone: false
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
