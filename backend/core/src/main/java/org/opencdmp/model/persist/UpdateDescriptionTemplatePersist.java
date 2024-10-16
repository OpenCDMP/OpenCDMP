package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UpdateDescriptionTemplatePersist {

    private UUID id;

    public static final String _id = "id";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    private String hash;

    public static final String _hash = "hash";

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(UpdateDescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UpdateDescriptionTemplatePersistValidator extends BaseValidator<UpdateDescriptionTemplatePersist> {

        public static final String ValidatorName = "UpdateDescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        protected UpdateDescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UpdateDescriptionTemplatePersist> modelClass() {
            return UpdateDescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(UpdateDescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UpdateDescriptionTemplatePersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{UpdateDescriptionTemplatePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(UpdateDescriptionTemplatePersist._id)
                            .failWith(messageSource.getMessage("Validation_Required", new Object[]{UpdateDescriptionTemplatePersist._id}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
