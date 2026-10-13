package org.opencdmp.model.persist;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.MaintenanceTask;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.MaintenanceRunEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MaintenanceRunPersist {

    private List<MaintenanceTask> tasks;

    public static final String _tasks = "tasks";

    private Boolean stopOnFailure;

    public static final String _stopOnFailure = "stopOnFailure";

    private String preset;

    public static final String _preset = "preset";

    public List<MaintenanceTask> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<MaintenanceTask> tasks) {
        this.tasks = tasks;
    }

    public Boolean getStopOnFailure() {
        return this.stopOnFailure;
    }

    public void setStopOnFailure(Boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
    }

    public String getPreset() {
        return this.preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    @Component(MaintenanceRunPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class MaintenanceRunPersistValidator extends BaseValidator<MaintenanceRunPersist> {
        public static final String ValidatorName = "MaintenanceRunPersistValidator";

        private final MessageSource messageSource;

        public MaintenanceRunPersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<MaintenanceRunPersist> modelClass() {
            return MaintenanceRunPersist.class;
        }

        @Override
        protected List<Specification> specifications(MaintenanceRunPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> item.getTasks() != null && !item.getTasks().isEmpty() && item.getTasks().stream().allMatch(Objects::nonNull))
                            .failOn(MaintenanceRunPersist._tasks).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{MaintenanceRunPersist._tasks}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStopOnFailure()))
                            .failOn(MaintenanceRunPersist._stopOnFailure).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{MaintenanceRunPersist._stopOnFailure}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getPreset()))
                            .must(() -> this.lessEqualLength(item.getPreset(), MaintenanceRunEntity._presetLength))
                            .failOn(MaintenanceRunPersist._preset).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{MaintenanceRunPersist._preset}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
