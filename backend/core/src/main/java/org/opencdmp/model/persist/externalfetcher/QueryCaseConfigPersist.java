package org.opencdmp.model.persist.externalfetcher;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.externalfetcher.config.entities.QueryCaseConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QueryCaseConfigPersist implements QueryCaseConfig {

    private String likePattern;
    public static final String _likePattern = "likePattern";

    private String separator;
    public static final String _separator = "separator";

    private String value;
    public static final String _value = "value";

    private UUID referenceTypeId;
    public static final String _referenceTypeId = "referenceTypeId";
    private String referenceTypeSourceKey;
    public static final String _referenceTypeSourceKey = "referenceTypeSourceKey";


    public String getLikePattern() {
        return likePattern;
    }

    public void setLikePattern(String likePattern) {
        this.likePattern = likePattern;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public String getReferenceTypeSourceKey() {
        return referenceTypeSourceKey;
    }

    public void setReferenceTypeSourceKey(String referenceTypeSourceKey) {
        this.referenceTypeSourceKey = referenceTypeSourceKey;
    }

    @Component(QueryCaseConfigPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class QueryCaseConfigPersistValidator extends BaseValidator<QueryCaseConfigPersist> {

        public static final String ValidatorName = "QueryCaseConfigPersistValidator";
        private final MessageSource messageSource;

        protected QueryCaseConfigPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<QueryCaseConfigPersist> modelClass() {
            return QueryCaseConfigPersist.class;
        }

        @Override
        protected List<Specification> specifications(QueryCaseConfigPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getValue()))
                            .failOn(QueryCaseConfigPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{QueryCaseConfigPersist._value}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getReferenceTypeId()))
                            .must(() -> !this.isEmpty(item.getReferenceTypeSourceKey()))
                            .failOn(QueryCaseConfigPersist._referenceTypeSourceKey).failWith(messageSource.getMessage("Validation_Required", new Object[]{QueryCaseConfigPersist._referenceTypeSourceKey}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}

