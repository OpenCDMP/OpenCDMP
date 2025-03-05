package org.opencdmp.service.descriptionworkflow;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionEntity;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionWorkflowEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.descriptionworkflow.DescriptionWorkflowBuilder;
import org.opencdmp.model.deleter.DescriptionWorkflowDeleter;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflow;
import org.opencdmp.model.persist.descriptionworkflow.DescriptionWorkflowDefinitionPersist;
import org.opencdmp.model.persist.descriptionworkflow.DescriptionWorkflowDefinitionTransitionPersist;
import org.opencdmp.model.persist.descriptionworkflow.DescriptionWorkflowPersist;
import org.opencdmp.query.DescriptionWorkflowQuery;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DescriptionWorkflowServiceImpl implements DescriptionWorkflowService{

    private final static LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowServiceImpl.class));

    private final AuthorizationService authService;
    private final ConventionService conventionService;
    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;
    private final TenantEntityManager entityManager;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final TenantScope tenantScope;
    private final QueryFactory queryFactory;

    public DescriptionWorkflowServiceImpl(AuthorizationService authService, ConventionService conventionService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory, DeleterFactory deleterFactory, TenantEntityManager entityManager, MessageSource messageSource, ErrorThesaurusProperties errors, TenantScope tenantScope, QueryFactory queryFactory) {
        this.authService = authService;
        this.conventionService = conventionService;
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;
        this.entityManager = entityManager;
        this.messageSource = messageSource;
        this.errors = errors;
        this.tenantScope = tenantScope;
        this.queryFactory = queryFactory;
    }


    @Override
    public DescriptionWorkflow persist(DescriptionWorkflowPersist model, FieldSet fields) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data description workflow").And("model", model).And("fields", fields));

        this.authService.authorizeForce(Permission.EditDescriptionWorkflow);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        DescriptionWorkflowEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionWorkflowEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new DescriptionWorkflowEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setUpdatedAt(Instant.now());
        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDescriptionWorkflowDefinitionEntity(model.getDefinition())));

        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(DescriptionWorkflowBuilder.class).build(BaseFieldSet.build(fields, DescriptionWorkflow._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws InvalidApplicationException {
        logger.debug("deleting description workflow: {}", id);

        this.authService.authorizeForce(Permission.DeleteDescriptionWorkflow);
        
        this.deleterFactory.deleter(DescriptionWorkflowDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private DescriptionWorkflowDefinitionEntity buildDescriptionWorkflowDefinitionEntity(DescriptionWorkflowDefinitionPersist persist) {
        DescriptionWorkflowDefinitionEntity data = new DescriptionWorkflowDefinitionEntity();
        if (persist == null) return data;

        data.setStartingStatusId(persist.getStartingStatusId());

        List<DescriptionWorkflowDefinitionTransitionEntity> transitionData = this.buildDescriptionWorkflowDefinitionTransitionEntities(persist.getStatusTransitions());
        data.setStatusTransitions(transitionData);

        return data;
    }

    private List<DescriptionWorkflowDefinitionTransitionEntity> buildDescriptionWorkflowDefinitionTransitionEntities(List<DescriptionWorkflowDefinitionTransitionPersist> persistData) {
        List<DescriptionWorkflowDefinitionTransitionEntity> data = new ArrayList<>();
        if (persistData == null || persistData.isEmpty())
            return data;

        for (DescriptionWorkflowDefinitionTransitionPersist persist : persistData) {
            DescriptionWorkflowDefinitionTransitionEntity d = new DescriptionWorkflowDefinitionTransitionEntity();
            d.setFromStatusId(persist.getFromStatusId());
            d.setToStatusId(persist.getToStatusId());

            data.add(d);
        }

        return data;
    }

    @Override
    public DescriptionWorkflowDefinitionEntity getActiveWorkFlowDefinition() throws InvalidApplicationException {

        DescriptionWorkflowQuery query = this.queryFactory.query(DescriptionWorkflowQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActives(IsActive.Active);
        if (this.tenantScope.isDefaultTenant()) query = query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        DescriptionWorkflowEntity entity = query.first();

        if (entity == null && !this.tenantScope.isDefaultTenant()) {
            query.clearTenantIds().tenantIsSet(false);
            entity = query.first();
        }

        if (entity == null) throw new MyApplicationException("Description workflow not found!");

        DescriptionWorkflowDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DescriptionWorkflowDefinitionEntity.class, entity.getDefinition());
        if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{entity.getId(), DescriptionWorkflowDefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        return definition;
    }
}
