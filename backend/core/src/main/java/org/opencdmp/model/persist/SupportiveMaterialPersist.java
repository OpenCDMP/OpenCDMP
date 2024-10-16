package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.SupportiveMaterialEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SupportiveMaterialPersist {

    private UUID id;

    private SupportiveMaterialFieldType type;

    public static final String _type = "type";

    private String languageCode;

    public static final String _languageCode = "languageCode";

    private String payload;

    public static final String _payload = "payload";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SupportiveMaterialFieldType getType() {
        return type;
    }

    public void setType(SupportiveMaterialFieldType type) {
        this.type = type;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(SupportiveMaterialPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SupportiveMaterialPersistValidator extends BaseValidator<SupportiveMaterialPersist> {

        public static final String ValidatorName = "SupportiveMaterialPersistValidator";

        private final MessageSource messageSource;

        protected SupportiveMaterialPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<SupportiveMaterialPersist> modelClass() {
            return SupportiveMaterialPersist.class;
        }

        @Override
        protected List<Specification> specifications(SupportiveMaterialPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(SupportiveMaterialPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{SupportiveMaterialPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(SupportiveMaterialPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(SupportiveMaterialPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{SupportiveMaterialPersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLanguageCode()))
                            .failOn(SupportiveMaterialPersist._languageCode).failWith(messageSource.getMessage("Validation_Required", new Object[]{SupportiveMaterialPersist._languageCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLanguageCode()))
                            .must(() -> this.lessEqualLength(item.getLanguageCode(), SupportiveMaterialEntity._languageCodeLength))
                            .failOn(SupportiveMaterialPersist._languageCode).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{SupportiveMaterialPersist._languageCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPayload()))
                            .failOn(SupportiveMaterialPersist._payload).failWith(messageSource.getMessage("Validation_Required", new Object[]{SupportiveMaterialPersist._payload}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
