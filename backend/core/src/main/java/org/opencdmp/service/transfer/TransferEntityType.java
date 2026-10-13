package org.opencdmp.service.transfer;

import org.opencdmp.data.*;

/**
 * Entity types included in a tenant transfer bundle.
 * Declaration order is the import dependency order: every type only references
 * types declared before it (or itself), so applying the bundle top-to-bottom is safe.
 */
public enum TransferEntityType {
    ReferenceType(ReferenceTypeEntity.class, ReferenceTypeEntity[].class),
    PrefillingSource(PrefillingSourceEntity.class, PrefillingSourceEntity[].class),
    DescriptionTemplateType(DescriptionTemplateTypeEntity.class, DescriptionTemplateTypeEntity[].class),
    PlanBlueprintType(PlanBlueprintTypeEntity.class, PlanBlueprintTypeEntity[].class),
    PlanStatus(PlanStatusEntity.class, PlanStatusEntity[].class),
    DescriptionStatus(DescriptionStatusEntity.class, DescriptionStatusEntity[].class),
    PlanWorkflow(PlanWorkflowEntity.class, PlanWorkflowEntity[].class),
    DescriptionWorkflow(DescriptionWorkflowEntity.class, DescriptionWorkflowEntity[].class),
    Language(LanguageEntity.class, LanguageEntity[].class),
    SupportiveMaterial(SupportiveMaterialEntity.class, SupportiveMaterialEntity[].class),
    User(UserEntity.class, UserEntity[].class),
    UserContactInfo(UserContactInfoEntity.class, UserContactInfoEntity[].class),
    StorageFile(StorageFileEntity.class, StorageFileEntity[].class),
    DescriptionTemplate(DescriptionTemplateEntity.class, DescriptionTemplateEntity[].class),
    PlanBlueprint(PlanBlueprintEntity.class, PlanBlueprintEntity[].class),
    Tag(TagEntity.class, TagEntity[].class),
    Reference(ReferenceEntity.class, ReferenceEntity[].class),
    Plan(PlanEntity.class, PlanEntity[].class),
    PlanUser(PlanUserEntity.class, PlanUserEntity[].class),
    PlanDescriptionTemplate(PlanDescriptionTemplateEntity.class, PlanDescriptionTemplateEntity[].class),
    PlanReference(PlanReferenceEntity.class, PlanReferenceEntity[].class),
    Description(DescriptionEntity.class, DescriptionEntity[].class),
    DescriptionReference(DescriptionReferenceEntity.class, DescriptionReferenceEntity[].class),
    DescriptionTag(DescriptionTagEntity.class, DescriptionTagEntity[].class),
    EntityDoi(EntityDoiEntity.class, EntityDoiEntity[].class),
    UserDescriptionTemplate(UserDescriptionTemplateEntity.class, UserDescriptionTemplateEntity[].class);

    private final Class<?> entityClass;
    private final Class<?> arrayClass;

    TransferEntityType(Class<?> entityClass, Class<?> arrayClass) {
        this.entityClass = entityClass;
        this.arrayClass = arrayClass;
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public Class<?> getArrayClass() {
        return this.arrayClass;
    }

    public String bundleEntryName() {
        return "entities/" + this.name() + ".json";
    }
}
