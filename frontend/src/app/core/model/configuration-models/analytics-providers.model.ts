
export class AnalyticsProviders {
  private _providers: AnalyticsProvider[];
  get providers(): AnalyticsProvider[] {
    return this._providers;
  }
  
  set providers(providers: AnalyticsProvider[]) {
    this._providers = providers;
  }

  public static parseValue(value: any): AnalyticsProviders {
		const analyticsProvidersObj: AnalyticsProviders = new AnalyticsProviders();

		analyticsProvidersObj.providers = [];
    for (let providerValue of value.providers) {
      const providerObj: AnalyticsProvider = AnalyticsProvider.parseValue(providerValue);
      analyticsProvidersObj.providers.push(providerObj);
    }

		return analyticsProvidersObj;
	}
}

export class AnalyticsProvider {
  private _type: AnalyticsProviderType;
  
  get type(): AnalyticsProviderType {
    return this._type;
  }

  set type(type: AnalyticsProviderType) {
    this._type = type;
  }

  private _enabled: boolean;

  get enabled(): boolean {
    return this._enabled;
  }

  set enabled(enabled: boolean) {
    this._enabled = enabled;
  }

  private _options: any;

  get options(): any {
    return this._options;
  }

  set options(options: any) {
    this._options = options;
  }

  public static parseValue(value: any): AnalyticsProvider {
		const obj: AnalyticsProvider = new AnalyticsProvider();
		obj.type = value.type;
		obj.enabled = value.enabled;
		obj.options = value.options;
		return obj;
	}  
}

export enum AnalyticsProviderType {
  Matomo = "matomo"
}
