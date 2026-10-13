package org.opencdmp.model.persist.evaluation;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EvaluationTargetPersist {

    private EntityType entityType;
    public static final String _entityType = "entityType";

    private UUID entityId;
    public static final String _entityId = "entityId";

    private String label;
    public static final String _label = "label";

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Component(EvaluationTargetPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationTargetPersistValidator extends BaseValidator<EvaluationTargetPersist> {

        public static final String ValidatorName = "EvaluationTargetPersistValidator";

        private final MessageSource messageSource;

        protected EvaluationTargetPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<EvaluationTargetPersist> modelClass() {
            return EvaluationTargetPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationTargetPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getEntityType()))
                            .failOn(EvaluationTargetPersist._entityType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationTargetPersist._entityType}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(EvaluationTargetPersist._entityId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationTargetPersist._entityId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
