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

public class PrefillingSourceDefinitionFixedValueFieldPersist {

    private String systemFieldTarget;
    public final static String _systemFieldTarget = "systemFieldTarget";
    private String semanticTarget;
    public final static String _semanticTarget = "semanticTarget";
    private String trimRegex;
    public final static String _trimRegex = "trimRegex";
    private String fixedValue;
    public final static String _fixedValue = "fixedValue";

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

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }


    @Component(PrefillingSourceDefinitionFixedValueFieldPersist.PrefillingSourceDefinitionFixedValueFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PrefillingSourceDefinitionFixedValueFieldPersistValidator extends BaseValidator<PrefillingSourceDefinitionFixedValueFieldPersist> {

        public static final String ValidatorName = "PrefillingSourceDefinitionFixedValueFieldPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PrefillingSourceDefinitionFixedValueFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PrefillingSourceDefinitionFixedValueFieldPersist> modelClass() {
            return PrefillingSourceDefinitionFixedValueFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(PrefillingSourceDefinitionFixedValueFieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getFixedValue()))
                            .failOn(PrefillingSourceDefinitionFixedValueFieldPersist._fixedValue).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFixedValueFieldPersist._fixedValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSemanticTarget()) || !this.isEmpty(item.getSystemFieldTarget()))
                            .failOn(PrefillingSourceDefinitionFixedValueFieldPersist._semanticTarget).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFixedValueFieldPersist._semanticTarget}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSemanticTarget()) || !this.isEmpty(item.getSystemFieldTarget()))
                            .failOn(PrefillingSourceDefinitionFixedValueFieldPersist._systemFieldTarget).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionFixedValueFieldPersist._systemFieldTarget}, LocaleContextHolder.getLocale()))
                    );
        }
    }
}
