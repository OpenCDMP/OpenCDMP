package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClonePlanPersist {

    private UUID id = null;

    public static final String _id = "id";

    private String label = null;

    public static final String _label = "label";

    private String description = null;

    public static final String _description = "description";

    private List<UUID> descriptions = new ArrayList<>();

    public static final String _descriptions = "descriptions";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UUID> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<UUID> descriptions) {
        this.descriptions = descriptions;
    }

    @Component(ClonePlanPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ClonePlanPersistValidator extends BaseValidator<ClonePlanPersist> {

        public static final String ValidatorName = "ClonePlanPersistValidator";

        private final MessageSource messageSource;

        protected ClonePlanPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<ClonePlanPersist> modelClass() {
            return ClonePlanPersist.class;
        }

        @Override
        protected List<Specification> specifications(ClonePlanPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(ClonePlanPersist._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{ClonePlanPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(ClonePlanPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{ClonePlanPersist._label}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
