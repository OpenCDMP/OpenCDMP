import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { TenantConfigurationType } from "@notification-service/core/enum/tenant-configuration-type";
import { NotificationContactType } from "@notification-service/core/enum/notification-contact-type";

export interface TenantConfiguration extends BaseEntity{
	type?: TenantConfigurationType;
	notifierList?: NotifierListTenantConfiguration;
	defaultUserLocale?: DefaultUserLocaleTenantConfiguration;
}

export interface NotifierListTenantConfiguration{
	notifiers: { [key: string]: NotificationContactType[] };
}

export interface DefaultUserLocaleTenantConfiguration{
	timezone: string;
	language: string;
	culture: string;
}

//persist

export interface TenantConfigurationPersist extends BaseEntityPersist{
	type: TenantConfigurationType;
	notifierList?: NotifierListTenantConfigurationPersist;
	defaultUserLocale?: DefaultUserLocaleTenantConfigurationPersist;
}

export interface NotifierListTenantConfigurationPersist{
	notifiers: { [key: string]: NotificationContactType[] };
}

export interface DefaultUserLocaleTenantConfigurationPersist{
	timezone: string;
	language: string;
	culture: string;
}
