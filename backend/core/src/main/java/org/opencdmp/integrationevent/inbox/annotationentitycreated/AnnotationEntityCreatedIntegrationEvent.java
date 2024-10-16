package org.opencdmp.integrationevent.inbox.annotationentitycreated;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.annotation.AnnotationEntityType;
import org.opencdmp.commons.enums.annotation.AnnotationProtectionType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.TrackedEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AnnotationEntityCreatedIntegrationEvent extends TrackedEvent {

    private UUID id;
    public static final String _id = "id";
    private UUID entityId;
    public static final String _entityId = "entityId";

    private AnnotationEntityType entityType;
    public static final String _entityType = "entityType";

    private String anchor;
    public static final String _anchor = "anchor";

    private String payload;
    public static final String _payload = "payload";

    private UUID subjectId;
    public static final String _subjectId = "subjectId";

    private UUID threadId;
    public static final String _threadId = "threadId";

    private UUID parentId;
    public static final String _parentId = "parentId";

    private AnnotationProtectionType protectionType;
    public static final String _protectionType = "protectionType";

    private Instant timeStamp;
    public static final String _timeStamp = "timeStamp";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public void setThreadId(UUID threadId) {
        this.threadId = threadId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public AnnotationProtectionType getProtectionType() {
        return protectionType;
    }

    public void setProtectionType(AnnotationProtectionType protectionType) {
        this.protectionType = protectionType;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Component(AnnotationEntityCreatedIntegrationEventValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AnnotationEntityCreatedIntegrationEventValidator extends BaseValidator<AnnotationEntityCreatedIntegrationEvent> {

        public static final String ValidatorName = "AnnotationEntityCreatedIntegrationEventValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected AnnotationEntityCreatedIntegrationEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<AnnotationEntityCreatedIntegrationEvent> modelClass() {
            return AnnotationEntityCreatedIntegrationEvent.class;
        }

        @Override
        protected List<Specification> specifications(AnnotationEntityCreatedIntegrationEvent item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getSubjectId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._subjectId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._subjectId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getEntityId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._entityId).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getEntityId()))
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._entityId).failWith(messageSource.getMessage("validation.invalidid", new Object[]{AnnotationEntityCreatedIntegrationEvent._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getEntityType()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._entityType).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._entityType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPayload()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._payload).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._payload}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getAnchor()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._anchor).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._anchor}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getThreadId()))
                            .must(() -> this.isValidGuid(item.getThreadId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._threadId).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{AnnotationEntityCreatedIntegrationEvent._threadId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getParentId()))
                            .must(() -> this.isValidGuid(item.getParentId()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._parentId).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{AnnotationEntityCreatedIntegrationEvent._parentId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getProtectionType()))
                            .failOn(AnnotationEntityCreatedIntegrationEvent._protectionType).failWith(messageSource.getMessage("Validation_Required", new Object[]{AnnotationEntityCreatedIntegrationEvent._protectionType}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
