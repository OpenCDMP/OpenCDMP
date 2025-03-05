package org.opencdmp.audit;

import gr.cite.tools.logging.EventId;

public class AuditableAction {

    public static final EventId DescriptionTemplateType_Query = new EventId(1000, "DescriptionTemplateType_Query");
    public static final EventId DescriptionTemplateType_Lookup = new EventId(1001, "DescriptionTemplateType_Lookup");
    public static final EventId DescriptionTemplateType_Persist = new EventId(1002, "DescriptionTemplateType_Persist");
    public static final EventId DescriptionTemplateType_Delete = new EventId(1003, "DescriptionTemplateType_Delete");

    public static final EventId EntityDoi_Query = new EventId(2000, "EntityDoi_Query");
    public static final EventId EntityDoi_Lookup = new EventId(2001, "EntityDoi_Lookup");
    public static final EventId EntityDoi_Persist = new EventId(2002, "EntityDoi_Persist");
    public static final EventId EntityDoi_Delete = new EventId(2003, "EntityDoi_Delete");

    public static final EventId PlanBlueprint_Query = new EventId(3000, "PlanBlueprint_Query");
    public static final EventId PlanBlueprint_Lookup = new EventId(3001, "PlanBlueprint_Lookup");
    public static final EventId PlanBlueprint_Persist = new EventId(3002, "PlanBlueprint_Persist");
    public static final EventId PlanBlueprint_Delete = new EventId(3003, "PlanBlueprint_Delete");
    public static final EventId PlanBlueprint_Clone = new EventId(3004, "PlanBlueprint_Clone");

    public static final EventId PlanBlueprint_PersistNewVersion = new EventId(3005, "PlanBlueprint_PersistNewVersion");
    public static final EventId PlanBlueprint_GetXml = new EventId(3006, "PlanBlueprint_GetXml");
    public static final EventId PlanBlueprint_Import = new EventId(3007, "PlanBlueprint_Import");

    public static final EventId User_Settings_Query = new EventId(4000, "User_Settings_Query");
    public static final EventId User_Settings_Lookup = new EventId(4001, "User_Settings_Lookup");
    public static final EventId User_Settings_Persist = new EventId(4002, "User_Settings_Persist");
    public static final EventId User_Settings_Delete = new EventId(4003, "User_Settings_Delete");

    public static final EventId Plan_Query = new EventId(5000, "Plan_Query");
    public static final EventId Plan_Lookup = new EventId(5001, "Plan_Lookup");
    public static final EventId Plan_Persist = new EventId(5002, "Plan_Persist");
    public static final EventId Plan_Delete = new EventId(5003, "Plan_Delete");
    public static final EventId Plan_Clone = new EventId(5004, "Plan_Clone");
    public static final EventId Plan_PersistNewVersion = new EventId(5005, "Plan_PersistNewVersion");
    public static final EventId Plan_Assign_Users = new EventId(5006, "Plan_Assign_Users");
    public static final EventId Plan_RemoveUser = new EventId(5007, "Plan_RemoveUser");
    public static final EventId Plan_Invite_Users = new EventId(5008, "Plan_Invite_Users");
    public static final EventId Plan_Invite_Accept = new EventId(5009, "Plan_Invite_Accept");
    public static final EventId Plan_PublicQuery = new EventId(5010, "Plan_PublicQuery");
    public static final EventId Plan_Export = new EventId(5011, "Plan_Export");
    public static final EventId Plan_PublicLookup = new EventId(5012, "Plan_PublicLookup");
    public static final EventId Plan_Finalize = new EventId(5013, "Plan_Finalize");
    public static final EventId Plan_Undo_Finalize = new EventId(5014, "Plan_Undo_Finalize");
    public static final EventId Plan_Validate = new EventId(5015, "Plan_Validate");
    public static final EventId Plan_GetXml = new EventId(5016, "Plan_GetXml");
    public static final EventId Plan_Import = new EventId(5017, "Plan_Import");
    public static final EventId Plan_GetPublicXml = new EventId(5017, "Plan_GetPublicXml");
    public static final EventId Plan_ExportPublic = new EventId(5018, "Plan_ExportPublic");
    public static final EventId Plan_PublicClone = new EventId(5019, "Plan_PublicClone");
    public static final EventId Plan_SetStatus = new EventId(5020, "Plan_SetStatus");


