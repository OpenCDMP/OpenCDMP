export class HelpService {

	private _enabled: boolean;
	get enabled(): boolean {
		return this._enabled;
	}

	private _url: string;
	get url(): string {
		return this._url;
	}

	public static parseValue(value: any): HelpService {
		const obj: HelpService = new HelpService();
		obj._enabled = value.Enabled;
		obj._url = value.Url;
		return obj;
	}

}
