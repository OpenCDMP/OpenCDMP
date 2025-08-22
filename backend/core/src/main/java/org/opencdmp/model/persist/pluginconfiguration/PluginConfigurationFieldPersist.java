package org.opencdmp.model.persist.pluginconfiguration;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
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

public class PluginConfigurationFieldPersist {

    private String code;
    public static final String _code = "code";

    private UUID fileValue;
    public static final String _fileValue = "fileValue";

    private String textValue;
    public static final String _textValue = "textValue";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getFileValue() {
        return fileValue;
    }

    public void setFileValue(UUID fileValue) {
        this.fileValue = fileValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    @Component(PluginConfigurationFieldPersist.PluginConfigurationFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PluginConfigurationFieldPersistValidator extends BaseValidator<PluginConfigurationFieldPersist> {

        public static final String ValidatorName = "PluginConfigurationFieldPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PluginConfigurationFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PluginConfigurationFieldPersist> modelClass() {
            return PluginConfigurationFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(PluginConfigurationFieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PluginConfigurationFieldPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationFieldPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getFileValue()))
                            .must(() -> this.isEmpty(item.getTextValue()))
                            .failOn(PluginConfigurationFieldPersist._textValue).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PluginConfigurationFieldPersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getTextValue()))
                            .must(() -> this.isNull(item.getFileValue()))
                            .failOn(PluginConfigurationFieldPersist._fileValue).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PluginConfigurationFieldPersist._fileValue}, LocaleContextHolder.getLocale()))
                 );
        }
    }
}
