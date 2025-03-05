package org.opencdmp.model.persist;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.BlueprintDescriptionTemplateEntity;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionPersist;
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

public class DescriptionPersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private UUID planId;

    public static final String _planId = "planId";

    private UUID planDescriptionTemplateId;

    public static final String _planDescriptionTemplateId = "planDescriptionTemplateId";

    private UUID descriptionTemplateId;

    public static final String _descriptionTemplateId = "descriptionTemplateId";

    private UUID statusId;

    public static final String _statusId = "statusId";

    private String description;

    private PropertyDefinitionPersist properties;

    public static final String _properties = "properties";

    private List<String> tags;

    public static final String _tags = "tags";


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

    public UUID getPlanId() {
        return this.planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getPlanDescriptionTemplateId() {
        return this.planDescriptionTemplateId;
    }

    public void setPlanDescriptionTemplateId(UUID planDescriptionTemplateId) {
        this.planDescriptionTemplateId = planDescriptionTemplateId;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PropertyDefinitionPersist getProperties() {
        return this.properties;
    }

    public void setProperties(PropertyDefinitionPersist properties) {
        this.properties = properties;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public UUID getDescriptionTemplateId() {
        return this.descriptionTemplateId;
    }

    public void setDescriptionTemplateId(UUID descriptionTemplateId) {
        this.descriptionTemplateId = descriptionTemplateId;
    }

    @Component(DescriptionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionPersistValidator extends BaseValidator<DescriptionPersist> {

        public static final String ValidatorName = "DescriptionPersistValidator";

        private final MessageSource messageSource;

        private final TenantEntityManager entityManager;
        private final XmlHandlingService xmlHandlingService;
        private final QueryFactory queryFactory;
        private final ValidatorFactory validatorFactory;


        protected DescriptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, TenantEntityManager entityManager, XmlHandlingService xmlHandlingService, QueryFactory queryFactory, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
	        this.entityManager = entityManager;
	        this.xmlHandlingService = xmlHandlingService;
            this.queryFactory = queryFactory;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionPersist> modelClass() {
            return DescriptionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionPersist item) {
	        DescriptionTemplateEntity descriptionTemplate  = null;
            PlanEntity planEntity = null;
            PlanBlueprintEntity planBlueprintEntity = null;
            DescriptionStatusEntity statusEntity = null;
            try {
		        descriptionTemplate = this.isValidGuid(item.getDescriptionTemplateId()) ? this.entityManager.find(DescriptionTemplateEntity.class, item.getDescriptionTemplateId(), true) : null;
                planEntity = this.isValidGuid(item.getPlanId()) ? this.entityManager.find(PlanEntity.class, item.getPlanId(), true) : null;
                planBlueprintEntity = planEntity == null ? null : this.entityManager.find(PlanBlueprintEntity.class, planEntity.getBlueprintId());
                statusEntity = this.isValidGuid(item.getStatusId()) ? this.entityManager.find(DescriptionStatusEntity.class, item.getStatusId(), true) : null;

            } catch (InvalidApplicationException e) {
		        throw new RuntimeException(e);
	        }
	        DefinitionEntity definition = descriptionTemplate == null ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, descriptionTemplate.getDefinition());
            PlanBlueprintEntity finalPlanBlueprintEntity = planBlueprintEntity;
            DescriptionStatusEntity finalStatusEntity = statusEntity;
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(DescriptionPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(DescriptionPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(DescriptionPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), DescriptionEntity._labelLength))
                            .failOn(DescriptionPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{DescriptionPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(DescriptionPersist._planId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._planId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionTemplateId()))
                            .failOn(DescriptionPersist._descriptionTemplateId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._descriptionTemplateId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanDescriptionTemplateId()))
                            .failOn(DescriptionPersist._planDescriptionTemplateId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._planDescriptionTemplateId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidGuid(item.getStatusId()))
                            .failOn(DescriptionPersist._statusId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._statusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == DescriptionStatus.Finalized)
                            .must(() -> !this.isNull(item.getProperties()))
                            .failOn(DescriptionPersist._properties).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionPersist._properties}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == DescriptionStatus.Finalized)
                            .must(() -> this.isDescriptionTemplateMaxMultiplicityValid(finalPlanBlueprintEntity, item.getPlanId(), item.getPlanDescriptionTemplateId(), this.isValidGuid(item.getId())))
                            .failOn(DescriptionPersist._descriptionTemplateId).failWith(this.messageSource.getMessage("Validation.InvalidDescriptionTemplateMultiplicity", new Object[]{DescriptionPersist._descriptionTemplateId}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> finalStatusEntity != null && finalStatusEntity.getInternalStatus() != null && finalStatusEntity.getInternalStatus() == DescriptionStatus.Finalized)
                            .on(DescriptionPersist._properties)
                            .over(item.getProperties())
                            .using(() -> this.validatorFactory.validator(PropertyDefinitionPersist.PropertyDefinitionPersistValidator.class).setStatus(finalStatusEntity.getInternalStatus()).withDefinition(definition).setVisibilityService(definition, item.getProperties()))
//                    this.navSpec()
//                            .iff(() -> !this.isNull(item.getTags()))
//                            .on(DescriptionPersist._tags)
//                            .over(item.getTags())
//                            .using((itm) -> this.validatorFactory.validator(TagPersist.TagPersistValidator.class))
                    );
        }

        private boolean isDescriptionTemplateMaxMultiplicityValid(PlanBlueprintEntity planBlueprintEntity, UUID planId, UUID planDescriptionTemplateId, Boolean isUpdate){
            org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
            if (definition == null || this.isListNullOrEmpty(definition.getSections())) return true;

            PlanDescriptionTemplateEntity planDescriptionTemplateEntity = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(planDescriptionTemplateId).isActive(IsActive.Active).planIds(planId).first();
            if (planDescriptionTemplateEntity == null) return true;

            List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planId).planDescriptionTemplateIds(planDescriptionTemplateId).isActive(IsActive.Active).collect();

            for (SectionEntity section: definition.getSections()) {
                if (planDescriptionTemplateEntity.getSectionId().equals(section.getId()) && section.getHasTemplates() && !this.isListNullOrEmpty(section.getDescriptionTemplates())){
                    int descriptionsCount;
                    if (isUpdate) descriptionsCount = -1;
                    else descriptionsCount = 0;

                    for (BlueprintDescriptionTemplateEntity sectionDescriptionTemplate: section.getDescriptionTemplates()) {
                        if (sectionDescriptionTemplate.getDescriptionTemplateGroupId().equals(planDescriptionTemplateEntity.getDescriptionTemplateGroupId())){
                            for (DescriptionEntity description: descriptionEntities){
                                if (description.getPlanDescriptionTemplateId().equals(planDescriptionTemplateEntity.getId())) descriptionsCount++;
                            }
                            if (sectionDescriptionTemplate.getMaxMultiplicity() != null && sectionDescriptionTemplate.getMaxMultiplicity() <= descriptionsCount) return false;
                        }

                    }

                }

            }
            return true;
        }
    }

}
