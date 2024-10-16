package org.opencdmp.model.persist.descriptiontemplatedefinition;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DefinitionPersist {

    private List<PagePersist> pages = null;

    public static final String _pages = "pages";


    public List<PagePersist> getPages() {
        return pages;
    }

    public void setPages(List<PagePersist> pages) {
        this.pages = pages;
    }

    @Component(DefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DefinitionPersistValidator extends BaseValidator<DefinitionPersist> {

        public static final String ValidatorName = "DescriptionTemplate.DefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        public DefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DefinitionPersist> modelClass() {
            return DefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getPages()))
                            .failOn(DefinitionPersist._pages).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._pages}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getPages()))
                            .on(DefinitionPersist._pages)
                            .over(item.getPages())
                            .using((itm) -> this.validatorFactory.validator(PagePersist.PagePersistValidator.class))
            );
        }
    }

}
