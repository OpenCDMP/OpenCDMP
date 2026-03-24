package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.commons.enums.PlanUpdateRequestStatus;
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

public class PlanUpdateRequestPersist {

    private UUID id;
    public static final String _id = "id";

    private UUID planId;
    public static final String _planId = "planId";

    private UUID submitterId;
    public static final String _submitterId = "submitterId";

    private PlanUpdateRequestActionType actionType;
    public static final String _actionType = "actionType";

    private PlanUpdateRequestStatus status;
    public static final String _status = "status";

    private Object data;
    public static final String _data = "data";

    private String sourceCode;
    public static final String _sourceCode = "sourceCode";

    private String hash;
    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(UUID submitterId) {
        this.submitterId = submitterId;
    }

    public PlanUpdateRequestActionType getActionType() {
        return actionType;
    }

    public void setActionType(PlanUpdateRequestActionType actionType) {
        this.actionType = actionType;
    }

    public PlanUpdateRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PlanUpdateRequestStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(PlanUpdateRequestPersist.PlanUpdateRequestPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanUpdateRequestPersistValidator extends BaseValidator<PlanUpdateRequestPersist> {

        public static final String ValidatorName = "PlanUpdateRequestPersist";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanUpdateRequestPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanUpdateRequestPersist> modelClass() {
            return PlanUpdateRequestPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanUpdateRequestPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(PlanUpdateRequestPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(PlanUpdateRequestPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getSubmitterId() != null)
                            .must(() -> this.isValidGuid(item.getSubmitterId()))
                            .failOn(PlanUpdateRequestPersist._submitterId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._submitterId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getData()))
                            .failOn(PlanUpdateRequestPersist._data).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._data}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(PlanUpdateRequestPersist._status).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._status}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getActionType()))
                            .failOn(PlanUpdateRequestPersist._actionType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._actionType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSourceCode()))
                            .failOn(PlanUpdateRequestPersist._sourceCode).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._sourceCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getActionType() != null && item.getActionType().equals(PlanUpdateRequestActionType.UpdatePlan))
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(PlanUpdateRequestPersist._planId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestPersist._planId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
