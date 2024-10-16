export enum NotificationServicePermission {
	//Tenant
	BrowseTenant = 'BrowseTenant',
	EditTenant = 'EditTenant',
	DeleteTenant = 'DeleteTenant',
	//Tenant Configuration
	BrowseTenantConfiguration = 'BrowseTenantConfiguration',
	BrowseTenantConfigurationSlackBroadcast = 'BrowseTenantConfigurationSlackBroadcast',
	BrowseTenantConfigurationSmsClient = 'BrowseTenantConfigurationSmsClient',
	BrowseTenantConfigurationEmailClient = 'BrowseTenantConfigurationEmailClient',
	BrowseTenantConfigurationUserLocale = 'BrowseTenantConfigurationUserLocale',
	BrowseTenantAvailableNotifierList = 'BrowseTenantAvailableNotifierList',
	BrowseTenantConfigurationNotifierList = 'BrowseTenantConfigurationNotifierList',
	DeleteTenantConfiguration = 'DeleteTenantConfiguration',
	EditTenantConfiguration = 'EditTenantConfiguration',
	//User
	BrowseUser = 'BrowseUser',
	EditUser = 'EditUser',
	DeleteUser = 'DeleteUser',
	//User Notification Preference
	BrowseUserNotificationPreference = 'BrowseUserNotificationPreference',
	EditUserNotificationPreference = 'EditUserNotificationPreference',
	DeleteUserNotificationPreference = 'DeleteUserNotificationPreference',
	//Contact Info
	BrowseContactInfo = 'BrowseContactInfo',
	//Profile
	BrowseUserProfile = 'BrowseUserProfile',
	//Notifications
	SubmitNotification = 'SubmitNotification',
	BrowseNotification = 'BrowseNotification',
	BrowseNotificationSensitive = 'BrowseNotificationSensitive',
	DeleteNotification = 'DeleteNotification',
	//Notification Templates
	BrowseNotificationTemplate = 'BrowseNotificationTemplate',
	SubmitNotificationTemplate = 'SubmitNotificationTemplate',
	EditNotificationTemplate = 'EditNotificationTemplate',
	DeleteNotificationTemplate = 'DeleteNotificationTemplate',
	//ForgetMe
	BrowseForgetMe = 'BrowseForgetMe',
	EditForgetMe = 'EditForgetMe',
	DeleteForgetMe = 'DeleteForgetMe',
	//WhatYouKnowAboutMe
	BrowseWhatYouKnowAboutMe = 'BrowseWhatYouKnowAboutMe',
	EditWhatYouKnowAboutMe = 'EditWhatYouKnowAboutMe',
	DeleteWhatYouKnowAboutMe = 'DeleteWhatYouKnowAboutMe',
}
