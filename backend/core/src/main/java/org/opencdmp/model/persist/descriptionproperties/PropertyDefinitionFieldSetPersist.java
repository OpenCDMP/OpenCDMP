package org.opencdmp.model.persist.descriptionproperties;

import org.opencdmp.commons.enums.DescriptionStatus;
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

public class PropertyDefinitionFieldSetPersist {

    public final static String _items = "items";
    private List<PropertyDefinitionFieldSetItemPersist> items;

    public final static String _comment = "comment";
    private String comment;

    public List<PropertyDefinitionFieldSetItemPersist> getItems() {
        return items;
    }

    public void setItems(List<PropertyDefinitionFieldSetItemPersist> items) {
        this.items = items;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Component(PersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PersistValidator extends BaseValidator<PropertyDefinitionFieldSetPersist> {

        public static final String ValidatorName = "Description.PropertyDefinitionFieldSetPersistValidator";
        private final ValidatorFactory validatorFactory;
        private final MessageSource messageSource;
        private FieldSetEntity fieldSetEntity;
        private DescriptionStatus status;
        private VisibilityService visibilityService;
        protected PersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, MessageSource messageSource, MessageSource messageSource1) {
            super(conventionService, errors);
	        this.validatorFactory = validatorFactory;
            this.messageSource = messageSource1;
        }

        @Override
        protected Class<PropertyDefinitionFieldSetPersist> modelClass() {
            return PropertyDefinitionFieldSetPersist.class;
        }

        @Override
        protected List<Specification> specifications(PropertyDefinitionFieldSetPersist item) {
            boolean isVisible =  this.fieldSetEntity != null ? this.visibilityService.isVisible(this.fieldSetEntity.getId(), 0) : true;
            int min = fieldSetEntity != null && fieldSetEntity.getHasMultiplicity() && fieldSetEntity.getMultiplicity() != null && fieldSetEntity.getMultiplicity().getMin() != null ? fieldSetEntity.getMultiplicity().getMin() : 0;
            int max = fieldSetEntity != null && fieldSetEntity.getHasMultiplicity()  && fieldSetEntity.getMultiplicity() != null && fieldSetEntity.getMultiplicity().getMax() != null ? fieldSetEntity.getMultiplicity().getMax() : Integer.MAX_VALUE;

            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isNull(item.getItems()))
                            .on(PropertyDefinitionFieldSetPersist._items)
                            .over(item.getItems())
                            .using((itm) -> this.validatorFactory.validator(PropertyDefinitionFieldSetItemPersist.PersistValidator.class).withFieldSetEntity(this.fieldSetEntity).withVisibilityService(this.visibilityService).setStatus(this.status)),
                    this.spec()
                            .iff(() -> DescriptionStatus.Finalized.equals(this.status) && isVisible && fieldSetEntity.getHasMultiplicity())
                            .must(() -> !this.isListNullOrEmpty(item.getItems()) && min <= item.getItems().size())
                            .failOn(PropertyDefinitionFieldSetPersist._items).failWith(messageSource.getMessage("Validation.LargerThenEqual", new Object[]{PropertyDefinitionFieldSetPersist._items, min}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> DescriptionStatus.Finalized.equals(this.status) && isVisible && fieldSetEntity.getHasMultiplicity())
                            .must(() -> !this.isListNullOrEmpty(item.getItems()) && max >= item.getItems().size())
                            .failOn(PropertyDefinitionFieldSetPersist._items).failWith(messageSource.getMessage("Validation.LessThenEqual", new Object[]{PropertyDefinitionFieldSetPersist._items, max}, LocaleContextHolder.getLocale()))
                    );
        }

        public PersistValidator withFieldSetEntity(FieldSetEntity fieldSetEntity) {
            this.fieldSetEntity = fieldSetEntity;
            return this;
        }

        public PersistValidator withVisibilityService(VisibilityService visibilityService) {
            this.visibilityService = visibilityService;
            return this;
        }

        public PropertyDefinitionFieldSetPersist.PersistValidator setStatus(DescriptionStatus status) {
            this.status = status;
            return this;
        }

    }

}


