import { Injectable } from '@angular/core';
import { UserSettingPersist, UserSettingsInformation, UserSettings as UserSettingsObject } from '@app/core/model/user-settings/user-settings.model';
import { BaseService } from '@common/base/base.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import moment, { Moment } from 'moment';
import { Observable, Subject, of as observableOf } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { AuthService, LoginStatus } from '../auth/auth.service';
import { ConfigurationService } from '../configuration/configuration.service';
import { UserSettingsHttpService } from './user-settings-http.service';

export enum UserSettingsType {
	Setting = 0,
	Config = 1
}

@Injectable()
export class UserSettingsService extends BaseService {
	static CACHE_SETTINGS_LIFETIME = 7200; // (value is in seconds) 2 hours

	public userSettingUpdated: Subject<string>;
	private userSettingsMap: Map<String, any> = new Map<String, any>();
	private userSettingsLastAccessMap: Map<String, Moment> = new Map<String, Moment>();

	constructor(
		private userSettingsHttpService: UserSettingsHttpService,
		private authService: AuthService,
		private installationConfigurationService: ConfigurationService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
		this.userSettingUpdated = new Subject<string>();

		this.authService.getAuthenticationStateObservable().pipe(takeUntil(this._destroyed)).subscribe(authenticationState => {
			if (authenticationState.loginStatus === LoginStatus.LoggedOut) {
				localStorage.clear();
				this.userSettingsMap = new Map<String, any>();
				this.userSettingsLastAccessMap = new Map<String, Moment>();
			}
		});
	}

	public getUserSettingUpdatedObservable(): Observable<string> {
		return this.userSettingUpdated.asObservable();
	}

	get<T>(userSettingsInformation: UserSettingsInformation<T>): Observable<UserSettings<T>> {
		if (this.userSettingsLastAccessMap.has(userSettingsInformation.key)) {
			const lastAccess = this.userSettingsLastAccessMap.get(userSettingsInformation.key);
			const difference = moment.utc().diff(lastAccess, 'seconds');

			if (difference > UserSettingsService.CACHE_SETTINGS_LIFETIME) {
				this.clearSetting(userSettingsInformation.key);
			}
		}

		this.userSettingsLastAccessMap.set(userSettingsInformation.key, moment.utc());

		if (this.userSettingsMap.has(userSettingsInformation.key)) { return observableOf(this.userSettingsMap.get(userSettingsInformation.key)); }

		return this.loadUserSettings<T>(userSettingsInformation);
	}

	set<T>(setting: UserSetting<T>, isDefault: boolean = false, userSettingsInformation: UserSettingsInformation<T> = null) {
		const userSetting = this.buildUserSetting(setting, userSettingsInformation.key, isDefault);
		let userSettings: UserSettings<T> = this.buildUserSettings(userSetting, userSettingsInformation.key);

		if (this.userSettingsMap.has(userSettingsInformation.key)) {
			userSettings = this.userSettingsMap.get(userSettingsInformation.key);
			if (userSettings == null) { userSettings = this.buildUserSettings(userSetting, userSettingsInformation.key); }
			const itemIndex = userSettings.settings.findIndex(item => item.id === userSetting.id);
			if (itemIndex < 0) {
				userSettings.settings.push(userSetting);
			} else {
				userSettings.settings[itemIndex] = userSetting;
			}
			if (isDefault) {
				userSettings.defaultSetting = userSetting;
			}
		} else {
			userSetting.isDefault = isDefault;
			if (isDefault) { userSettings.defaultSetting = userSetting; }
		}

		userSettings.settings.forEach((element) => {
			if (!(element.id === userSettings.defaultSetting.id)) {
				element.isDefault = false;
			}
		});

		this.persistUserSettings(userSettingsInformation.key, userSettings, userSettingsInformation, true);
		this.userSettingsLastAccessMap.set(userSettingsInformation.key, moment.utc());
	}

