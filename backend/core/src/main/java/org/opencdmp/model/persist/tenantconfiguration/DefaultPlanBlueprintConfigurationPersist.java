package org.opencdmp.model.persist.tenantconfiguration;


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

public class DefaultPlanBlueprintConfigurationPersist {

    private UUID groupId;
    public static final String _groupId = "groupId";

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    @Component(DefaultPlanBlueprintConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DefaultPlanBlueprintConfigurationPersistValidator extends BaseValidator<DefaultPlanBlueprintConfigurationPersist> {

        public static final  String ValidatorName = "DefaultPlanBlueprintConfigurationPersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected DefaultPlanBlueprintConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DefaultPlanBlueprintConfigurationPersist> modelClass() { return DefaultPlanBlueprintConfigurationPersist.class; }

        @Override
        protected List<Specification> specifications(DefaultPlanBlueprintConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getGroupId()))
                            .failOn(DefaultPlanBlueprintConfigurationPersist._groupId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DefaultPlanBlueprintConfigurationPersist._groupId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
