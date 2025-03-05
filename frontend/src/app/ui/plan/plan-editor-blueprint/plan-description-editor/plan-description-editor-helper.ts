import { FormGroup } from "@angular/forms";
import { DescriptionStatusPermission } from "@app/core/common/enum/description-status-permission.enum";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { DescriptionStatus, DescriptionStatusDefinition } from "@app/core/model/description-status/description-status";
import { DescriptionTemplate} from "@app/core/model/description-template/description-template";
import { Description, DescriptionExternalIdentifier, DescriptionField, DescriptionPropertyDefinition, DescriptionPropertyDefinitionFieldSet, DescriptionPropertyDefinitionFieldSetItem, DescriptionPropertyDefinitionFieldSetItemPersist, DescriptionPropertyDefinitionFieldSetPersist, DescriptionPropertyDefinitionPersist, DescriptionReference, DescriptionReferenceData, DescriptionTag } from "@app/core/model/description/description";
import { PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection, DescriptionTemplatesInSection } from "@app/core/model/plan-blueprint/plan-blueprint";
import { PlanStatus } from "@app/core/model/plan-status/plan-status";
import { Plan, PlanDescriptionTemplate, PlanUser } from "@app/core/model/plan/plan";
import { PrefillingSource } from "@app/core/model/prefilling-source/prefilling-source";
import { ReferenceType } from "@app/core/model/reference-type/reference-type";
import { Reference } from "@app/core/model/reference/reference";
import { Tag } from "@app/core/model/tag/tag";
import { DescriptionEditorForm, PropertiesFormGroup } from "@app/ui/description/editor/description-editor.model";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { nameof } from "ts-simple-nameof";


export const DescriptionEditorHelper = {
    BaseDescriptionLookupFields,
    DescriptionTemplateInDescriptionLookupFields,
    DescriptionTemplateLookupFields,
    DescriptionLookupFields,
    DescriptionPlanLookupFields,
    getDescriptionErrors
}

function  DescriptionTemplateInDescriptionLookupFields(): string[] {
    return ['DescriptionEditorDescriptionTemplateForDescriptionLookupFields'];
}

function BaseDescriptionLookupFields(): string[] {
    return [
    ...BaseEditorResolver.lookupFields(),
    nameof<Description>(x => x.id),
    nameof<Description>(x => x.label),
    nameof<Description>(x => x.description),
    [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
    [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
    [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
    [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),

    [nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
    [nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
    [nameof<Description>(x => x.authorizationFlags), AppPermission.FinalizeDescription].join('.'),
    [nameof<Description>(x => x.authorizationFlags), AppPermission.AnnotateDescription].join('.'),

    [nameof<Description>(x => x.statusAuthorizationFlags), DescriptionStatusPermission.Edit].join('.'),

    [nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
    [nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
    [nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.comment)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.ordinal)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.textValue)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.textListValue)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.dateValue)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.booleanValue)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.identifier)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.type)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.id)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.label)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.reference)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.references), nameof<Reference>(x => x.isActive)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.id)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.label)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.isActive)].join('.'),
    [nameof<Description>(x => x.properties), nameof<DescriptionPropertyDefinition>(x => x.fieldSets), nameof<DescriptionPropertyDefinitionFieldSet>(x => x.items), nameof<DescriptionPropertyDefinitionFieldSetItem>(x => x.fields), nameof<DescriptionField>(x => x.tags), nameof<Tag>(x => x.hash)].join('.'),

    [nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.id),].join('.'),
    [nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.tag), nameof<Tag>(x => x.label)].join('.'),
    [nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.isActive)].join('.'),

    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.fieldId)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.ordinal)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.sourceType)].join('.'),
    [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.isActive)].join('.'),

    nameof<Description>(x => x.createdAt),
    nameof<Description>(x => x.hash),
    nameof<Description>(x => x.isActive),

    [nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.id)].join('.'),
    [nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.name)].join('.'),
    [nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
    [nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.action)].join('.'),
]}

function DescriptionTemplateLookupFields(): string[] {
    return ['DescriptionEditorDescriptionTemplateLookupFields'];
} 

function DescriptionLookupFields(): string[] {
    return [
        ...DescriptionEditorHelper.BaseDescriptionLookupFields(),
        ...DescriptionEditorHelper.DescriptionPlanLookupFields(nameof<Description>(x => x.plan)),
        ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()
    ]
}

function DescriptionPlanLookupFields(prefix?: string): string[] {
    return [
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.label)].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),			
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.isActive)].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.isActive)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.maxMultiplicity)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSourcesEnabled)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.prefillingSources), nameof<PrefillingSource>(x => x.id)].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.currentDescriptionTemplate), nameof<DescriptionTemplate>(x => x.version)].join('.'),

        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
        (prefix ? prefix + '.' : '') + [nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
    ]
}

function getDescriptionErrors(params: {formGroup: FormGroup<DescriptionEditorForm>, htmlMapping: (string) => string}): string[]{
    const {formGroup, htmlMapping} = params;
    const errorFields = [];
    const fieldSets = (formGroup.controls.properties as PropertiesFormGroup).controls.fieldSets;
    Object.keys(fieldSets.controls).forEach(fieldSetId => {
        if(fieldSets.get(fieldSetId)?.invalid) {
            errorFields.push(fieldSetId);
        } 
    });
    return errorFields.map((id) => document.getElementById(htmlMapping(id))?.textContent).filter((x) => x != null && x != undefined);
}

