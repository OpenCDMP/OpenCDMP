import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BlueprintFieldSet, DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, RootSection, PlanCreationConsent } from '@app/core/model/plan-blueprint/plan-blueprint';
import { BaseField, BaseFieldData, BaseFieldSet, BaseSection, Multiplicity, FieldDefaultValue, FieldExternalDatasetData, FieldLabelAndMultiplicityData, FieldReferenceTypeData, FieldRule, FieldSelectData, FieldSelectOption, FieldSystemData, FieldUploadData, FieldUploadOption, DescriptionTemplateDefinition } from '@app/core/model/description-template/description-template';
import { Plan, PlanAuthorizationInformation, PlanAuthorizationInformationPerSection, PlanContact, PlanDescriptionTemplate, PlanProperties, PlanPropertiesConsent, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference, PlanReferenceData } from '@app/core/model/plan/plan-reference';
import { UserContactInfo } from '@app/core/model/user/user';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceDefinition, ReferenceField, Reference } from '@app/core/model/reference/reference';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { PlanStatus, PlanStatusDefinition } from '@app/core/model/plan-status/plan-status';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { User } from '@app/core/model/user/user';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PropertyDefinitionFieldSet, PropertyDefinitionFieldSetItem, PropertyDefinitionField, DescriptionExternalIdentifier, Description } from '@app/core/model/description/description';
import { Tag } from '@app/core/model/tag/tag';
import { DescriptionStatus, DescriptionStatusDefinition } from '@app/core/model/description-status/description-status';

@Injectable()
export class PlanEditorEntityResolver extends BaseEditorResolver {

	constructor(private planService: PlanService, private breadcrumbService: BreadcrumbService) {
		super();
	}

    public static planDescriptionTemplateFields(): string[] {
        return [
            [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplates)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.version)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>((x) => x.name)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>((x) => x.id)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>((x) => x.iconConfiguration)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>((x) => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>((x) => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
        ]
    }

    public static planEditorDescriptionsLookupFields(): string[] {
        return [
            'PlanEditorDescriptionsLookupFields'
        ];
    }

	public static planEditorPlanBlueprintForPlanLookupFields(): string[] {
		return [
			"PlanEditorPlanBlueprintForPlanLookupFields"
            
		]
	}

	public static planEditorPlanBlueprintLookupFields(): string[] {
		return [
			"PlanEditorPlanBlueprintLookupFields"
		]
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Plan>(x => x.id),
			nameof<Plan>(x => x.label),

			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),	
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),					
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),					
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),					
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),					
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),					
			
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.description),
			nameof<Plan>(x => x.language),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.isActive),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.publicAfter),
			nameof<Plan>(x => x.creator),
			nameof<Plan>(x => x.hash),

			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DepositPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EvaluatePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.AnnotatePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.AnnotateDescription].join('.'),

			[nameof<Plan>(x => x.statusAuthorizationFlags), PlanStatusPermission.Edit].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts),nameof<PlanContact>(x => x.firstName),].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts),nameof<PlanContact>(x => x.lastName),].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts), nameof<PlanContact>(x => x.email)].join('.'),

			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.consent), nameof<PlanPropertiesConsent>(x => x.consentAt)].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.consent), nameof<PlanPropertiesConsent>(x => x.consentBy), nameof<User>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.comment)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.ordinal)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.textValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.textListValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.dateValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.booleanValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.numberValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.identifier)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.type)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.label)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.reference)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.isActive)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.label)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.isActive)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.hash)].join('.'),


            ...PlanEditorEntityResolver.planEditorDescriptionsLookupFields(),
            [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),					
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),					
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),


			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.value)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.type)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.data), nameof<PlanReferenceData>(x => x.blueprintFieldId)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.data), nameof<PlanReferenceData>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.sourceType)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.description)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.code)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.dataType)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.definition), nameof<ReferenceDefinition>(x => x.fields), nameof<ReferenceField>(x => x.value)].join('.'),


            ...PlanEditorEntityResolver.planDescriptionTemplateFields(),

			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.action)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

			...PlanEditorEntityResolver.planEditorPlanBlueprintForPlanLookupFields(),

		]
	}

	public static blueprintLookupFields(prefix?: string): string[] {
		return [
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.enabled)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.message)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.details)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.ordinal)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.description)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.canEditDescriptionTemplates)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSourcesEnabled)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.id)].join('.'),			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.minMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.id)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.description)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.extendedDescription)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.ordinal)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.title)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.hasMultiplicity)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.hasCommentField)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.min)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.max)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.placeholder)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.tableView)].join('.'),
			(prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BlueprintFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.transposedTableView)].join('.'),

            //FIELDS
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.id)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.ordinal)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.semantics)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.textValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.dateValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.booleanValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.numberValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.includeInExport)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.validations)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.target)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.textValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.dateValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.booleanValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.numberValue)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.label)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.fieldType)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.multipleSelect)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.label)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.value)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldLabelAndMultiplicityData>(x => x.multipleSelect)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldExternalDatasetData>(x => x.type)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.maxFileSizeInMB)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.label)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.value)].join('.'),
            (prefix ? prefix + '.' : '') + [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSystemData>(x => x.systemFieldType)].join('.'),
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanEditorEntityResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const fieldId = route.paramMap.get('fieldId');
		const descriptionId = route.paramMap.get('descriptionId');
		if (id != null) {
			return this.planService.getSingle(Guid.parse(id), fields).pipe(tap(item => {
				if (fieldId) {
					this.breadcrumbService.addExcludedParam(fieldId, true);
					this.breadcrumbService.addExcludedParam('f', true);
					this.breadcrumbService.addExcludedParam('annotation', true);
				}
                if(descriptionId){
					this.breadcrumbService.addExcludedParam(descriptionId, true);
					this.breadcrumbService.addExcludedParam('d', true);
					// this.breadcrumbService.addExcludedParam('finalize', true);
                }
                if(item) {
                    this.breadcrumbService.addIdResolvedValue(id, item.label);
                    const planCopy: Plan = {...item, descriptions: []}; //add plan info to desc w/o requesting it from the lookup twice
                    item.descriptions = item.descriptions ?? [];
                    item.descriptions.forEach((desc) => desc.plan = planCopy)
                }
			}), takeUntil(this._destroyed));
		}
	}
}
