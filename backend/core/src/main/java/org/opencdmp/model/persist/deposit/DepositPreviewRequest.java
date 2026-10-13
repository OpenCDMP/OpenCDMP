package org.opencdmp.model.persist.deposit;

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

public class DepositPreviewRequest {

    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    private UUID planId;

    public static final String _planId = "planId";

    public String getRepositoryId() {
        return this.repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public UUID getPlanId() {
        return this.planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    @Component(DepositPreviewRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DepositPreviewRequestValidator extends BaseValidator<DepositPreviewRequest> {

        public static final String ValidatorName = "DepositPreviewRequestValidator";

        private final MessageSource messageSource;

        protected DepositPreviewRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DepositPreviewRequest> modelClass() {
            return DepositPreviewRequest.class;
        }

        @Override
        protected List<Specification> specifications(DepositPreviewRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getRepositoryId()))
                            .failOn(DepositPreviewRequest._repositoryId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositPreviewRequest._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(DepositPreviewRequest._planId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositPreviewRequest._planId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
