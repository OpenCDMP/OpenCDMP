import { Component, OnInit } from '@angular/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
	templateUrl: "./logout.component.html",
	styleUrls: ["./logout.component.scss"],
})
export class LogoutComponent implements OnInit {
	constructor(
		private keycloak: KeycloakService,
		private authService: AuthService,
	) { }

	ngOnInit() {
		this.authService.clear();
		this.keycloak.logout(location.origin).then(() => {
			localStorage.clear();
		});
	}
}
