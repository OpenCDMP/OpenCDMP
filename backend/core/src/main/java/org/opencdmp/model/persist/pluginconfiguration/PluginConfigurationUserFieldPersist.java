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

public class PluginConfigurationUserFieldPersist {

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

    @Component(PluginConfigurationUserFieldPersist.PluginConfigurationUserFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PluginConfigurationUserFieldPersistValidator extends BaseValidator<PluginConfigurationUserFieldPersist> {

        public static final String ValidatorName = "PluginConfigurationUserFieldPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PluginConfigurationUserFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PluginConfigurationUserFieldPersist> modelClass() {
            return PluginConfigurationUserFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(PluginConfigurationUserFieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PluginConfigurationUserFieldPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationUserFieldPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getFileValue()))
                            .must(() -> this.isEmpty(item.getTextValue()))
                            .failOn(PluginConfigurationUserFieldPersist._textValue).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PluginConfigurationUserFieldPersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getTextValue()))
                            .must(() -> this.isNull(item.getFileValue()))
                            .failOn(PluginConfigurationUserFieldPersist._fileValue).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PluginConfigurationUserFieldPersist._fileValue}, LocaleContextHolder.getLocale()))
                 );
        }
    }
}
