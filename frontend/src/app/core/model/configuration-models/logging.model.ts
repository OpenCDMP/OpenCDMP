export class Logging {

	private _enabled: boolean;
	get enabled(): boolean {
		return this._enabled;
	}

	private _loglevels: string[] = [];
	get loglevels(): string[] {
		return this._loglevels;
	}

	public static parseValue(value: any): Logging {
		const obj: Logging = new Logging();
		obj._enabled = value.enabled;
		obj._loglevels = value.logLevels;
		return obj;
	}
}
