package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class TenantPersist {

    private UUID id;

    private String code;

    public static final String _code = "code";

    private String name;

    public static final String _name = "name";

    private String description;

    public static final String _description = "description";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(TenantPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class TenantPersistValidator extends BaseValidator<TenantPersist> {

        public static final String ValidatorName = "TenantPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected TenantPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<TenantPersist> modelClass() {
            return TenantPersist.class;
        }

        @Override
        protected List<Specification> specifications(TenantPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(TenantPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(TenantPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(TenantPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), TenantEntity._codeLength))
                            .failOn(TenantPersist._code).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{TenantPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.validateCodePattern(item.getCode()))
                            .failOn(TenantPersist._code).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{TenantPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(TenantPersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), TenantEntity._nameLength))
                            .failOn(TenantPersist._name).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{TenantPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getDescription()))
                            .failOn(TenantPersist._description).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantPersist._description}, LocaleContextHolder.getLocale()))

            );
        }

        private boolean validateCodePattern(String code){
            if (this.isEmpty(code)) return false;

            Pattern pattern = Pattern.compile("^[a-z0-9_]*$");
            return pattern.matcher(code).matches();
        }
    }

}
