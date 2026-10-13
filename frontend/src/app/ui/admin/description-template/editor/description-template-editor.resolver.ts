import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplate, BaseFieldData, FieldDefaultValue, DescriptionTemplateDefinition, FieldExternalDatasetData, BaseField, BaseFieldSet, FieldLabelAndMultiplicityData, Multiplicity, FieldReferenceTypeData, FieldRule, BaseSection, FieldSelectData, FieldSelectOption, SemanticValue, FieldUploadData, FieldUploadOption, UserDescriptionTemplate, FieldSystemData } from '@app/core/model/description-template/description-template';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { PluginConfiguration, PluginConfigurationField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { User } from '@app/core/model/user/user';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { map, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class DescriptionTemplateEditorResolver extends BaseEditorResolver {

	constructor(private descriptionTemplateService: DescriptionTemplateService, private breadcrumbService: BreadcrumbService) {
		super();
	}

    public static baseLookupFields(): string[] {
        return [
            ...BaseEditorResolver.lookupFields(),
            nameof<DescriptionTemplate>(x => x.id),
            nameof<DescriptionTemplate>(x => x.label),
            nameof<DescriptionTemplate>(x => x.version),
            nameof<DescriptionTemplate>(x => x.versionStatus),
            nameof<DescriptionTemplate>(x => x.groupId),
            nameof<DescriptionTemplate>(x => x.isActive),
        ]
    }

    public static definitionLookupFields(): string[]{
        return [

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginCode)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.pluginType)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.code)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.textValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.secret)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.secretSet)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.extension)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.mimeType)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.fullName)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pluginConfigurations), nameof<PluginConfiguration>(x => x.fields), nameof<PluginConfigurationField>(x => x.fileValue), nameof<StorageFile>(x => x.name)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.name)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.extension)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.fullName)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.mimeType)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.title)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition),  nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition),  nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.title)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition),  nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.description)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition),  nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.sections)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition),  nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.title)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.description)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.extendedDescription)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.additionalInformation)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.hasCommentField)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.hasMultiplicity)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.min)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.max)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.placeholder)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.tableView)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.transposedTableView)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.semantics)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.textValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.dateValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.booleanValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue), nameof<FieldDefaultValue>(x => x.numberValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.includeInExport)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.validations)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.target)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.textValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.dateValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.booleanValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.numberValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.fieldType)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.multipleSelect)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.value)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.semantics)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.semantics)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.semantics), nameof<SemanticValue>(x => x.key)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.semantics), nameof<SemanticValue>(x => x.value)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.exportsNothing)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldLabelAndMultiplicityData>(x => x.multipleSelect)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldExternalDatasetData>(x => x.type)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.maxFileSizeInMB)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldUploadData>(x => x.types), nameof<FieldUploadOption>(x => x.value)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSystemData>(x => x.systemFieldType)].join('.'),
        ]
    }

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<DescriptionTemplate>(x => x.id),
			nameof<DescriptionTemplate>(x => x.label),
			nameof<DescriptionTemplate>(x => x.code),
			nameof<DescriptionTemplate>(x => x.status),
			nameof<DescriptionTemplate>(x => x.description),
			nameof<DescriptionTemplate>(x => x.language),
			nameof<DescriptionTemplate>(x => x.status),
			nameof<DescriptionTemplate>(x => x.version),
			nameof<DescriptionTemplate>(x => x.versionStatus),
			nameof<DescriptionTemplate>(x => x.groupId),
			nameof<DescriptionTemplate>(x => x.updatedAt),
			nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant),

			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.EditDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.DeleteDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CloneDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CreateNewVersionDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ImportDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ExportDescriptionTemplate].join('.'),

			[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),

            ...this.definitionLookupFields(),

			[nameof<DescriptionTemplate>(x => x.users), nameof<UserDescriptionTemplate>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.users), nameof<UserDescriptionTemplate>(x => x.user), nameof<User>(x => x.id),].join('.'),
			[nameof<DescriptionTemplate>(x => x.users), nameof<UserDescriptionTemplate>(x => x.user), nameof<User>(x => x.name),].join('.'),
			[nameof<DescriptionTemplate>(x => x.users), nameof<UserDescriptionTemplate>(x => x.role),].join('.'),
			[nameof<DescriptionTemplate>(x => x.users), nameof<UserDescriptionTemplate>(x => x.isActive),].join('.'),


			nameof<DescriptionTemplate>(x => x.createdAt),
			nameof<DescriptionTemplate>(x => x.hash),
			nameof<DescriptionTemplate>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fieldSets = [
			...DescriptionTemplateEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');
		const cloneid = route.paramMap.get('cloneid');
		const newversion = route.paramMap.get('newversionid');
		if (id != null) {
			return this.descriptionTemplateService.getSingle(Guid.parse(id), fieldSets).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		} else if (cloneid != null) {
			return this.descriptionTemplateService.clone(Guid.parse(cloneid), fieldSets)
				.pipe(
					tap(x => this.breadcrumbService.addIdResolvedValue(cloneid, x.label)), 
					takeUntil(this._destroyed),
					map((descriptionTemplate: DescriptionTemplate) => {
						descriptionTemplate.id = null;
						descriptionTemplate.hash = null;
						descriptionTemplate.code = null;
						descriptionTemplate.isActive = IsActive.Active;
                        descriptionTemplate.belongsToCurrentTenant = true;
						return descriptionTemplate;
				}));
		} else if (newversion != null) {
			return this.descriptionTemplateService.getSingle(Guid.parse(newversion), fieldSets).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(newversion, x.label)), takeUntil(this._destroyed));
		}
	}
}
