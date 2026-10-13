package org.opencdmp.model.persist.planproperties;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.RootSectionEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetPersist;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionPersist;
import org.opencdmp.model.persist.validation.StatusAware;
import org.opencdmp.service.visibility.VisibilityService;
import org.opencdmp.service.visibility.VisibilityServiceImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlanPropertiesPersist {

    private Map<String, PropertyDefinitionFieldSetPersist> fieldSets;

    public final static String _fieldSets = "fieldSets";

    private List<PlanContactPersist> contacts;

    public static final String _contacts = "contacts";

    private Boolean includeConsent;

    public static final String _includeConsent = "includeConsent";

    public Map<String, PropertyDefinitionFieldSetPersist> getFieldSets() {
        return fieldSets;
    }

    public void setFieldSets(Map<String, PropertyDefinitionFieldSetPersist> fieldSets) {
        this.fieldSets = fieldSets;
    }

    public List<PlanContactPersist> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactPersist> contacts) {
        this.contacts = contacts;
    }

    public Boolean getIncludeConsent() {
        return includeConsent;
    }

    public void setIncludeConsent(Boolean includeConsent) {
        this.includeConsent = includeConsent;
    }

    @Component(PlanPropertiesPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanPropertiesPersistValidator extends BaseValidator<PlanPropertiesPersist> implements StatusAware<PlanStatus> {

        public static final String ValidatorName = "PlanPropertiesPersistValidator";

        private final ValidatorFactory validatorFactory;
        private final MessageSource messageSource;
        private VisibilityService visibilityService;

        private PlanStatus status;
        private DefinitionEntity definition;

        protected PlanPropertiesPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, MessageSource messageSource) {
            super(conventionService, errors);
            this.validatorFactory = validatorFactory;
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanPropertiesPersist> modelClass() {
            return PlanPropertiesPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanPropertiesPersist item) {
            String contactField = this.getContactFieldLabel();
            return Arrays.asList(
                    this.mapSpec()
                            .iff(() -> !this.isNull(item.getFieldSets()))
                            .on(PropertyDefinitionPersist._fieldSets)
                            .over(item.getFieldSets())
                            .mapKey((k) -> ((String)k))
                            .using((itm) -> {
                                FieldSetEntity fieldSetEntity = this.definition != null ? this.definition.getFieldSetById((String)itm.getKey()).stream().findFirst().orElse(null) : null;
                                return this.validatorFactory.validator(PropertyDefinitionFieldSetPersist.PersistValidator.class).withFieldSetEntity(fieldSetEntity).withVisibilityService(this.visibilityService).isFinalized(this.status != null && this.status.equals(PlanStatus.Finalized));
                            }),
                    this.spec()
                            .iff(() -> this.status == PlanStatus.Finalized && contactField != null)
                            .must(() -> !this.isListNullOrEmpty(item.getContacts()))
                            .failOn(PlanPropertiesPersist._contacts).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{contactField}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> this.status == PlanStatus.Finalized && !this.isListNullOrEmpty(item.getContacts()))
                            .on(PlanPropertiesPersist._contacts)
                            .over(item.getContacts())
                            .using((itm) -> this.validatorFactory.validator(PlanContactPersist.PlanContactPersistValidator.class))
            );
        }

        @Override
        public PlanPropertiesPersistValidator setStatus(PlanStatus status) {
            this.status = status;
            return this;
        }

        public PlanPropertiesPersistValidator withDefinition(DefinitionEntity definition) {
            this.definition = definition;
            return this;
        }

        private String getContactFieldLabel(){
            if (this.definition == null || this.isListNullOrEmpty(this.definition.getSections())) return null;

            for (RootSectionEntity section: this.definition.getSections()) {
                if (!this.isListNullOrEmpty(section.getAllField())) {
                    List<FieldEntity> fields = section.getAllField().stream().filter(x -> x.getData() != null && x.getData().getFieldType() != null && x.getData().getFieldType().equals(FieldType.SYSTEM)).collect(Collectors.toList());
                    if (!this.isListNullOrEmpty(fields)){

                        for (FieldEntity fieldEntity: fields) {
                            SystemFieldDataEntity data = (SystemFieldDataEntity) fieldEntity.getData();
                            if (data != null && data.getSystemFieldType() != null && data.getSystemFieldType().equals(SystemFieldType.PlanContact) && fieldEntity.getValidations() != null && fieldEntity.getValidations().contains(FieldValidationType.Required)) {
                                return !this.conventionService.isNullOrEmpty(data.getLabel()) ? data.getLabel() : SystemFieldType.PlanContact.name();
                            }
                        }
                    }
                }
            }
            return null;
        }

        public PlanPropertiesPersist.PlanPropertiesPersistValidator setVisibilityService(org.opencdmp.commons.types.planblueprint.DefinitionEntity definition, PlanPropertiesPersist propertyDefinition) {
            this.visibilityService = VisibilityServiceImpl.fromPersist(definition.getSections(), propertyDefinition.getFieldSets());
            return this;
        }

    }

}
