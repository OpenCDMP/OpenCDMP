import { UserSettingsType } from '@app/core/services/user-settings/user-settings.service';
import { Guid } from '@common/types/guid';

export interface UserSetting {
	id: Guid;
	name: string;
	type: UserSettingsType;
	value: any;
	createdAt: Date;
	updatedAt: Date;
	hash: string;
	userId: Guid;
	isDefault?: boolean;
}

export interface UserSettings {
	key: string;
	settings: UserSetting[];
	defaultSetting: UserSetting;
}

export interface UserSettingPersist {
	id: Guid;
	name: string;
	key: string;
	type: UserSettingsType;
	value: string;
	hash: string;
	isDefault: boolean;
}

export interface UserSettingsKey {
	key: string;
}

//TODO possible move these
export interface UserSettingsInformation<T> {
	key: string;
	type: UserSettingsBuilder<T>;
}

export type UserSettingsBuilder<T> = new () => T;

export interface UserSettingsLookupBuilder<T> {
	update(lookup: T);
	apply(lookup: T): T;
}
