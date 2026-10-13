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

import java.util.List;

public class InstallationPersist {

    private Boolean shareUsage;

    public static final String _shareUsage = "shareUsage";

    public Boolean getShareUsage() {
        return this.shareUsage;
    }

    public void setShareUsage(Boolean shareUsage) {
        this.shareUsage = shareUsage;
    }

    @Component(InstallationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class InstallationPersistValidator extends BaseValidator<InstallationPersist> {
        public static final String ValidatorName = "InstallationPersistValidator";

        private final MessageSource messageSource;

        public InstallationPersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<InstallationPersist> modelClass() {
            return InstallationPersist.class;
        }

        @Override
        protected List<Specification> specifications(InstallationPersist item) {
            return List.of(
                    this.spec()
                            .must(() -> !this.isNull(item.getShareUsage()))
                            .failOn(InstallationPersist._shareUsage).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{InstallationPersist._shareUsage}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
