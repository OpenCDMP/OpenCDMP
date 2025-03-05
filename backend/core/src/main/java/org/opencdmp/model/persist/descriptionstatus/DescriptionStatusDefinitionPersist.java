package org.opencdmp.model.persist.descriptionstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionStatusDefinitionPersist {

    private DescriptionStatusDefinitionAuthorizationPersist authorization;
    public final static String _authorization = "authorization";

    private List<DescriptionStatusAvailableActionType> availableActions;
    public final static String _availableActions = "availableActions";

    public final static String _matIconName = "matIconName";
    private  String matIconName;

    public final static String _storageFileId = "storageFileId";
    private UUID storageFileId;

    public final static String _statusColor = "statusColor";
    private  String statusColor;

    public DescriptionStatusDefinitionAuthorizationPersist getAuthorization() { return authorization; }
    public void setAuthorization(DescriptionStatusDefinitionAuthorizationPersist authorization) { this.authorization = authorization; }

    public List<DescriptionStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<DescriptionStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    @Component(DescriptionStatusDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusDefinitionPersistValidator extends BaseValidator<DescriptionStatusDefinitionPersist> {

        public final static String ValidatorName = "DescriptionStatusPersistValidation.DescriptionStatusDefinitionPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionStatusDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionStatusDefinitionPersist> modelClass() {
            return DescriptionStatusDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getAuthorization()))
                            .failOn(DescriptionStatusDefinitionPersist._authorization).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusDefinitionPersist._authorization}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAuthorization()))
                            .on(DescriptionStatusDefinitionPersist._authorization)
                            .over(item.getAuthorization())
                            .using(() -> this.validatorFactory.validator(DescriptionStatusDefinitionAuthorizationPersist.DescriptionStatusDefinitionAuthorizationPersistValidator.class)),
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
    }
}
