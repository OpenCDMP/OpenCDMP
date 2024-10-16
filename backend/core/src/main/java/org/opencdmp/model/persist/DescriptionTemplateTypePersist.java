package org.opencdmp.model.persist;


import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.validation.*;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DescriptionTemplateTypePersist {

    private UUID id;

    public final static String _id = "id";

    private String code;

    public static final String _code = "code";

    private String name = null;

    public final static String _name = "name";

    private String hash;

    public final static String _hash = "hash";

    private DescriptionTemplateTypeStatus status;
    
    private Map<UUID, DescriptionTemplateTypePersist> test;

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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public DescriptionTemplateTypeStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionTemplateTypeStatus status) {
        this.status = status;
    }

    @Component(DescriptionTemplateTypePersistValidator.ValidatorName)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionTemplateTypePersistValidator extends BaseValidator<DescriptionTemplateTypePersist> {
        public static final String ValidatorName = "DescriptionTemplateTypePersistValidator";

        private final MessageSource messageSource;

	    public DescriptionTemplateTypePersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
		    this.messageSource = messageSource;
	    }

        @Override
        protected Class<DescriptionTemplateTypePersist> modelClass() {
            return DescriptionTemplateTypePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionTemplateTypePersist item) {
            return Arrays.asList(
                    this.spec()
                        .iff(() -> this.isValidGuid(item.getId()))
                        .must(() -> this.isValidHash(item.getHash()))
                        .failOn(DescriptionTemplateTypePersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplateTypePersist._hash}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isValidGuid(item.getId()))
                        .must(() -> !this.isValidHash(item.getHash()))
                        .failOn(DescriptionTemplateTypePersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .must(() -> !this.isEmpty(item.getName()))
                        .failOn(DescriptionTemplateTypePersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplateTypePersist._name}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isEmpty(item.getName()))
                        .must(() -> this.lessEqualLength(item.getName(), DescriptionTemplateTypeEntity._nameLength))
                        .failOn(DescriptionTemplateTypePersist._name).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{DescriptionTemplateTypePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(DescriptionTemplateTypePersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplateTypePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), DescriptionTemplateTypeEntity._codeLength))
                            .failOn(DescriptionTemplateTypePersist._code).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{DescriptionTemplateTypePersist._code}, LocaleContextHolder.getLocale()))
                    );
        }
    }
    
}

