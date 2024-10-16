package org.opencdmp.model.persist.referencetypedefinition;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherApiSourceConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherStaticOptionSourceConfigurationPersist;
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

public class ReferenceTypeDefinitionPersist {

    private List<ReferenceTypeFieldPersist> fields = null;

    public static final String _fields = "fields";

    private List<ExternalFetcherBaseSourceConfigurationPersist> sources = null;

    public static final String _sources = "sources";

    public List<ReferenceTypeFieldPersist> getFields() {
        return fields;
    }

    public void setFields(List<ReferenceTypeFieldPersist> fields) {
        this.fields = fields;
    }

    public List<ExternalFetcherBaseSourceConfigurationPersist> getSources() {
        return sources;
    }

    public void setSources(List<ExternalFetcherBaseSourceConfigurationPersist> sources) {
        this.sources = sources;
    }

    @Component(ReferenceTypeDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceTypeDefinitionPersistValidator extends BaseValidator<ReferenceTypeDefinitionPersist> {

        public static final String ValidatorName = "ReferenceTypeDefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ReferenceTypeDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ReferenceTypeDefinitionPersist> modelClass() {
            return ReferenceTypeDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferenceTypeDefinitionPersist item) {
            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(ReferenceTypeDefinitionPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(ReferenceTypeFieldPersist.ReferenceTypeFieldPersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getSources()))
                            .failOn(ReferenceTypeDefinitionPersist._sources).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeDefinitionPersist._sources}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSources()))
                            .on(ReferenceTypeDefinitionPersist._sources)
                            .over(item.getSources())
                            .using((itm) -> ((ExternalFetcherBaseSourceConfigurationPersist) itm).getType() == ExternalFetcherSourceType.STATIC? this.validatorFactory.validator(ExternalFetcherStaticOptionSourceConfigurationPersist.ExternalFetcherStaticOptionSourceConfigurationPersistValidator.class): this.validatorFactory.validator(ExternalFetcherApiSourceConfigurationPersist.ExternalFetcherApiSourceConfigurationPersistValidator.class))
            );
        }
    }

}