	remove<T>(id: Guid, userSettingsInformation: UserSettingsInformation<T> = null) {
		this.deleteFromWebServer(id, userSettingsInformation);
	}

	share(userSettings: UserSetting<any>, targetId: Guid, newName: string): Observable<UserSettingsObject> {
		const userSettingsToPersist: UserSettingPersist = {
			id: null,
			key: userSettings.key,
			isDefault: false,
			name: newName,
			type: userSettings.type,
			value: JSON.stringify(userSettings.value),
			hash: null
		};
		return this.userSettingsHttpService.share(userSettingsToPersist, targetId);
	}

	private deleteFromWebServer<T>(id: Guid, userSettingsInformation: UserSettingsInformation<T>) {
		this.userSettingsHttpService.delete(id).pipe(takeUntil(this._destroyed)).subscribe(item => {
			const result: UserSettings<T> = (item ? this.toTypedUserSettings<T>(item as UserSettingsObject) : null);
			this.persistUserSettings(userSettingsInformation.key, result, userSettingsInformation, false);
			this.userSettingUpdated.next(userSettingsInformation.key);
		},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	private buildUserSettings<T>(setting: UserSetting<T>, key: string): UserSettings<T> {
		const userSettings: UserSettings<T> = {
			key: key,
			settings: [setting],
			defaultSetting: setting
		};
		return userSettings;
	}

	private buildUserSetting<T>(setting: UserSetting<T>, key: string, isDefault: boolean): UserSetting<T> {
		const userSettings: UserSetting<T> = {
			id: setting.id,
			key: key,
			name: setting.name,
			value: setting.value,
			type: UserSettingsType.Setting,
			isDefault: isDefault,
			hash: setting.hash
		};
		return userSettings;
	}

	private loadUserSettings<T>(userSettingsInformation: UserSettingsInformation<T>): Observable<UserSettings<T>> {
		const localStorageItem = this.loadFromLocalStorage(userSettingsInformation.key);
		if (localStorageItem) {
			const jsonLocalStorageItem = JSON.parse(localStorageItem);
			if (jsonLocalStorageItem) { return observableOf((jsonLocalStorageItem as UserSettings<T>)); }
		}

		return this.userSettingsHttpService.getSingle(userSettingsInformation.key).pipe(
			map(x => {
				const result: UserSettings<T> = (x ? this.toTypedUserSettings<T>(x as UserSettingsObject) : null);
				this.persistUserSettings(userSettingsInformation.key, result, userSettingsInformation, false);
				return result;
			}));
	}

	private persistUserSettings<T>(key: string, userSettings: UserSettings<T>, userSettingsInformation: UserSettingsInformation<T>, pushChangeToServer: boolean = true) {
		this.userSettingsMap.set(key, userSettings);
		this.storeToLocalStorage(key, userSettings);

		if (pushChangeToServer) {
			this.persistSettingsChanges(key, userSettings, userSettingsInformation);
		}
	}

	private persistSettingsChanges<T>(key: string, userSettings: UserSettings<T>, userSettingsInformation: UserSettingsInformation<T>, updateLocalAfterPush: boolean = true) {
		const changesToPush: UserSettingPersist[] = [];
		const userSettingsPersist = this.prepareSettingsToPushToWebServer(userSettings);
		userSettingsPersist.forEach(element => {
			changesToPush.push(element);
		});
		this.pushToWebServer(changesToPush, userSettingsInformation, updateLocalAfterPush);
	}

	private buildPersistModel(element: any): UserSettingPersist {
		const userSettingsToPersist: UserSettingPersist = {
			id: element.id,
			key: element.key,
			isDefault: element.isDefault,
			name: element.name,
			type: element.type,
			value: JSON.stringify(element.value),
			hash: element.hash
		};

		return userSettingsToPersist;
	}

	private prepareSettingsToPushToWebServer(userSettings: UserSettings<any>): UserSettingPersist[] {

		const userSettingsArray: UserSettingPersist[] = [];
		if (userSettings && userSettings.settings && userSettings.settings.length !== 0) {
			userSettings.settings.forEach(element => {
				userSettingsArray.push(this.buildPersistModel(element));
			});
		}
		return userSettingsArray;
	}

	private toTypedUserSettings<T>(userSettings: UserSettingsObject): UserSettings<T> {
		const settingsArray: UserSetting<T>[] = [];
		if (userSettings && userSettings.settings && userSettings.settings.length !== 0) {
			userSettings.settings.forEach(element => {
				const settingElement: UserSetting<T> = {
					id: element.id,
					key: userSettings.key,
					name: element.name,
					value: JSON.parse(element.value),
					userId: element.userId,
					updatedAt: element.updatedAt,
					createdAt: element.createdAt,
					hash: element.hash,
					type: element.type,
					isDefault: element.isDefault
				};
				settingsArray.push(settingElement);
			});
		}
		let defaultSetting: UserSetting<T> = null;
		if (userSettings.defaultSetting) {
			const defaultSettingValue = userSettings.defaultSetting;
			defaultSetting = {
				id: defaultSettingValue.id,
				key: userSettings.key,
				name: defaultSettingValue.name,
				value: userSettings.defaultSetting ? JSON.parse(defaultSettingValue.value) : defaultSettingValue.value,
				userId: defaultSettingValue.userId,
				updatedAt: defaultSettingValue.updatedAt,
				createdAt: defaultSettingValue.createdAt,
				hash: defaultSettingValue.hash,
				type: defaultSettingValue.type,
				isDefault: defaultSettingValue.isDefault
			};
		}
		const result: UserSettings<T> = {
			key: userSettings.key,
			settings: settingsArray,
			defaultSetting: defaultSetting
		};
		return result;
	}

	private pushToWebServer<T>(userSettingsToPersist: UserSettingPersist[], userSettingsInformation: UserSettingsInformation<T>, updateLocalAfterPush: boolean) {
		if (!userSettingsToPersist || userSettingsToPersist.length === 0) { return; }
		this.userSettingsHttpService.persistAll(userSettingsToPersist).pipe(takeUntil(this._destroyed)).subscribe(items => {
			if (updateLocalAfterPush) {
				const result: UserSettings<any>[] = items ? items.map(x => this.toTypedUserSettings<any>(x as UserSettingsObject)) : [];
				result.forEach(item => {
					if (item.defaultSetting != null) {
						this.userSettingsMap.set(item.key, item);
						this.storeToLocalStorage(item.key, item);
					} else {
						this.clearSetting(item.key);
					}
					this.userSettingUpdated.next(item.key);
				});
			}
		},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	private clearSetting(key: string) {
		this.userSettingsMap.delete(key);
		this.userSettingsLastAccessMap.delete(key);
		this.deleteFromLocalStorage(key);
	}

	private loadFromLocalStorage(key: string) {
		return localStorage.getItem(this.generateLocalStorageKey(key));
	}

	private storeToLocalStorage(key: string, value: any) {
		return localStorage.setItem(this.generateLocalStorageKey(key), JSON.stringify(value));
	}

	private deleteFromLocalStorage(key: string) {
		return localStorage.removeItem(this.generateLocalStorageKey(key));
	}

	private generateLocalStorageKey(key: string): string {
		return `${this.getUserSettingsVersion()}_${this.getUserId()}_${key}`;
	}

	private getUserId(): Guid {
		return this.authService.userId();
	}

	private getUserSettingsVersion(): string {
		return this.installationConfigurationService.userSettingsVersion;
	}
}

export interface UserSettings<T> {
	key: string;
	settings: UserSetting<T>[];
	defaultSetting: UserSetting<T>;
}

export interface UserSetting<T> {
	id: Guid;
	name: string;
	key: string;
	type: UserSettingsType;
	value: T;
	createdAt?: Date;
	updatedAt?: Date;
	hash?: string;
	userId?: Guid;
	isDefault?: boolean;
}
