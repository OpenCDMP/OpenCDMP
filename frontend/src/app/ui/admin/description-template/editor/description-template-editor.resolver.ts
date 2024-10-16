import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplate, DescriptionTemplateBaseFieldData, DescriptionTemplateDefaultValue, DescriptionTemplateDefinition, DescriptionTemplateExternalDatasetData, DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateMultiplicity, DescriptionTemplatePage, DescriptionTemplateReferenceTypeData, DescriptionTemplateRule, DescriptionTemplateSection, DescriptionTemplateSelectData, DescriptionTemplateSelectOption, DescriptionTemplateUploadData, DescriptionTemplateUploadOption, UserDescriptionTemplate } from '@app/core/model/description-template/description-template';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
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

			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.EditDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.DeleteDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CloneDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CreateNewVersionDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ImportDescriptionTemplate].join('.'),
			[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ExportDescriptionTemplate].join('.'),

			[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.title)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.title)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.description)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.sections)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.title)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.description)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.extendedDescription)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.additionalInformation)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.hasCommentField)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.hasMultiplicity)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.multiplicity), nameof<DescriptionTemplateMultiplicity>(x => x.min)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.multiplicity), nameof<DescriptionTemplateMultiplicity>(x => x.max)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.multiplicity), nameof<DescriptionTemplateMultiplicity>(x => x.placeholder)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.multiplicity), nameof<DescriptionTemplateMultiplicity>(x => x.tableView)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.ordinal)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.semantics)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.defaultValue), nameof<DescriptionTemplateDefaultValue>(x => x.textValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.defaultValue), nameof<DescriptionTemplateDefaultValue>(x => x.dateValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.defaultValue), nameof<DescriptionTemplateDefaultValue>(x => x.booleanValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.includeInExport)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.validations)].join('.'),

			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.visibilityRules), nameof<DescriptionTemplateRule>(x => x.target)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.visibilityRules), nameof<DescriptionTemplateRule>(x => x.textValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.visibilityRules), nameof<DescriptionTemplateRule>(x => x.dateValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.visibilityRules), nameof<DescriptionTemplateRule>(x => x.booleanValue)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateBaseFieldData>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateBaseFieldData>(x => x.fieldType)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateSelectData>(x => x.multipleSelect)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateSelectData>(x => x.options), nameof<DescriptionTemplateSelectOption>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateSelectData>(x => x.options), nameof<DescriptionTemplateSelectOption>(x => x.value)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateLabelAndMultiplicityData>(x => x.multipleSelect)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateExternalDatasetData>(x => x.type)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateUploadData>(x => x.maxFileSizeInMB)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateUploadData>(x => x.types), nameof<DescriptionTemplateUploadOption>(x => x.label)].join('.'),
			[nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.pages), nameof<DescriptionTemplatePage>(x => x.sections), nameof<DescriptionTemplateSection>(x => x.fieldSets), nameof<DescriptionTemplateFieldSet>(x => x.fields), nameof<DescriptionTemplateField>(x => x.data), nameof<DescriptionTemplateUploadData>(x => x.types), nameof<DescriptionTemplateUploadOption>(x => x.value)].join('.'),

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