    public static final EventId Description_Query = new EventId(6000, "Description_Query");
    public static final EventId Description_Lookup = new EventId(6001, "Description_Lookup");
    public static final EventId Description_Persist = new EventId(6002, "Description_Persist");
    public static final EventId Description_Delete = new EventId(6003, "Description_Delete");
    public static final EventId Description_PublicQuery = new EventId(6004, "Description_PublicQuery");
    public static final EventId Description_PublicLookup = new EventId(6005, "Description_PublicLookup");
    public static final EventId Description_PersistStatus = new EventId(6006, "Description_PersistStatus");
    public static final EventId Description_UploadFieldFiles = new EventId(6007, "Description_UploadFieldFiles");
    public static final EventId Description_GetFieldFile = new EventId(6008, "Description_GetFieldFile");
    public static final EventId Description_Validate = new EventId(6009, "Description_Validate");
    public static final EventId Description_GetDescriptionSectionPermissions = new EventId(6010, "Description_GetDescriptionSectionPermissions");
    public static final EventId Description_UpdateDescriptionTemplate = new EventId(6011, "Description_UpdateDescriptionTemplate");
    public static final EventId Description_GetXml = new EventId(6012, "Description_GetXml");
    public static final EventId Description_GetPublicXml = new EventId(6013, "Description_GetPublicXml");
    public static final EventId Description_PersistMultiple = new EventId(6014, "Description_PersistMultiple");
    public static final EventId Description_Clone = new EventId(6015, "Description_Clone");


    public static final EventId Reference_Query = new EventId(7000, "Reference_Query");
    public static final EventId Reference_Lookup = new EventId(7001, "Reference_Lookup");
    public static final EventId Reference_Persist = new EventId(7002, "Reference_Persist");
    public static final EventId Reference_Delete = new EventId(7003, "Reference_Delete");
    public static final EventId Reference_Search = new EventId(7004, "Reference_Search");
    public static final EventId Reference_Test = new EventId(7005, "Reference_Test");
    
    public static final EventId DescriptionTemplate_Query = new EventId(8000, "DescriptionTemplate_Query");
    public static final EventId DescriptionTemplate_Lookup = new EventId(8001, "DescriptionTemplate_Lookup");
    public static final EventId DescriptionTemplate_Persist = new EventId(8002, "DescriptionTemplate_Persist");
    public static final EventId DescriptionTemplate_Delete = new EventId(8003, "DescriptionTemplate_Delete");
    public static final EventId DescriptionTemplate_Clone = new EventId(8004, "DescriptionTemplate_Clone");
    public static final EventId DescriptionTemplate_PersistNewVersion = new EventId(8005, "DescriptionTemplate_PersistNewVersion");
    public static final EventId DescriptionTemplate_GetXml = new EventId(8006, "DescriptionTemplate_GetXml");
    public static final EventId DescriptionTemplate_Import = new EventId(8007, "DescriptionTemplate_Import");
    
    public static final EventId SupportiveMaterial_Query = new EventId(9000, "SupportiveMaterial_Query");
    public static final EventId SupportiveMaterial_Lookup = new EventId(9001, "SupportiveMaterial_Lookup");
    public static final EventId SupportiveMaterial_Persist = new EventId(9002, "SupportiveMaterial_Persist");
    public static final EventId SupportiveMaterial_Delete = new EventId(9003, "SupportiveMaterial_Delete");

    public static final EventId ReferenceType_Query = new EventId(10000, "ReferenceType_Query");
    public static final EventId ReferenceType_Lookup = new EventId(10001, "ReferenceType_Lookup");
    public static final EventId ReferenceType_Persist = new EventId(10002, "ReferenceType_Persist");
    public static final EventId ReferenceType_Delete = new EventId(10003, "ReferenceType_Delete");

