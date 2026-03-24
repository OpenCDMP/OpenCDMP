package org.opencdmp.model.persist;


import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintTypeStatus;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintTypeEntity;
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

public class PlanBlueprintTypePersist {

    private UUID id;

    public final static String _id = "id";

    private String code;

    public static final String _code = "code";

    private String name = null;

    public final static String _name = "name";

    private String hash;

    public final static String _hash = "hash";

    private PlanBlueprintTypeStatus status;

    public final static String _status = "status";

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

    public PlanBlueprintTypeStatus getStatus() {
        return status;
    }

    public void setStatus(PlanBlueprintTypeStatus status) {
        this.status = status;
    }

    @Component(PlanBlueprintTypePersistValidator.ValidatorName)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanBlueprintTypePersistValidator extends BaseValidator<PlanBlueprintTypePersist> {
        public static final String ValidatorName = "PlanBlueprintTypePersistValidator";

        private final MessageSource messageSource;

	    public PlanBlueprintTypePersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
		    this.messageSource = messageSource;
	    }

        @Override
        protected Class<PlanBlueprintTypePersist> modelClass() {
            return PlanBlueprintTypePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanBlueprintTypePersist item) {
            return Arrays.asList(
                    this.spec()
                        .iff(() -> this.isValidGuid(item.getId()))
                        .must(() -> this.isValidHash(item.getHash()))
                        .failOn(PlanBlueprintTypePersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintTypePersist._hash}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isValidGuid(item.getId()))
                        .must(() -> !this.isValidHash(item.getHash()))
                        .failOn(PlanBlueprintTypePersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .must(() -> !this.isEmpty(item.getName()))
                        .failOn(PlanBlueprintTypePersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintTypePersist._name}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isEmpty(item.getName()))
                        .must(() -> this.lessEqualLength(item.getName(), PlanBlueprintTypeEntity._nameLength))
                        .failOn(PlanBlueprintTypePersist._name).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{PlanBlueprintTypePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PlanBlueprintTypePersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintTypePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), PlanBlueprintTypeEntity._codeLength))
                            .failOn(PlanBlueprintTypePersist._code).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{PlanBlueprintTypePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(DescriptionTemplateTypePersist._status).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplateTypePersist._status}, LocaleContextHolder.getLocale()))
                    );
        }
    }
    
}

