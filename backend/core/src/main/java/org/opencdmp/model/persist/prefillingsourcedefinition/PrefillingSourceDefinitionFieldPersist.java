package org.opencdmp.model.persist.prefillingsourcedefinition;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class PrefillingSourceDefinitionFieldPersist {

    private String code;
    public final static String _code = "code";
    private String systemFieldTarget;
    public final static String _systemFieldTarget = "systemFieldTarget";
    private String semanticTarget;
    public final static String _semanticTarget = "semanticTarget";
    private String trimRegex;
    public final static String _trimRegex = "trimRegex";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSystemFieldTarget() {
        return systemFieldTarget;
    }

    public void setSystemFieldTarget(String systemFieldTarget) {
        this.systemFieldTarget = systemFieldTarget;
    }

    public String getSemanticTarget() {
        return semanticTarget;
    }

    public void setSemanticTarget(String semanticTarget) {
        this.semanticTarget = semanticTarget;
    }

    public String getTrimRegex() {
        return trimRegex;
    }

    public void setTrimRegex(String trimRegex) {
        this.trimRegex = trimRegex;
    }


    @Component(PrefillingSourceDefinitionFieldPersist.PrefillingSourceDefinitionFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PrefillingSourceDefinitionFieldPersistValidator extends BaseValidator<PrefillingSourceDefinitionFieldPersist> {

        public static final String ValidatorName = "PrefillingSourceDefinitionFieldPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PrefillingSourceDefinitionFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PrefillingSourceDefinitionFieldPersist> modelClass() {
            return PrefillingSourceDefinitionFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(PrefillingSourceDefinitionFieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PrefillingSourceDefinitionFieldPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFieldPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSemanticTarget()) || !this.isEmpty(item.getSystemFieldTarget()))
                            .failOn(PrefillingSourceDefinitionFieldPersist._semanticTarget).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFieldPersist._semanticTarget}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSemanticTarget()) || !this.isEmpty(item.getSystemFieldTarget()))
                            .failOn(PrefillingSourceDefinitionFieldPersist._systemFieldTarget).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFieldPersist._systemFieldTarget}, LocaleContextHolder.getLocale()))

                    );
        }
    }
}