    public static final EventId User_Query = new EventId(11000, "User_Query");
    public static final EventId User_Lookup = new EventId(11001, "User_Lookup");
    public static final EventId User_Persist = new EventId(11002, "User_Persist");
    public static final EventId User_Delete = new EventId(11003, "User_Delete");
    public static final EventId User_LookupByEmail = new EventId(11004, "User_LookupByEmail");
    public static final EventId User_ExportCsv = new EventId(11005, "User_ExportCsv");
    public static final EventId User_PersistRoles = new EventId(11006, "User_PersistRoles");
    public static final EventId User_LanguageMine = new EventId(11007, "User_LanguageMine");
    public static final EventId User_TimezoneMine = new EventId(11008, "User_TimezoneMine");
    public static final EventId User_CultureMine = new EventId(11009, "User_CultureMine");
    public static final EventId User_MergeRequest = new EventId(11010, "User_MergeRequest");
    public static final EventId User_MergeConfirm = new EventId(11011, "User_MergeConfirm");
    public static final EventId User_RemoveCredentialRequest = new EventId(11012, "User_RemoveCredentialRequest");
    public static final EventId User_RemoveCredentialConfirm = new EventId(11013, "User_RemoveCredentialConfirm");
    public static final EventId User_PlanAssociatedQuery = new EventId(11014, "User_PlanAssociatedQuery");
    public static final EventId User_AllowMergeAccount = new EventId(11015, "User_AllowMergeAccount");
    public static final EventId User_InviteToTenant = new EventId(11016, "User_InviteToTenant");
    public static final EventId User_InviteToTenantConfirm = new EventId(11017, "User_InviteToTenantConfirm");

    public static final EventId Tenant_Query = new EventId(12000, "Tenant_Query");
    public static final EventId Tenant_Lookup = new EventId(12001, "Tenant_Lookup");
    public static final EventId Tenant_Persist = new EventId(12002, "Tenant_Persist");
    public static final EventId Tenant_Delete = new EventId(12003, "Tenant_Delete");
    
    public static final EventId Language_Query = new EventId(13000, "Language_Query");
    public static final EventId Language_Lookup = new EventId(13001, "Language_Lookup");
    public static final EventId Language_Persist = new EventId(13002, "Language_Persist");
    public static final EventId Language_Delete = new EventId(13003, "Language_Delete");

    public static final EventId StorageFile_Download = new EventId(14000, "StorageFile_Download");
    public static final EventId StorageFile_Upload = new EventId(14001, "StorageFile_Upload");
    public static final EventId StorageFile_Query = new EventId(14002, "StorageFile_Query");

    public static final EventId Dashboard_MyRecentActivityItems = new EventId(15000, "Dashboard_MyRecentActivityItems");
    public static final EventId Dashboard_MyDashboardStatistics = new EventId(15001, "Dashboard_MyDashboardStatistics");
    public static final EventId Dashboard_PublicDashboardStatistics = new EventId(15002, "Dashboard_PublicDashboardStatistics");

    public static final EventId Notification_Persist = new EventId(16000, "Notification_Persist");

    public static final EventId Lock_Query = new EventId(17000, "Lock_Query");
    public static final EventId Lock_Lookup = new EventId(17001, "Lock_Lookup");
    public static final EventId Lock_Persist = new EventId(17002, "Lock_Persist");
    public static final EventId Lock_Delete = new EventId(17003, "Lock_Delete");
    public static final EventId Lock_IsLocked = new EventId(17004, "Lock_IsLocked");
    public static final EventId Lock_UnLocked = new EventId(17005, "Lock_UnLocked");
    public static final EventId Lock_Touched = new EventId(17006, "Lock_Touched");
    public static final EventId Lock_Locked = new EventId(17007, "Lock_Locked");
    public static final EventId Lock_UnLockedMultiple = new EventId(17008, "Lock_UnLockedMultiple");
    public static final EventId Lock_CheckLock = new EventId(17009, "Lock_CheckLock");

    public static final EventId Deposit_GetAvailableRepositories = new EventId(18000, "Deposit_GetAvailableRepositories");
    public static final EventId Deposit_GetAccessToken = new EventId(18001, "Deposit_GetAccessToken");
    public static final EventId Deposit_Deposit = new EventId(18002, "Deposit_Deposit");
    public static final EventId Deposit_GetLogo = new EventId(18003, "Deposit_GetLogo");
    public static final EventId Deposit_GetRepository = new EventId(18004, "Deposit_GetRepository");

    public static final EventId Tag_Query = new EventId(19000, "Tag_Query");
    public static final EventId Tag_Lookup = new EventId(19001, "Tag_Lookup");
    public static final EventId Tag_Persist = new EventId(19002, "Tag_Persist");
    public static final EventId Tag_Delete = new EventId(19003, "Tag_Delete");

