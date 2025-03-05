package org.opencdmp.model.persist;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.types.planblueprint.*;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planproperties.PlanPropertiesPersist;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanDescriptionTemplateQuery;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlanPersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private UUID statusId;

    public static final String _statusId = "statusId";

    private PlanPropertiesPersist properties;

    public static final String _properties = "properties";

    private String description;
    public static final String _description = "description";

    private String language;
    public static final String _language = "language";

    private UUID blueprint;

    public static final String _blueprint = "blueprint";

    private PlanAccessType accessType;
    public static final String _accessType = "accessType";

    private List<PlanDescriptionTemplatePersist> descriptionTemplates;

    public static final String _descriptionTemplates = "descriptionTemplates";

    private List<PlanUserPersist> users;
    public static final String _users = "users";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public PlanPropertiesPersist getProperties() {
        return this.properties;
    }

    public void setProperties(PlanPropertiesPersist properties) {
        this.properties = properties;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getBlueprint() {
        return this.blueprint;
    }

    public void setBlueprint(UUID blueprint) {
        this.blueprint = blueprint;
    }

    public PlanAccessType getAccessType() {
        return this.accessType;
    }

    public void setAccessType(PlanAccessType accessType) {
        this.accessType = accessType;
    }

    public List<PlanDescriptionTemplatePersist> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }

    public void setDescriptionTemplates(List<PlanDescriptionTemplatePersist> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public List<PlanUserPersist> getUsers() {
        return this.users;
    }

    public void setUsers(List<PlanUserPersist> users) {
        this.users = users;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(PlanPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanPersistValidator extends BaseValidator<PlanPersist> {

        public static final String ValidatorName = "PlanPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;
        private final TenantEntityManager entityManager;
        private final XmlHandlingService xmlHandlingService;
        private final QueryFactory queryFactory;

        protected PlanPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory, TenantEntityManager entityManager, XmlHandlingService xmlHandlingService, QueryFactory queryFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
            this.entityManager = entityManager;
            this.xmlHandlingService = xmlHandlingService;
            this.queryFactory = queryFactory;
        }

        @Override
        protected Class<PlanPersist> modelClass() {
            return PlanPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanPersist item) {
            PlanBlueprintEntity planBlueprintEntity = null;
            PlanStatusEntity statusEntity = null;
            try {
                planBlueprintEntity = this.isValidGuid(item.getBlueprint()) ? this.entityManager.find(PlanBlueprintEntity.class, item.getBlueprint(), true) : null;
                statusEntity = this.isValidGuid(item.getStatusId()) ? this.entityManager.find(PlanStatusEntity.class, item.getStatusId(), true) : null;
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            }
            PlanBlueprintEntity finalPlanBlueprintEntity = planBlueprintEntity;
            DefinitionEntity definition = planBlueprintEntity == null ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, planBlueprintEntity.getDefinition());

            String languageFieldLabel = null;
            String accessFieldLabel = null;

            if (definition != null){
                languageFieldLabel = this.getSystemFieldLabel(definition, PlanBlueprintSystemFieldType.Language);
                accessFieldLabel = this.getSystemFieldLabel(definition, PlanBlueprintSystemFieldType.AccessRights);
            }

            PlanStatusEntity finalStatusEntity = statusEntity;
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(PlanPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(PlanPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(PlanPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), PlanEntity._labelLength))
                            .failOn(PlanPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{PlanPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> !this.isEmpty(item.getDescription()))
                            .failOn(PlanPersist._description).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._description}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> !this.isNull(item.getStatusId()))
                            .failOn(PlanPersist._statusId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._statusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> this.isValidGuid(item.getBlueprint()))
                            .failOn(PlanPersist._blueprint).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._blueprint}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> !this.isNull(item.getProperties()))
                            .failOn(PlanPersist._properties).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._properties}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> this.isDescriptionTemplateMultiplicityValid(finalPlanBlueprintEntity, item.getId()))
                            .failOn(PlanPersist._descriptionTemplates).failWith(this.messageSource.getMessage("Validation.InvalidDescriptionTemplateMultiplicityOnPlan", new Object[]{PlanPersist._descriptionTemplates}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getProperties()) && finalStatusEntity != null)
                            .on(PlanPersist._properties)
                            .over(item.getProperties())
                            .using(() -> this.validatorFactory.validator(PlanPropertiesPersist.PlanPropertiesPersistValidator.class).setStatus(finalStatusEntity.getInternalStatus()).withDefinition(definition)),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> !this.isNull(item.getLanguage()))
                            .failOn(PlanPersist._language).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{languageFieldLabel != null ? languageFieldLabel :  PlanBlueprintSystemFieldType.Language.name()}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> !this.isNull(item.getAccessType()))
                            .failOn(PlanPersist._accessType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{accessFieldLabel != null ? accessFieldLabel :  PlanBlueprintSystemFieldType.AccessRights.name()}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == PlanStatus.Finalized)
                            .must(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .failOn(PlanPersist._descriptionTemplates).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanPersist._descriptionTemplates}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .on(PlanPersist._descriptionTemplates)
                            .over(item.getDescriptionTemplates())
                            .using((itm) -> this.validatorFactory.validator(PlanDescriptionTemplatePersist.PlanDescriptionTemplatePersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .on(PlanPersist._users)
                            .over(item.getUsers())
                            .using((itm) -> this.validatorFactory.validator(PlanUserPersist.PlanUserPersistValidator.class))
            );
        }

        private boolean isDescriptionTemplateMultiplicityValid(PlanBlueprintEntity planBlueprintEntity, UUID planId){
            org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
            if (definition == null || this.isListNullOrEmpty(definition.getSections())) return true;

            List<PlanDescriptionTemplateEntity> planDescriptionTemplateEntities = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).planIds(planId).collect();
            List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planId).isActive(IsActive.Active).collect();

            for (SectionEntity section: definition.getSections()) {
                if (section.getHasTemplates() && !this.isListNullOrEmpty(section.getDescriptionTemplates())){

                    for (BlueprintDescriptionTemplateEntity sectionDescriptionTemplate: section.getDescriptionTemplates()) {
                        if (sectionDescriptionTemplate.getMaxMultiplicity() == null && sectionDescriptionTemplate.getMinMultiplicity() == null ) continue;

                        int descriptionsCount = 0;
                        for (PlanDescriptionTemplateEntity planDescriptionTemplate: planDescriptionTemplateEntities) {

                            if(planDescriptionTemplate.getSectionId().equals(section.getId())) {
                                for (DescriptionEntity description: descriptionEntities){
                                    if (sectionDescriptionTemplate.getDescriptionTemplateGroupId().equals(planDescriptionTemplate.getDescriptionTemplateGroupId())) {
                                        if (description.getPlanDescriptionTemplateId().equals(planDescriptionTemplate.getId()) && planDescriptionTemplate.getSectionId().equals(section.getId()))
                                            descriptionsCount++;
                                    }
                                }
                            }
                        }
                        if (sectionDescriptionTemplate.getMinMultiplicity() != null && sectionDescriptionTemplate.getMinMultiplicity() > descriptionsCount) return false;
                        if (sectionDescriptionTemplate.getMaxMultiplicity() != null && sectionDescriptionTemplate.getMaxMultiplicity() < descriptionsCount) return false;

                    }

                }

            }
            return true;
        }

        private String getSystemFieldLabel(DefinitionEntity definition, PlanBlueprintSystemFieldType type){
            if (this.isListNullOrEmpty(definition.getSections())) return null;

            for (SectionEntity section: definition.getSections()) {
                if (!this.isListNullOrEmpty(section.getFields())) {
                    List<FieldEntity> fields = section.getFields().stream().filter(x -> x.getCategory().equals(PlanBlueprintFieldCategory.System)).collect(Collectors.toList());
                    if (!this.isListNullOrEmpty(fields)){

                        List<SystemFieldEntity> systemFields = (List<SystemFieldEntity>)(List<?>) fields;
                        if (!this.isListNullOrEmpty(systemFields)){
                            List<SystemFieldEntity> contactSystemFields = systemFields.stream().filter(x -> x.getType().equals(type) && x.isRequired()).collect(Collectors.toList());
                            if(!this.isListNullOrEmpty(contactSystemFields)) {
                                return contactSystemFields.getFirst().getLabel() != null ? contactSystemFields.getFirst().getLabel() : type.name();
                            }
                        }

                    }
                }
            }
            return null;
        }
    }

}
