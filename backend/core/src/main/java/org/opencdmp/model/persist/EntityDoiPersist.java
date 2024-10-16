package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
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

public class EntityDoiPersist {

    private UUID id;

    private UUID entityId;

    public static final String _entityId = "entityId";

    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    private String doi;

    public static final String _doi = "doi";

    private String hash;

    public static final String _hash = "hash";

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

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(EntityDoiPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EntityDoiPersistValidator extends BaseValidator<EntityDoiPersist> {

        public static final String ValidatorName = "EntityDoiPersistValidator";

        private final MessageSource messageSource;

        protected EntityDoiPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<EntityDoiPersist> modelClass() {
            return EntityDoiPersist.class;
        }

        @Override
        protected List<Specification> specifications(EntityDoiPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(EntityDoiPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{EntityDoiPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(EntityDoiPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(EntityDoiPersist._entityId).failWith(messageSource.getMessage("Validation_Required", new Object[]{EntityDoiPersist._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getRepositoryId()))
                            .failOn(EntityDoiPersist._repositoryId).failWith(messageSource.getMessage("Validation_Required", new Object[]{EntityDoiPersist._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getDoi()))
                            .failOn(EntityDoiPersist._doi).failWith(messageSource.getMessage("Validation_Required", new Object[]{EntityDoiPersist._doi}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