    public static final EventId FileTransformer_GetAvailableConfigurations = new EventId(20000, "FileTransformer_GetAvailableConfigurations");

    public static final EventId Evaluation_Query = new EventId(7000, "Evaluation_Query");
    public static final EventId Evaluation_Lookup = new EventId(10001, "Evaluation_Lookup");
    public static final EventId Evaluation_Persist = new EventId(10002, "Evaluation_Persist");
    public static final EventId Evaluation_Delete = new EventId(10003, "Evaluation_Delete");

    public static final EventId Evaluator_GetAvailableConfigurations = new EventId(20001, "Evaluator_GetAvailableConfigurations");

    public static final EventId ContactSupport_Sent = new EventId(210000, "ContactSupport_Sent");
    public static final EventId ContactSupport_PublicSent = new EventId(210001, "ContactSupport_PublicSent");

    public static final EventId Maintenance_GenerateElastic = new EventId(220000, "Maintenance_GenerateElastic");
    public static final EventId Maintenance_ClearElastic = new EventId(230000, "Maintenance_ClearElastic");
    public static final EventId Maintenance_SendUserTouchEvents = new EventId(230001, "Maintenance_SendUserTouchEvents");
    public static final EventId Maintenance_SendTenantTouchEvents = new EventId(230002, "Maintenance_SendTenantTouchEvents");
    public static final EventId Maintenance_SendPlanTouchEvents = new EventId(230003, "Maintenance_SendPlanTouchEvents");
    public static final EventId Maintenance_SendDescriptionTouchEvents = new EventId(230004, "Maintenance_SendDescriptionTouchEvents");
    public static final EventId Maintenance_SendPlanAccountingEntriesEvents = new EventId(230005, "Maintenance_SendPlanAccountingEntriesEvents");
    public static final EventId Maintenance_SendBlueprintAccountingEntriesEvents = new EventId(230006, "Maintenance_SendBlueprintAccountingEntriesEvents");
    public static final EventId Maintenance_SendDescriptionAccountingEntriesEvents = new EventId(230007, "Maintenance_SendDescriptionAccountingEntriesEvents");
    public static final EventId Maintenance_SendDescriptionTemplateAccountingEntriesEvents = new EventId(230008, "Maintenance_SendDescriptionTemplateAccountingEntriesEvents");
    public static final EventId Maintenance_SendDescriptionTemplateTypeAccountingEntriesEvents = new EventId(230009, "Maintenance_SendDescriptionTemplateTypeAccountingEntriesEvents");
    public static final EventId Maintenance_SendPrefillingSourceAccountingEntriesEvents = new EventId(230010, "Maintenance_SendPrefillingSourceAccountingEntriesEvents");
    public static final EventId Maintenance_SendReferenceTypeAccountingEntriesEvents = new EventId(230011, "Maintenance_SendReferenceTypeAccountingEntriesEvents");
    public static final EventId Maintenance_SendUserAccountingEntriesEvents = new EventId(230012, "Maintenance_SendUserAccountingEntriesEvents");
    public static final EventId Maintenance_SendIndicatorCreateEntryEvents = new EventId(230013, "Maintenance_SendIndicatorCreateEntryEvents");
    public static final EventId Maintenance_SendIndicatorResetEntryEvents = new EventId(230014, "Maintenance_SendIndicatorResetEntryEvents");
    public static final EventId Maintenance_SendIndicatorAccessEntryEvents = new EventId(230015, "Maintenance_SendIndicatorAccessEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointPlanEntryEvents = new EventId(230016, "Maintenance_SendIndicatorPointPlanEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointDescriptionEntryEvents = new EventId(230017, "Maintenance_SendIndicatorPointDescriptionEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointReferenceEntryEvents = new EventId(230018, "Maintenance_SendIndicatorPointReferenceEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointUserEntryEvents = new EventId(230019, "Maintenance_SendIndicatorPointUserEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointPlanBlueprintEntryEvents = new EventId(230020, "Maintenance_SendIndicatorPointPlanBlueprintEntryEvents");
    public static final EventId Maintenance_SendIndicatorPointDescriptionTemplateEntryEvents = new EventId(230021, "Maintenance_SendIndicatorPointDescriptionTemplateEntryEvents");
    public static final EventId Maintenance_SendPlanStatusAccountingEntriesEvents = new EventId(230022, "Maintenance_SendPlanStatusAccountingEntriesEvents");
    public static final EventId Maintenance_SendDescriptionStatusAccountingEntriesEvents = new EventId(230023, "Maintenance_SendDescriptionStatusAccountingEntriesEvents");
    public static final EventId Maintenance_SendEvaluationPlanAccountingEntriesEvents = new EventId(230024, "Maintenance_SendEvaluationPlanAccountingEntriesEvents");
    public static final EventId Maintenance_SendEvaluationDescriptionAccountingEntriesEvents = new EventId(230025, "Maintenance_SendEvaluationDescriptionAccountingEntriesEvents");

    public static final EventId Principal_Lookup = new EventId(240000, "Principal_Lookup");
    public static final EventId Principal_MyTenants = new EventId(240001, "Principal_MyTenants");

    public static final EventId GetSemantics = new EventId(250000, "GetSemantics");

    public static final EventId PrefillingSource_Query = new EventId(260000, "PrefillingSource_Query");
    public static final EventId PrefillingSource_Lookup = new EventId(260001, "PrefillingSource_Lookup");
    public static final EventId PrefillingSource_Persist = new EventId(260002, "PrefillingSource_Persist");
    public static final EventId PrefillingSource_Delete = new EventId(260003, "PrefillingSource_Delete");
    public static final EventId PrefillingSource_Generate = new EventId(260004, "PrefillingSource_Generate");

    public static final EventId TenantConfiguration_Query = new EventId(270000, "TenantConfiguration_Query");
    public static final EventId TenantConfiguration_Lookup = new EventId(270001, "TenantConfiguration_Lookup");
    public static final EventId TenantConfiguration_Persist = new EventId(270002, "TenantConfiguration_Persist");
    public static final EventId TenantConfiguration_Delete = new EventId(270003, "TenantConfiguration_Delete");
    public static final EventId TenantConfiguration_LookupByType = new EventId(270004, "TenantConfiguration_LookupByType");
    public static final EventId TenantConfiguration_LookupBActiveType = new EventId(270005, "TenantConfiguration_LookupBActiveType");

    public static final EventId Annotation_Created_Notify = new EventId(280000, "Annotation_Created_Notify");

    public static final EventId UsageLimit_Query = new EventId(290000, "UsageLimit_Query");
    public static final EventId UsageLimit_Lookup = new EventId(290001, "UsageLimit_Lookup");
    public static final EventId UsageLimit_Persist = new EventId(290002, "UsageLimit_Persist");
    public static final EventId UsageLimit_Delete = new EventId(290003, "UsageLimit_Delete");

    public static final EventId PlanStatus_Query = new EventId(300000, "PlanStatus_Query");
    public static final EventId PlanStatus_Lookup = new EventId(300001, "PlanStatus_Lookup");
    public static final EventId PlanStatus_Persist = new EventId(300002, "PlanStatus_Persist");
    public static final EventId PlanStatus_Delete = new EventId(300003, "PlanStatus_Delete");

    public static final EventId DescriptionStatus_Query = new EventId(400000, "DescriptionStatus_Query");
    public static final EventId DescriptionStatus_Lookup = new EventId(400001, "DescriptionStatus_Lookup");
    public static final EventId DescriptionStatus_Persist = new EventId(400002, "DescriptionStatus_Persist");
    public static final EventId DescriptionStatus_Delete = new EventId(400003, "DescriptionStatus_Delete");

    public static final EventId PlanWorkflow_Query = new EventId(500000, "PlanWorkflow_Query");
    public static final EventId PlanWorkflow_Lookup = new EventId(500001, "PlanWorkflow_Lookup");
    public static final EventId PlanWorkflow_Persist = new EventId(500002, "PlanWorkflow_Persist");
    public static final EventId PlanWorkflow_Delete = new EventId(500003, "PlanWorkflow_Delete");

    public static final EventId DescriptionWorkflow_Query = new EventId(600000, "DescriptionWorkflow_Query");
    public static final EventId DescriptionWorkflow_Lookup = new EventId(600001, "DescriptionWorkflow_Lookup");
    public static final EventId DescriptionWorkflow_Persist = new EventId(600002, "DescriptionWorkflow_Persist");
    public static final EventId DescriptionWorkflow_Delete = new EventId(600003, "DescriptionWorkflow_Delete");

}
