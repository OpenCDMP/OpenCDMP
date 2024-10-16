package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PrefillingSearchRequest {

    private String like;
    public static final String _like = "like";

    private UUID prefillingSourceId;

    public static final String _prefillingSourceId = "prefillingSourceId";

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public UUID getPrefillingSourceId() {
        return prefillingSourceId;
    }

    public void setPrefillingSourceId(UUID prefillingSourceId) {
        this.prefillingSourceId = prefillingSourceId;
    }

    @Component(PrefillingSearchRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PrefillingSearchRequestValidator extends BaseValidator<PrefillingSearchRequest> {

        public static final String ValidatorName = "PrefillingSearchRequestValidator";

        private final MessageSource messageSource;


        protected PrefillingSearchRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PrefillingSearchRequest> modelClass() {
            return PrefillingSearchRequest.class;
        }

        @Override
        protected List<Specification> specifications(PrefillingSearchRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPrefillingSourceId()))
                            .failOn(PrefillingSearchRequest._prefillingSourceId).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSearchRequest._prefillingSourceId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

