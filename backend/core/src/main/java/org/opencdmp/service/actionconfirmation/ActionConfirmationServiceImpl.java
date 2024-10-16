package org.opencdmp.service.actionconfirmation;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.actionconfirmation.PlanInvitationEntity;
import org.opencdmp.commons.types.actionconfirmation.MergeAccountConfirmationEntity;
import org.opencdmp.commons.types.actionconfirmation.RemoveCredentialRequestEntity;
import org.opencdmp.commons.types.actionconfirmation.UserInviteToTenantRequestEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ActionConfirmationEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.actionconfirmation.ActionConfirmation;
import org.opencdmp.model.builder.actionconfirmation.ActionConfirmationBuilder;
import org.opencdmp.model.deleter.ActionConfirmationDeleter;
import org.opencdmp.model.persist.ActionConfirmationPersist;
import org.opencdmp.model.persist.UserInviteToTenantRequestPersist;
import org.opencdmp.model.persist.actionconfirmation.PlanInvitationPersist;
import org.opencdmp.model.persist.actionconfirmation.MergeAccountConfirmationPersist;
import org.opencdmp.model.persist.actionconfirmation.RemoveCredentialRequestPersist;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.service.planblueprint.PlanBlueprintServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class ActionConfirmationServiceImpl implements ActionConfirmationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;
    private final ErrorThesaurusProperties errors;
    private final UserScope userScope;

    public ActionConfirmationServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
		    ConventionService conventionService, MessageSource messageSource,
		    XmlHandlingService xmlHandlingService, ErrorThesaurusProperties errors, UserScope userScope) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.errors = errors;
	    this.userScope = userScope;
    }


    public ActionConfirmation persist(ActionConfirmationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));


        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        ActionConfirmationEntity data;
        if (isUpdate) {
            data = this.entityManager.find(ActionConfirmationEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {

            data = new ActionConfirmationEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setCreatedById(this.userScope.getUserId());
        }
        this.authorizationService.authorizeAtLeastOneForce(List.of(new OwnedResource(data.getCreatedById())), Permission.EditActionConfirmation);
        
        data.setToken(model.getToken());
        data.setType(model.getType());
        data.setStatus(model.getStatus());
        data.setExpiresAt(model.getExpiresAt());
        switch (model.getType()){
            case MergeAccount ->  data.setData(this.xmlHandlingService.toXmlSafe(this.buildMergeAccountConfirmationEntity(model.getMergeAccountConfirmation())));
            case PlanInvitation ->  data.setData(this.xmlHandlingService.toXmlSafe(this.buildPlanInvitationEntity(model.getPlanInvitation())));
            case RemoveCredential ->  data.setData(this.xmlHandlingService.toXmlSafe(this.buildMergeAccountConfirmationEntity(model.getRemoveCredentialRequest())));
            case UserInviteToTenant ->  data.setData(this.xmlHandlingService.toXmlSafe(this.buildUserInviteToTenantRequestEntity(model.getUserInviteToTenantRequest())));
            default -> throw new InternalError("unknown type: " + model.getType()); 
        }
        data.setUpdatedAt(Instant.now());

        if (isUpdate) this.entityManager.merge(data);
        else this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(ActionConfirmationBuilder.class).build(BaseFieldSet.build(fields, ActionConfirmation._id), data);
    }

    private @NotNull PlanInvitationEntity buildPlanInvitationEntity(PlanInvitationPersist persist){
        PlanInvitationEntity data = new PlanInvitationEntity();
        if (persist == null) return data;

        data.setEmail(persist.getEmail());
        data.setRole(persist.getRole());
        data.setPlanId(persist.getPlanId());
        data.setSectionId(persist.getSectionId());

        return data;
    }

    private @NotNull MergeAccountConfirmationEntity buildMergeAccountConfirmationEntity(MergeAccountConfirmationPersist persist){
        MergeAccountConfirmationEntity data = new MergeAccountConfirmationEntity();
        if (persist == null) return data;

        data.setEmail(persist.getEmail());

        return data;
    }

    private @NotNull RemoveCredentialRequestEntity buildMergeAccountConfirmationEntity(RemoveCredentialRequestPersist persist){
        RemoveCredentialRequestEntity data = new RemoveCredentialRequestEntity();
        if (persist == null) return data;

        data.setCredentialId(persist.getCredentialId());

        return data;
    }

    private @NotNull UserInviteToTenantRequestEntity buildUserInviteToTenantRequestEntity(UserInviteToTenantRequestPersist persist){
        UserInviteToTenantRequestEntity data = new UserInviteToTenantRequestEntity();
        if (persist == null) return data;

        data.setEmail(persist.getEmail());
        data.setTenantCode(persist.getTenantCode());
        data.setRoles(persist.getRoles());

        return data;
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteActionConfirmation);

        this.deleterFactory.deleter(ActionConfirmationDeleter.class).deleteAndSaveByIds(List.of(id));
    }

}
