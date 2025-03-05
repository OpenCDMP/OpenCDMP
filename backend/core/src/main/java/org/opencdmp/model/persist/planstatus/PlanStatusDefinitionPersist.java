package org.opencdmp.model.persist.planstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanStatusAvailableActionType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanStatusDefinitionPersist {
    public final static String _authorization = "authorization";
    private PlanStatusDefinitionAuthorizationPersist authorization = null;

    public final static String _availableActions = "availableActions";
    private List<PlanStatusAvailableActionType> availableActions;

    public final static String _matIconName = "matIconName";
    private  String matIconName;

    public final static String _storageFileId = "storageFileId";
    private UUID storageFileId;

    public final static String _statusColor = "statusColor";
    private  String statusColor;

    public PlanStatusDefinitionAuthorizationPersist getAuthorization() { return authorization; }

    public void setAuthorization(PlanStatusDefinitionAuthorizationPersist authorization) { this.authorization = authorization; }

    public List<PlanStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<PlanStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    @Component(PlanStatusDefinitionPersist.PlanStatusDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanStatusDefinitionPersistValidator extends BaseValidator<PlanStatusDefinitionPersist> {
        public static final String ValidatorName = "PlanStatus.PlanStatusDefinitionPersistValidator";
        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        public PlanStatusDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanStatusDefinitionPersist> modelClass() {
            return PlanStatusDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanStatusDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getAuthorization()))
                            .failOn(PlanStatusDefinitionPersist._authorization).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanStatusDefinitionPersist._authorization}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAuthorization()))
                            .on(PlanStatusDefinitionPersist._authorization)
                            .over(item.getAuthorization())
                            .using(() -> this.validatorFactory.validator(PlanStatusDefinitionAuthorizationPersist.PlanStatusDefinitionAuthorizationPersistValidator.class)),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getMatIconName()))
                            .must(() -> this.isNull(item.getStorageFileId()))
                            .failOn(PlanStatusDefinitionPersist._storageFileId).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PlanStatusDefinitionPersist._storageFileId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getStorageFileId()))
                            .must(() -> this.isEmpty(item.getMatIconName()))
                            .failOn(PlanStatusDefinitionPersist._matIconName).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PlanStatusDefinitionPersist._matIconName}, LocaleContextHolder.getLocale()))

            );
        }

        @Override
        public Errors validateObject(Object target) {
            return super.validateObject(target);
        }
    }
}
