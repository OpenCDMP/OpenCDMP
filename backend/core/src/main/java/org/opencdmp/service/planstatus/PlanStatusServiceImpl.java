package org.opencdmp.service.planstatus;

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
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.deleter.PlanStatusDeleter;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionAuthorizationItemPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionAuthorizationPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusPersist;
import org.opencdmp.model.planstatus.PlanStatus;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlanStatusServiceImpl implements PlanStatusService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanStatusServiceImpl.class));

    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;

    private final AuthorizationService authorizationService;
    private final ConventionService conventionService;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManager entityManager;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;

    public PlanStatusServiceImpl(BuilderFactory builderFactory, DeleterFactory deleterFactory, AuthorizationService authorizationService, ConventionService conventionService, XmlHandlingService xmlHandlingService, TenantEntityManager entityManager, MessageSource messageSource, ErrorThesaurusProperties errors, EventBroker eventBroker) {
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;

        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
        this.xmlHandlingService = xmlHandlingService;
        this.entityManager = entityManager;
        this.messageSource = messageSource;
        this.errors = errors;
    }

    @Override
    public PlanStatus persist(PlanStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data plan status").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanStatus);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanStatusEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PlanStatusEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new PlanStatusEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setInternalStatus(model.getInternalStatus());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildPlanStatusDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());

        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(PlanStatusBuilder.class).build(BaseFieldSet.build(fields, PlanStatus._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting plan status: {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanStatus);

        this.deleterFactory.deleter(PlanStatusDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private @NotNull PlanStatusDefinitionEntity buildPlanStatusDefinitionEntity(PlanStatusDefinitionPersist persist) {
        PlanStatusDefinitionEntity data = new PlanStatusDefinitionEntity();
        if (persist == null)
            return data;

        if (persist.getAuthorization() != null) {
            PlanStatusDefinitionAuthorizationEntity definitionAuthorizationData = this.buildPlanStatusDefinitionAuthorizationEntity(persist.getAuthorization());
            data.setAuthorization(definitionAuthorizationData);
        }
        return data;
    }

    private @NotNull PlanStatusDefinitionAuthorizationEntity buildPlanStatusDefinitionAuthorizationEntity(PlanStatusDefinitionAuthorizationPersist persist) {
        PlanStatusDefinitionAuthorizationEntity data = new PlanStatusDefinitionAuthorizationEntity();
        if (persist == null)
            return data;

        if (persist.getEdit() != null) {
            PlanStatusDefinitionAuthorizationItemEntity definitionAuthorizationData = this.buildPlanStatusDefinitionAuthorizationItemEntity(persist.getEdit());
            data.setEdit(definitionAuthorizationData);
        }
        return data;
    }

    private @NotNull PlanStatusDefinitionAuthorizationItemEntity buildPlanStatusDefinitionAuthorizationItemEntity(PlanStatusDefinitionAuthorizationItemPersist persist) {
        PlanStatusDefinitionAuthorizationItemEntity data = new PlanStatusDefinitionAuthorizationItemEntity();
        if (persist == null)
            return data;

        data.setPlanRoles(persist.getPlanRoles());
        data.setRoles(persist.getRoles());
        data.setAllowAuthenticated(persist.getAllowAuthenticated());
        data.setAllowAnonymous(persist.getAllowAnonymous());

        return data;
    }
}
