
export class Sidebar {
  
  private _infoItems: SidebarItem[];
  get infoItems(): SidebarItem[] {
    return this._infoItems;
  }
  
  private _footerItems: SidebarItem[];
  get footerItems(): SidebarItem[] {
    return this._footerItems;
  }

	public static parseValue(value: any): Sidebar {
		const sidebarItemsObj: Sidebar = new Sidebar();

    sidebarItemsObj._infoItems = [];
    for (let infoItem of value.infoItems) {
      const infoItemObj: SidebarItem = SidebarItem.parseValue(infoItem);
      sidebarItemsObj._infoItems.push(infoItemObj);
    }
    
		sidebarItemsObj._footerItems = [];
    for (let footerItem of value.footerItems) {
      const footerItemObj: SidebarItem = SidebarItem.parseValue(footerItem);
      sidebarItemsObj._footerItems.push(footerItemObj);
    }

		return sidebarItemsObj;
	}
}

export class SidebarItem {
  private _routerPath: string;
  get routerPath(): string {
    return this._routerPath;
  }
  
  private _title: string;
  get title(): string {
    return this._title;
  }
  
  private _icon: string;
  get icon(): string {
    return this._icon;
  }
  
  private _externalUrl?: string|null;
  get externalUrl(): string|null {
    return this._externalUrl;
  }

  private _accessLevel: AccessLevel;
  get accessLevel(): AccessLevel {
    return this._accessLevel;
  }

  get isExternalLink(): boolean {
    return this.externalUrl != null && this.externalUrl != '';
  }

  public static parseValue(value: any): SidebarItem {
		const obj: SidebarItem = new SidebarItem();
		obj._routerPath = value.routerPath;
		obj._title = value.title;
		obj._icon = value.icon;
		obj._externalUrl = value.externalUrl;
		obj._accessLevel = value.accessLevel ?? AccessLevel.Public;
		return obj;
	}
}

export enum AccessLevel {
  Authenticated = "authenticated",
  Unauthenticated = "unauthenticated",
  Public = "public",
}
