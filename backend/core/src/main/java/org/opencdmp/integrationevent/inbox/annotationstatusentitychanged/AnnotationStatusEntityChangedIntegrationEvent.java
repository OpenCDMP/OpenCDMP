package org.opencdmp.integrationevent.inbox.annotationstatusentitychanged;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.annotation.AnnotationEntityType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.TrackedEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AnnotationStatusEntityChangedIntegrationEvent extends TrackedEvent {

    private UUID id;
    public static final String _id = "id";

    private UUID annotationId;
    public static final String _annotationId = "annotationId";

    private UUID statusId;
    public static final String _statusId = "statusId";

    private UUID subjectId;
    public static final String _subjectId = "subjectId";

    private UUID entityId;
    public static final String _entityId = "entityId";

    private AnnotationEntityType entityType;
    public static final String _entityType = "entityType";

    private String anchor;
    public static final String _anchor = "anchor";

    private String statusLabel;
    public static final String _statusLabel = "statusLabel";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(UUID annotationId) {
        this.annotationId = annotationId;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public AnnotationEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(AnnotationEntityType entityType) {
        this.entityType = entityType;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    @Component(AnnotationStatusEntityChangedIntegrationEventValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AnnotationStatusEntityChangedIntegrationEventValidator extends BaseValidator<AnnotationStatusEntityChangedIntegrationEvent> {

        public static final String ValidatorName = "AnnotationStatusEntityChangedIntegrationEventValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected AnnotationStatusEntityChangedIntegrationEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<AnnotationStatusEntityChangedIntegrationEvent> modelClass() {
            return AnnotationStatusEntityChangedIntegrationEvent.class;
        }

        @Override
        protected List<Specification> specifications(AnnotationStatusEntityChangedIntegrationEvent item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getAnnotationId()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._annotationId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._annotationId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getStatusId()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._statusId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._statusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getSubjectId()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._subjectId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._subjectId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._entityId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getEntityType()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._entityType).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._entityType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getAnchor()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._anchor).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._anchor}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getStatusLabel()))
                            .failOn(AnnotationStatusEntityChangedIntegrationEvent._statusLabel).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationStatusEntityChangedIntegrationEvent._statusLabel}, LocaleContextHolder.getLocale()))
                    );
        }
    }
}
