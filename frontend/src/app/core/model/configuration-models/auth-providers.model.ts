
export class AuthProviders {
  
  private _authProviders: AuthProvider[];
  get authProviders(): AuthProvider[] {
    return this._authProviders;
  }
  
  private _defaultAuthProvider: AuthProvider;
  get defaultAuthProvider(): AuthProvider {
    return this._defaultAuthProvider;
  }

	public static parseValue(value: any): AuthProviders {
		const authProvidersObj: AuthProviders = new AuthProviders();

    authProvidersObj._defaultAuthProvider = AuthProvider.parseValue(value.defaultAuthProvider);

		authProvidersObj._authProviders = [];
    for (let authProviderValue of value.authProviders) {
      const authProviderObj: AuthProvider = AuthProvider.parseValue(authProviderValue);
      authProvidersObj._authProviders.push(authProviderObj);
    }

		return authProvidersObj;
	}
  
  public findOrGetDefault(providerName: string, culture: string): AuthProvider {
    const authProvider = this.find(providerName, culture);

    if (authProvider === undefined) return this.defaultAuthProvider;

    return authProvider;
	}

  public find(providerName: string, culture: string): AuthProvider | undefined {
    return this.authProviders.find(p => p.name === providerName && p.cultures.includes(culture));
	}
}

export class AuthProvider {
  private _name: string;
  get name(): string {
    return this._name;
  }
  
  private _providerClass: string;
  get providerClass(): string {
    return this._providerClass;
  }
  
  private _cultures: string[];
  get cultures(): string[] {
    return this._cultures;
  }

  public static parseValue(value: any): AuthProvider {
		const obj: AuthProvider = new AuthProvider();
		obj._name = value.name;
		obj._providerClass = value.providerClass;
		obj._cultures = value.cultures;
		return obj;
	}
}
