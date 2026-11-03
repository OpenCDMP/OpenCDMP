package org.opencdmp.model.persist.tenantconfiguration;

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

public class DashboardSlideTranslationPersist {

    private String languageCode;
    public static final String _languageCode = "languageCode";

    private String title;
    public static final String _title = "title";

    private String text;
    public static final String _text = "text";

    private String buttonText;
    public static final String _buttonText = "buttonText";

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    @Component(DashboardSlideTranslationPersist.DashboardSlideTranslationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DashboardSlideTranslationPersistValidator extends BaseValidator<DashboardSlideTranslationPersist> {

        public static final String ValidatorName = "DashboardSlideTranslationPersistValidator";

        private final MessageSource messageSource;

        protected DashboardSlideTranslationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DashboardSlideTranslationPersist> modelClass() {
            return DashboardSlideTranslationPersist.class;
        }

        @Override
        protected List<Specification> specifications(DashboardSlideTranslationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLanguageCode()))
                            .failOn(DashboardSlideTranslationPersist._languageCode).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlideTranslationPersist._languageCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTitle()))
                            .failOn(DashboardSlideTranslationPersist._title).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlideTranslationPersist._title}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
