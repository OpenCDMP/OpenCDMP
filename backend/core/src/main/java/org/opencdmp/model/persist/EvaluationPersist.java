package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.evaluation.EvaluationDataPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EvaluationPersist {

    private UUID id;
    public static final String _id = "id";

    private EntityType entityType;
    public static final String _entityType = "entityType";

    private UUID entityId;
    public static final String _entityId = "entityId";

    private EvaluationDataPersist data;
    public static final String _data = "data";

    private EvaluationStatus status;
    public static final String _status = "status";

    private UUID createdById;
    public static final String _createdById = "createdById";

    private String hash;
    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public EvaluationDataPersist getData() {
        return data;
    }

    public void setData(EvaluationDataPersist data) {
        this.data = data;
    }

    public EvaluationStatus getStatus() {
        return status;
    }

    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(EvaluationPersist.EvaluationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationPersistValidator extends BaseValidator<EvaluationPersist> {

        public static final String ValidatorName = "EvaluationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected EvaluationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluationPersist> modelClass() {
            return EvaluationPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(EvaluationPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(EvaluationPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(EvaluationPersist._entityId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationPersist._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getData()))
                            .failOn(EvaluationPersist._data).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationPersist._data}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getData()))
                            .on(EvaluationPersist._data)
                            .over(item.getData())
                            .using(() -> this.validatorFactory.validator(EvaluationDataPersist.EvaluationDataPersistValidator.class))
            );
        }
    }

}
