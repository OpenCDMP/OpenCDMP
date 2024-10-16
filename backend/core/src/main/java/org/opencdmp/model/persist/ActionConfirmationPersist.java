package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.ActionConfirmationStatus;
import org.opencdmp.commons.enums.ActionConfirmationType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.actionconfirmation.PlanInvitationPersist;
import org.opencdmp.model.persist.actionconfirmation.MergeAccountConfirmationPersist;
import org.opencdmp.model.persist.actionconfirmation.RemoveCredentialRequestPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ActionConfirmationPersist {

    private UUID id;

    private ActionConfirmationType type;

    private static final String _type = "type";

    private ActionConfirmationStatus status;

    private static final String _status = "status";

    private String token;

    private static final String _token = "token";

    private PlanInvitationPersist planInvitation;

    private static final String _planInvitation = "planInvitation";

    private MergeAccountConfirmationPersist mergeAccountConfirmation;

    private static final String _mergeAccountConfirmation = "mergeAccountConfirmation";

    private RemoveCredentialRequestPersist removeCredentialRequest;

    private static final String _removeCredentialRequest = "removeCredentialRequest";

    private UserInviteToTenantRequestPersist userInviteToTenantRequestPersist;

    private static final String _userInviteToTenantRequest = "userInviteToTenantRequest";

    private Instant expiresAt;

    private static final String _expiresAt = "expiresAt";

    private String hash;

    private static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ActionConfirmationType getType() {
        return type;
    }

    public void setType(ActionConfirmationType type) {
        this.type = type;
    }

    public ActionConfirmationStatus getStatus() {
        return status;
    }

    public void setStatus(ActionConfirmationStatus status) {
        this.status = status;
    }

    public PlanInvitationPersist getPlanInvitation() {
        return planInvitation;
    }

    public void setPlanInvitation(PlanInvitationPersist planInvitation) {
        this.planInvitation = planInvitation;
    }

    public MergeAccountConfirmationPersist getMergeAccountConfirmation() {
        return mergeAccountConfirmation;
    }

    public void setMergeAccountConfirmation(MergeAccountConfirmationPersist mergeAccountConfirmation) {
        this.mergeAccountConfirmation = mergeAccountConfirmation;
    }

    public RemoveCredentialRequestPersist getRemoveCredentialRequest() {
        return removeCredentialRequest;
    }

    public void setRemoveCredentialRequest(RemoveCredentialRequestPersist removeCredentialRequest) {
        this.removeCredentialRequest = removeCredentialRequest;
    }

    public UserInviteToTenantRequestPersist getUserInviteToTenantRequest() {
        return userInviteToTenantRequestPersist;
    }

    public void setUserInviteToTenantRequest(UserInviteToTenantRequestPersist userInviteToTenantRequestPersist) {
        this.userInviteToTenantRequestPersist = userInviteToTenantRequestPersist;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(ActionConfirmationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ActionConfirmationPersistValidator extends BaseValidator<ActionConfirmationPersist> {

        public static final String ValidatorName = "ActionConfirmationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        public ActionConfirmationPersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ActionConfirmationPersist> modelClass() {
            return ActionConfirmationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ActionConfirmationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(ActionConfirmationPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(ActionConfirmationPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(ActionConfirmationPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(ActionConfirmationPersist._status).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._status}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getToken()))
                            .failOn(ActionConfirmationPersist._token).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._token}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getExpiresAt()))
                            .failOn(ActionConfirmationPersist._expiresAt).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._expiresAt}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> ActionConfirmationType.MergeAccount.equals(item.getType()))
                            .must(() -> !this.isNull(item.getMergeAccountConfirmation()))
                            .failOn(ActionConfirmationPersist._mergeAccountConfirmation).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._mergeAccountConfirmation}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> ActionConfirmationType.PlanInvitation.equals(item.getType()))
                            .must(() -> !this.isNull(item.getPlanInvitation()))
                            .failOn(ActionConfirmationPersist._planInvitation).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._planInvitation}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> ActionConfirmationType.RemoveCredential.equals(item.getType()))
                            .must(() -> !this.isNull(item.getRemoveCredentialRequest()))
                            .failOn(ActionConfirmationPersist._removeCredentialRequest).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._removeCredentialRequest}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> ActionConfirmationType.UserInviteToTenant.equals(item.getType()))
                            .must(() -> !this.isNull(item.getUserInviteToTenantRequest()))
                            .failOn(ActionConfirmationPersist._userInviteToTenantRequest).failWith(messageSource.getMessage("Validation_Required", new Object[]{ActionConfirmationPersist._userInviteToTenantRequest}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getPlanInvitation()))
                            .on(ActionConfirmationPersist._planInvitation)
                            .over(item.getPlanInvitation())
                            .using(() -> this.validatorFactory.validator(PlanInvitationPersist.PlanInvitationPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getMergeAccountConfirmation()))
                            .on(ActionConfirmationPersist._mergeAccountConfirmation)
                            .over(item.getMergeAccountConfirmation())
                            .using(() -> this.validatorFactory.validator(MergeAccountConfirmationPersist.MergeAccountConfirmationPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getRemoveCredentialRequest()))
                            .on(ActionConfirmationPersist._removeCredentialRequest)
                            .over(item.getRemoveCredentialRequest())
                            .using(() -> this.validatorFactory.validator(RemoveCredentialRequestPersist.RemoveCredentialRequestPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getUserInviteToTenantRequest()))
                            .on(ActionConfirmationPersist._userInviteToTenantRequest)
                            .over(item.getUserInviteToTenantRequest())
                            .using(() -> this.validatorFactory.validator(UserInviteToTenantRequestPersist.UserInviteToTenantRequestPersistValidator.class))
            );
        }

    }

}



