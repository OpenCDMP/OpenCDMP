import { KeycloakFlow } from 'keycloak-js';

export class KeycloakConfiguration {

	private _enabled: boolean
	get enabled(): boolean {
		return this._enabled;
	}

	private _address: string
	get address(): string {
		return this._address;
	}

	private _realm: string
	get realm(): string {
		return this._realm;
	}

	private _flow: KeycloakFlow
	get flow(): KeycloakFlow {
		return this._flow;
	}

	private _clientId: string
	get clientId(): string {
		return this._clientId;
	}

	private _silentCheckSsoRedirectUri: string
	get silentCheckSsoRedirectUri(): string {
		return this._silentCheckSsoRedirectUri;
	}

	private _scope: string
	get scope(): string {
		return this._scope;
	}



	public static parseValue(value: any): KeycloakConfiguration {
		const obj: KeycloakConfiguration = new KeycloakConfiguration();
		obj._enabled = value.enabled;
		obj._address = value.address;
		obj._realm = value.realm;
		obj._flow = value.flow;
		obj._clientId = value.clientId;
		obj._silentCheckSsoRedirectUri = value.silentCheckSsoRedirectUri;
		obj._scope = value.scope;
		return obj;
	}
}
