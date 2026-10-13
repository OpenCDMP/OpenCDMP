import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, RootSection, PlanCreationConsent } from '@app/core/model/plan-blueprint/plan-blueprint';
import { BaseFieldData, FieldDefaultValue, Multiplicity, FieldRule, BaseFieldSet, BaseField, FieldExternalDatasetData, FieldLabelAndMultiplicityData, FieldReferenceTypeData, FieldSelectData, FieldSelectOption, SemanticValue, FieldSystemData, FieldUploadData, FieldUploadOption, BaseSection } from '@app/core/model/description-template/description-template';
import { PluginConfiguration, PluginConfigurationField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { map, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';

@Injectable()
export class PlanBlueprintEditorResolver extends BaseEditorResolver {

	constructor(private planBlueprintService: PlanBlueprintService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<PlanBlueprint>(x => x.id),
			nameof<PlanBlueprint>(x => x.label),
			nameof<PlanBlueprint>(x => x.description),
			nameof<PlanBlueprint>(x => x.code),
			nameof<PlanBlueprint>(x => x.status),
			nameof<PlanBlueprint>(x => x.version),
			nameof<PlanBlueprint>(x => x.versionStatus),
			nameof<PlanBlueprint>(x => x.groupId),

			[nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.name)].join('.'),
			

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginCode)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginType)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.code)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.textValue)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.secret)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.secretSet)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.extension)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.mimeType)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.fullName)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.name)].join('.'),
			
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.name)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.extension)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.fullName)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.mimeType)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),

            //ROOT SECTION
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.description)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.ordinal)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.canEditDescriptionTemplates)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.minMultiplicity)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.id)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.label)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.code)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.prefillingSourcesEnabled)].join('.'),


            //FIELD SETS
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.id)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.ordinal)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.title)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.description)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.extendedDescription)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.additionalInformation)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.hasCommentField)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.hasMultiplicity)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.min)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.max)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.placeholder)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.tableView)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.transposedTableView)].join('.'),

            //FIELDS
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.id)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.ordinal)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.semantics)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.textValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.dateValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.booleanValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.numberValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.includeInExport)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.validations)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.target)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.textValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.dateValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.booleanValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.numberValue)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.label)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.fieldType)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.multipleSelect)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.label)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.value)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.semantics)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.semantics)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.semantics), nameof<SemanticValue>(x => x.key)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.semantics), nameof<SemanticValue>(x => x.value)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.exportsNothing)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldLabelAndMultiplicityData>(x => x.multipleSelect)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldExternalDatasetData>(x => x.type)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.maxFileSizeInMB)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.label)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.value)].join('.'),
            [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSystemData>(x => x.systemFieldType)].join('.'),

			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.enabled)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.message)].join('.'),
			[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.planCreationConsent), nameof<PlanCreationConsent>(x => x.details)].join('.'),

			nameof<PlanBlueprint>(x => x.createdAt),
			nameof<PlanBlueprint>(x => x.hash),
			nameof<PlanBlueprint>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...PlanBlueprintEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const cloneid = route.paramMap.get('cloneid');
		const newversion = route.paramMap.get("newversionid");
		if (id != null) {
			return this.planBlueprintService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		} else if (cloneid != null) {
			return this.planBlueprintService.clone(Guid.parse(cloneid), fields)
				.pipe(
					tap(x => this.breadcrumbService.addIdResolvedValue(cloneid, x.label)), 
					takeUntil(this._destroyed),
					map((blueprint: PlanBlueprint) => {
                        return {
                            ...blueprint,
                            id: null,
                            hash: null,
                            code: null,
                            isActive: IsActive.Active,
                            belongsToCurrentTenant: true
                        }
				}));
		} else if (newversion != null) {
			return this.planBlueprintService.getSingle(Guid.parse(newversion), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(newversion, x.label)), takeUntil(this._destroyed));
		}
	}
}
