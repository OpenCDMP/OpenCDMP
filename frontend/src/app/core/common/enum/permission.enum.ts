export enum AppPermission {
	/////
	DeferredAffiliation = "DeferredAffiliation",

	//Public
	PublicBrowseDescription = "PublicBrowseDescription",
	PublicBrowseDescriptionTemplate = "PublicBrowseDescriptionTemplate",
	PublicBrowsePlan = "PublicBrowsePlan",
	PublicBrowsePlanReference = "PublicBrowsePlanReference",
	PublicBrowsePlanUser = "PublicBrowsePlanUser",
	PublicBrowseReference = "PublicBrowseReference",
	PublicBrowseUser = "PublicBrowseUser",
	PublicBrowseDashboardStatistics = "PublicBrowseDashboardStatistics",
	PublicSendContactSupport = "PublicSendContactSupport",
	PublicBrowseReferenceType = "PublicBrowseReferenceType",
	PublicClonePlan = "PublicClonePlan",
	PublicCloneDescription = "PublicCloneDescription",
	//Elastic
	ManageElastic = "ManageElastic",
	//Queue Events
	ManageQueueEvents = "ManageQueueEvents",


	//Deposit
	BrowseDeposit = "BrowseDeposit",
	EditDeposit = "BrowseDeposit",


	//Language
	BrowseLanguage = "BrowseLanguage",
	EditLanguage = "EditLanguage",
	DeleteLanguage = "DeleteLanguage",


	//NotificationTemplate
	BrowseNotificationTemplate = "BrowseNotificationTemplate",
	EditNotificationTemplate = "EditNotificationTemplate",
	DeleteNotificationTemplate = "DeleteNotificationTemplate",

	//Language
	BrowseStatistics = "BrowseStatistics",
	BrowsePublicStatistics = "BrowsePublicStatistics",

	//DescriptionTemplate
	BrowseDescriptionTemplate = "BrowseDescriptionTemplate",
	EditDescriptionTemplate = "EditDescriptionTemplate",
	DeleteDescriptionTemplate = "DeleteDescriptionTemplate",
	CloneDescriptionTemplate = "CloneDescriptionTemplate",
	CreateNewVersionDescriptionTemplate = "CreateNewVersionDescriptionTemplate",
	ImportDescriptionTemplate = "ImportDescriptionTemplate",
	ExportDescriptionTemplate = "ExportDescriptionTemplate",



	//User
	BrowseUser = "BrowseUser",
	EditUser = "EditUser",
	DeleteUser = "DeleteUser",
	ExportUsers = "ExportUsers",
	BrowsePlanAssociatedUser = "BrowsePlanAssociatedUser",


	//StorageFile
	BrowseStorageFile = "BrowseStorageFile",
	EditStorageFile = "EditStorageFile",
	DeleteStorageFile = "DeleteStorageFile",

	//DescriptionTemplateType
	BrowseDescriptionTemplateType = "BrowseDescriptionTemplateType",
	EditDescriptionTemplateType = "EditDescriptionTemplateType",
	DeleteDescriptionTemplateType = "DeleteDescriptionTemplateType",

	//Plan
	BrowsePlan = "BrowsePlan",
	EditPlan = "EditPlan",
	NewPlan = "NewPlan",
	DepositPlan = "DepositPlan",
	DeletePlan = "DeletePlan",
	ClonePlan = "ClonePlan",
	ExportPlan = "ExportPlan",
	CreateNewVersionPlan = "CreateNewVersionPlan",
	FinalizePlan = "FinalizePlan",
	UndoFinalizePlan = "UndoFinalizePlan",
	AssignPlanUsers = "AssignPlanUsers",
	InvitePlanUsers = "InvitePlanUsers",
	AnnotatePlan = "AnnotatePlan",
	EvaluatePlan = "EvaluatePlan",

	//PlanStatus
	BrowsePlanStatus = "BrowsePlanStatus",
	EditPlanStatus = "EditPlanStatus",
	DeletePlanStatus = "DeletePlanStatus",

    //DescriptionStatus
    EditDescriptionStatus = "EditDescriptionStatus",
	DeleteDescriptionStatus = "DeleteDescriptionStatus",

	//PlanBlueprint
	BrowsePlanBlueprint = "BrowsePlanBlueprint",
	EditPlanBlueprint = "EditPlanBlueprint",
	DeletePlanBlueprint = "DeletePlanBlueprint",
	ClonePlanBlueprint = "ClonePlanBlueprint",
	CreateNewVersionPlanBlueprint = "CreateNewVersionPlanBlueprint",
	ExportPlanBlueprint = "ExportPlanBlueprint",
	ImportPlanBlueprint = "ImportPlanBlueprint",

	//PlanDescriptionTemplate
	BrowsePlanDescriptionTemplate = "BrowsePlanDescriptionTemplate",
	EditPlanDescriptionTemplate = "EditPlanDescriptionTemplate",
	DeletePlanDescriptionTemplate = "DeletePlanDescriptionTemplate",

	//PlanUser
	BrowsePlanUser = "BrowsePlanUser",
	EditPlanUser = "EditPlanUser",
	DeletePlanUser = "DeletePlanUser",

	//Description
	BrowseDescription = "BrowseDescription",
	AnnotateDescription = "AnnotateDescription",
	EditDescription = "EditDescription",
	FinalizeDescription = "FinalizeDescription",
	DeleteDescription = "DeleteDescription",
	CloneDescription = "CloneDescription",
	ExportDescription = "ExportDescription",
	EvaluateDescription = "EvaluateDescription",

	//DescriptionTag
	BrowseDescriptionTag = "BrowseDescriptionTag",
	EditDescriptionTag = "EditDescriptionTag",
	DeleteDescriptionTag = "DeleteDescriptionTag",

	//DescriptionTemplateType
	BrowseEntityDoi = "BrowseEntityDoi",
	EditEntityDoi = "EditEntityDoi",
	DeleteEntityDoi = "DeleteEntityDoi",

	//UserSettings
	BrowseUserSettings = "BrowseUserSettings",
	EditUserSettings = "EditUserSettings",
	DeleteUserSettings = "DeleteUserSettings",



	//Reference
	BrowseReference = "BrowseReference",
	EditReference = "EditReference",
	DeleteReference = "DeleteReference",

	//Tag
	BrowseTag = "BrowseTag",
	EditTag = "EditTag",
	DeleteTag = "DeleteTag",

	//PlanReference
	BrowsePlanReference = "BrowsePlanReference",
	EditPlanReference = "EditPlanReference",
	DeletePlanReference = "DeletePlanReference",

	//DescriptionReference
	BrowseDescriptionReference = "BrowseDescriptionReference",
	EditDescriptionReference = "EditDescriptionReference",
	DeleteDescriptionReference = "DeleteDescriptionReference",

	//SupportiveMaterial
	BrowseSupportiveMaterial = "BrowseSupportiveMaterial",
	EditSupportiveMaterial= "EditSupportiveMaterial",
	DeleteSupportiveMaterial = "DeleteSupportiveMaterial",

	//ReferenceType
	BrowseReferenceType = "BrowseReferenceType",
	EditReferenceType= "EditReferenceType",
	DeleteReferenceType = "DeleteReferenceType",

	//Tenant
	BrowseTenant = "BrowseTenant",
	EditTenant= "EditTenant",
	DeleteTenant = "DeleteTenant",
	AllowNoTenant = "AllowNoTenant",

	//TenantConfiguration
	BrowseTenantConfiguration = "BrowseTenantConfiguration",
	EditTenantConfiguration = "EditTenantConfiguration",
	DeleteTenantConfiguration = "DeleteTenantConfiguration",

	//TenantUser
	BrowseTenantUser = "BrowseTenantUser",
	EditTenantUser = "EditTenantUser",
	DeleteTenantUser = "DeleteTenantUser",

	//Prefilling
	BrowsePrefilling = "BrowsePrefilling",


	//Lock
	BrowseLock = "BrowseLock",
	EditLock = "EditLock",
	DeleteLock = "DeleteLock",

	//ContactSupport
	SendContactSupport = "SendContactSupport",

	//ActionConfirmation
	BrowseActionConfirmation = "BrowseActionConfirmation",
	EditActionConfirmation = "EditActionConfirmation",
	DeleteActionConfirmation = "DeleteActionConfirmation",

	//PrefillingSource
	BrowsePrefillingSource = "BrowsePrefillingSource",
	EditPrefillingSource= "EditPrefillingSource",
	DeletePrefillingSource = "DeletePrefillingSource",

	//Status
	BrowseStatus = "BrowseStatus",
	EditStatus = "EditStatus",
	DeleteStatus = "DeleteStatus",

	//UsageLimit
	BrowseUsageLimit = "BrowseUsageLimit",
	EditUsageLimit = "EditUsageLimit",
	DeleteUsageLimit = "DeleteUsageLimit",

    //PlanWorkflow
    EditPlanWorkflow = "EditPlanWorkflow",
    DeletePlanWorkflow = "DeletePlanWorkflow",

    //DescriptionWorkflow
    EditDescriptionWorkflow = "EditDescriptionWorkflow",
    DeleteDescriptionWorkflow = "DeleteDescriptionWorkflow",

	// UI Pages
	ViewDescriptionTemplateTypePage = "ViewDescriptionTemplateTypePage",
	ViewMaintenancePage = "ViewMaintenancePage",
	ViewNotificationPage = "ViewNotificationPage",
	ViewNotificationTemplatePage = "ViewNotificationTemplatePage",
	ViewSupportiveMaterialPage = "ViewSupportiveMaterialPage",
	ViewLanguagePage = "ViewLanguagePage",
	ViewUserPage = "ViewUserPage",
	ViewTenantUserPage = "ViewTenantUserPage",
	ViewTenantPage = "ViewTenantPage",
	ViewPrefillingSourcePage = "ViewPrefillingSourcePage",
	ViewReferenceTypePage = "ViewReferenceTypePage",
	ViewReferencePage = "ViewReferencePage",
	ViewEntityLockPage = "ViewEntityLockPage",
	ViewDescriptionTemplatePage = "ViewDescriptionTemplatePage",
	ViewPlanBlueprintPage = "ViewPlanBlueprintPage",
	ViewPublicDescriptionPage = "ViewPublicDescriptionPage",
	ViewPublicPlanPage = "ViewPublicPlanPage",
	ViewMyDescriptionPage = "ViewMyDescriptionPage",
	ViewMyPlanPage = "ViewMyPlanPage",
	ViewHomePage = "ViewHomePage",
	ViewMineInAppNotificationPage = "ViewMineInAppNotificationPage",
	ViewTenantConfigurationPage = "ViewTenantConfigurationPage",
	ViewStatusPage = "ViewStatusPage",
	ViewUsageLimitPage = "ViewUsageLimitPage",
    ViewPlanStatusPage = "ViewPlanStatusPage",
    ViewDescriptionStatusPage = "ViewDescriptionStatusPage",
    ViewPlanWorkflowPage = "ViewPlanWorkflowPage",//TODO remove if workflows remain in tenant config view
    ViewDescriptionWorkflowPage = "ViewDescriptionWorkflowPage",//TODO remove if workflows remain in tenant config view
	ViewIndicatorDashboardPage = "ViewIndicatorDashboardPage",
}

