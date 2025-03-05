package org.opencdmp.model.persist.externalfetcher;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.externalfetcher.config.entities.SourceStaticOptionConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ExternalFetcherStaticOptionSourceConfigurationPersist extends ExternalFetcherBaseSourceConfigurationPersist implements SourceStaticOptionConfiguration<StaticPersist> {

    List<StaticPersist> items;

    public static final String _items = "items";


    public List<StaticPersist> getItems() {
        return items;
    }

    public void setItems(List<StaticPersist> items) {
        this.items = items;
    }

    @Component(ExternalFetcherStaticOptionSourceConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ExternalFetcherStaticOptionSourceConfigurationPersistValidator extends ExternalFetcherBaseSourceConfigurationPersistValidator<ExternalFetcherStaticOptionSourceConfigurationPersist> {

        public static final String ValidatorName = "ExternalFetcherStaticOptionSourceConfigurationPersistValidator";

        protected ExternalFetcherStaticOptionSourceConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource, validatorFactory);
        }

        @Override
        protected Class<ExternalFetcherStaticOptionSourceConfigurationPersist> modelClass() {
            return ExternalFetcherStaticOptionSourceConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExternalFetcherStaticOptionSourceConfigurationPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getItems()))
                            .failOn(ExternalFetcherStaticOptionSourceConfigurationPersist._items).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherStaticOptionSourceConfigurationPersist._items}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getItems()))
                            .on(ExternalFetcherStaticOptionSourceConfigurationPersist._items)
                            .over(item.getItems())
                            .using((itm) -> this.validatorFactory.validator(StaticPersist.StaticPersistValidator.class))
            ));
            return specifications;
        }
    }

}
