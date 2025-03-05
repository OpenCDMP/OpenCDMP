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

public class DescriptionStatusPersist {

    private UUID id;

    public static final String _id = "id";

    private UUID statusId;

    public static final String _status = "status";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(DescriptionStatusPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusPersistValidator extends BaseValidator<DescriptionStatusPersist> {

        public static final String ValidatorName = "DescriptionStatusPersistValidator";

        private final MessageSource messageSource;

        protected DescriptionStatusPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionStatusPersist> modelClass() {
            return DescriptionStatusPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(DescriptionStatusPersist._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(DescriptionStatusPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getStatusId()))
                            .failOn(DescriptionStatusPersist._status).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._status}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
