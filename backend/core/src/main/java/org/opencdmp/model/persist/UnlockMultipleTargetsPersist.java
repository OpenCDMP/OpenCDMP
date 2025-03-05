package org.opencdmp.model.persist;

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

public class UnlockMultipleTargetsPersist {

    private List<UUID> targetIds;
    public static final String _targetIds = "targetIds";

    public List<UUID> getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(List<UUID> targetIds) {
        this.targetIds = targetIds;
    }

    @Component(UnlockMultipleTargetsPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UnlockMultipleTargetsPersistValidator extends BaseValidator<UnlockMultipleTargetsPersist> {

        public static final String ValidatorName = "UnlockMultipleTargetsPersistValidator";

        private final MessageSource messageSource;

        protected UnlockMultipleTargetsPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UnlockMultipleTargetsPersist> modelClass() {
            return UnlockMultipleTargetsPersist.class;
        }

        @Override
        protected List<Specification> specifications(UnlockMultipleTargetsPersist item) {
            return Arrays.asList(
                  this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getTargetIds()))
                            .failOn(UnlockMultipleTargetsPersist._targetIds).failWith(messageSource.getMessage("Validation_Required", new Object[]{UnlockMultipleTargetsPersist._targetIds}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

