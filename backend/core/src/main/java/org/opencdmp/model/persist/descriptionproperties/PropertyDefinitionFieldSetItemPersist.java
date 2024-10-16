package org.opencdmp.model.persist.descriptionproperties;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.visibility.VisibilityService;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PropertyDefinitionFieldSetItemPersist {

    public final static String _fields = "fields";
    private Map<String, FieldPersist> fields;

    public final static String _ordinal = "ordinal";
    private Integer ordinal = null;

    public Map<String, FieldPersist> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldPersist> fields) {
        this.fields = fields;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Component(PersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PersistValidator extends BaseValidator<PropertyDefinitionFieldSetItemPersist> {

        public static final String ValidatorName = "Description.PropertyDefinitionFieldSetItemPersistValidator";
        private final ValidatorFactory validatorFactory;

        private final MessageSource messageSource;
        private FieldSetEntity fieldSetEntity;
        private DescriptionStatus status;
        private VisibilityService visibilityService;
        protected PersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, MessageSource messageSource) {
            super(conventionService, errors);
	        this.validatorFactory = validatorFactory;
	        this.messageSource = messageSource;
        }

        @Override
        protected Class<PropertyDefinitionFieldSetItemPersist> modelClass() {
            return PropertyDefinitionFieldSetItemPersist.class;
        }

        @Override
        protected List<Specification> specifications(PropertyDefinitionFieldSetItemPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(PropertyDefinitionFieldSetItemPersist._ordinal).failWith(messageSource.getMessage("Validation_Required", new Object[]{PropertyDefinitionFieldSetItemPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.mapSpec()
                            .iff(() -> !this.isNull(item.getFields()))
                            .on(PropertyDefinitionFieldSetItemPersist._fields)
                            .over(item.getFields())
                            .mapKey((k) -> ((String)k))
                            .using((itm) ->
                            {
                                FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById((String)itm.getKey()).stream().findFirst().orElse(null) : null;
                                return this.validatorFactory.validator(FieldPersist.PersistValidator.class).withFieldEntity(fieldEntity).withVisibilityService(visibilityService).withOrdinal(item.getOrdinal()).setStatus(this.status);
                            })

            );
        }

        public PersistValidator withFieldSetEntity(FieldSetEntity fieldSetEntity) {
            this.fieldSetEntity = fieldSetEntity;
            return this;
        }

        public PersistValidator setStatus(DescriptionStatus status) {
            this.status = status;
            return this;
        }

        public PersistValidator withVisibilityService(VisibilityService visibilityService) {
            this.visibilityService = visibilityService;
            return this;
        }
    }

}


