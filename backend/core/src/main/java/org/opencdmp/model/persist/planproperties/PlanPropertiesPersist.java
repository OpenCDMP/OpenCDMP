package org.opencdmp.model.persist.planproperties;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.commons.types.planblueprint.SystemFieldEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.validation.StatusAware;
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

    private Map<UUID, PlanBlueprintValuePersist> planBlueprintValues;

    public static final String _planBlueprintValues = "planBlueprintValues";

    private List<PlanContactPersist> contacts;

    public static final String _contacts = "contacts";

    public Map<UUID, PlanBlueprintValuePersist> getPlanBlueprintValues() {
        return this.planBlueprintValues;
    }

    public void setPlanBlueprintValues(Map<UUID, PlanBlueprintValuePersist> planBlueprintValues) {
        this.planBlueprintValues = planBlueprintValues;
    }

    public List<PlanContactPersist> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactPersist> contacts) {
        this.contacts = contacts;
    }

    @Component(PlanPropertiesPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanPropertiesPersistValidator extends BaseValidator<PlanPropertiesPersist> implements StatusAware<PlanStatus> {

        public static final String ValidatorName = "PlanPropertiesPersistValidator";

        private final ValidatorFactory validatorFactory;
        private final MessageSource messageSource;

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
                            .iff(() ->this.status == PlanStatus.Finalized && !this.isNull(item.getPlanBlueprintValues()))
                            .on(PlanPropertiesPersist._planBlueprintValues)
                            .over(item.getPlanBlueprintValues())
                            .mapKey((k) -> ((UUID)k).toString())
                            .using((itm) -> this.validatorFactory.validator(PlanBlueprintValuePersist.PlanBlueprintValuePersistValidator.class).withDefinition(this.definition)),
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

            for (SectionEntity section: this.definition.getSections()) {
                if (!this.isListNullOrEmpty(section.getFields())) {
                    List<FieldEntity> fields = section.getFields().stream().filter(x -> x.getCategory().equals(PlanBlueprintFieldCategory.System)).collect(Collectors.toList());
                    if (!this.isListNullOrEmpty(fields)){

                        List<SystemFieldEntity> systemFields = (List<SystemFieldEntity>)(List<?>) fields;
                        if (!this.isListNullOrEmpty(systemFields)){
                            List<SystemFieldEntity> contactSystemFields = systemFields.stream().filter(x -> x.getType().equals(PlanBlueprintSystemFieldType.Contact) && x.isRequired()).collect(Collectors.toList());
                            if(!this.isListNullOrEmpty(contactSystemFields)) {
                                return contactSystemFields.getFirst().getLabel() != null ? contactSystemFields.getFirst().getLabel() : PlanBlueprintSystemFieldType.Contact.name();
                            }
                        }

                    }
                }
            }
            return null;
        }

    }

}
