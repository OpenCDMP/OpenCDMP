package org.opencdmp.model.persist.descriptionproperties;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.validation.StatusAware;
import org.opencdmp.service.visibility.VisibilityService;
import org.opencdmp.service.visibility.VisibilityServiceImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyDefinitionPersist {

    public final static String _fieldSets = "fieldSets";

    private Map<String, PropertyDefinitionFieldSetPersist> fieldSets;

    public Map<String, PropertyDefinitionFieldSetPersist> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(Map<String, PropertyDefinitionFieldSetPersist> fieldSets) {
        this.fieldSets = fieldSets;
    }

    @Component(PropertyDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PropertyDefinitionPersistValidator extends BaseValidator<PropertyDefinitionPersist> implements StatusAware<DescriptionStatus> {

        public static final String ValidatorName = "Description.PropertyDefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        private DescriptionStatus status;
        private DefinitionEntity definition;
        private VisibilityService visibilityService;

        protected PropertyDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PropertyDefinitionPersist> modelClass() {
            return PropertyDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(PropertyDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.status == DescriptionStatus.Finalized)
                            .must(() -> !this.isNull(item.getFieldSets()) && !item.getFieldSets().isEmpty())
                            .failOn(PropertyDefinitionPersist._fieldSets).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PropertyDefinitionPersist._fieldSets}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isListNullOrEmpty(this.getMissingFieldSetEntity(item)))
                            .failOn(PropertyDefinitionPersist._fieldSets).failWith(this.messageSource.getMessage("Validation.MissingFields", new Object[]{this.serializeMissingFieldSets(this.getMissingFieldSetEntity(item))}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isListNullOrEmpty(this.getMissingFieldsEntity(item)))
                            .failOn(PropertyDefinitionPersist._fieldSets).failWith(this.messageSource.getMessage("Validation.MissingFields", new Object[]{this.serializeMissingFields(this.getMissingFieldsEntity(item))}, LocaleContextHolder.getLocale())),
                    this.mapSpec()
                            .iff(() -> !this.isNull(item.getFieldSets()))
                            .on(PropertyDefinitionPersist._fieldSets)
                            .over(item.getFieldSets())
                            .mapKey((k) -> ((String)k))
                            .using((itm) -> {
                                FieldSetEntity fieldSetEntity = this.definition != null ? this.definition.getFieldSetById((String)itm.getKey()).stream().findFirst().orElse(null) : null;
                                return this.validatorFactory.validator(PropertyDefinitionFieldSetPersist.PersistValidator.class).withFieldSetEntity(fieldSetEntity).withVisibilityService(this.visibilityService).setStatus(this.status);
                            })
            );
        }

        @Override
        public PropertyDefinitionPersistValidator setStatus(DescriptionStatus status) {
            this.status = status;
            return this;
        }

        public PropertyDefinitionPersistValidator withDefinition(DefinitionEntity definition) {
            this.definition = definition;
            return this;
        }

        public PropertyDefinitionPersistValidator setVisibilityService(DefinitionEntity definition, PropertyDefinitionPersist propertyDefinition) {
            this.visibilityService = new VisibilityServiceImpl(definition, propertyDefinition);
            return this;
        }

        private List<FieldSetEntity> getMissingFieldSetEntity(PropertyDefinitionPersist item){
            List<FieldSetEntity> missingMultipleFieldSets = new ArrayList<>();

            if (this.definition == null || this.definition.getAllFieldSets() == null) return missingMultipleFieldSets;
            for (FieldSetEntity fieldSet: this.definition.getAllFieldSets()) {

                boolean requiredAtLeastOneFieldSet = fieldSet.getMultiplicity() != null && fieldSet.getHasMultiplicity() && fieldSet.getMultiplicity().getMin() > 0;
                if (requiredAtLeastOneFieldSet) {
                    if (item == null || item.getFieldSets() == null)  missingMultipleFieldSets.add(fieldSet);
                    if (item != null && item.getFieldSets() != null) {
                        PropertyDefinitionFieldSetPersist fieldSetPersist = item.getFieldSets().getOrDefault(fieldSet.getId(), null);
                        if (fieldSetPersist == null) missingMultipleFieldSets.add(fieldSet);
                    }
                }

            }
            return missingMultipleFieldSets;
        }

        private List<FieldEntity> getMissingFieldsEntity(PropertyDefinitionPersist item){
            List<FieldEntity> missingFields = new ArrayList<>();

            if (this.definition == null || this.definition.getAllFieldSets() == null) return missingFields;
            for (FieldSetEntity fieldSet: this.definition.getAllFieldSets()) {

                if (fieldSet.getFields() == null) continue;

                for(FieldEntity field : fieldSet.getFields()){

                    if (field.getValidations() == null) continue;
                    if (field.getValidations().contains(FieldValidationType.Required)){
                        if (item == null || item.getFieldSets() == null) {
                            missingFields.add(field);
                            continue;
                        }
                        PropertyDefinitionFieldSetPersist propertyDefinitionFieldSetPersist = item.getFieldSets().getOrDefault(fieldSet.getId(), null);
                        if (propertyDefinitionFieldSetPersist == null || propertyDefinitionFieldSetPersist.getItems() == null) {
                            missingFields.add(field);
                            continue;
                        }

                        for (PropertyDefinitionFieldSetItemPersist propertyDefinitionFieldSetItemPersist: propertyDefinitionFieldSetPersist.getItems()) {

                            if (propertyDefinitionFieldSetItemPersist.getFields() == null){
                                missingFields.add(field);
                                continue;
                            }
                            FieldPersist fieldPersist = propertyDefinitionFieldSetItemPersist.getFields().getOrDefault(field.getId(), null);
                            if (fieldPersist == null){
                                missingFields.add(field);
                            }
                        }

                    }
                }
            }
            return missingFields;
        }

        private String serializeMissingFieldSets (List<FieldSetEntity> missingFieldSets){
            if (missingFieldSets == null) return "";
            return missingFieldSets.stream().map(FieldSetEntity::getId).collect(Collectors.joining(", "));
        }

        private String serializeMissingFields (List<FieldEntity> missingFields){
            if (missingFields == null) return "";
            return missingFields.stream().map(FieldEntity::getId).collect(Collectors.joining(", "));
        }
    }

}
