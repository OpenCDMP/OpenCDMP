package org.opencdmp.service.planworkflow;

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
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionEntity;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanWorkflowEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.planworkflow.PlanWorkflowBuilder;
import org.opencdmp.model.deleter.PlanWorkflowDeleter;
import org.opencdmp.model.persist.planworkflow.PlanWorkflowDefinitionPersist;
import org.opencdmp.model.persist.planworkflow.PlanWorkflowDefinitionTransitionPersist;
import org.opencdmp.model.persist.planworkflow.PlanWorkflowPersist;
import org.opencdmp.model.planworkflow.PlanWorkflow;
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
public class PlanWorkflowServiceImpl implements PlanWorkflowService {

    private final static LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanWorkflowServiceImpl.class));

    private final AuthorizationService authorizationService;
    private final ConventionService conventionService;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManager entityManager;
    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    public PlanWorkflowServiceImpl(AuthorizationService authorizationService, ConventionService conventionService, XmlHandlingService xmlHandlingService, TenantEntityManager entityManager, BuilderFactory builderFactory, DeleterFactory deleterFactory, MessageSource messageSource, ErrorThesaurusProperties errors) {
        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
        this.xmlHandlingService = xmlHandlingService;
        this.entityManager = entityManager;
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;
        this.messageSource = messageSource;
        this.errors = errors;
    }

    @Override
    public PlanWorkflow persist(PlanWorkflowPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data plan workflow").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanWorkflow);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanWorkflowEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PlanWorkflowEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanWorkflow.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new PlanWorkflowEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildPlanWorkflowDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());

        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(PlanWorkflowBuilder.class).build(BaseFieldSet.build(fields, PlanWorkflow._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting plan workflow: {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanWorkflow);

        this.deleterFactory.deleter(PlanWorkflowDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private PlanWorkflowDefinitionEntity buildPlanWorkflowDefinitionEntity(PlanWorkflowDefinitionPersist persist) {
        PlanWorkflowDefinitionEntity data = new PlanWorkflowDefinitionEntity();
        if (persist == null)
            return data;

        data.setStartingStatusId(persist.getStartingStatusId());

        if (persist.getStatusTransitions() != null) {
            List<PlanWorkflowDefinitionTransitionEntity> transitionsData = this.buildPlanWorkflowDefinitionTransitionEntities(persist.getStatusTransitions());
            data.setStatusTransitions(transitionsData);
        }

        return data;
    }

    private List<PlanWorkflowDefinitionTransitionEntity> buildPlanWorkflowDefinitionTransitionEntities(List<PlanWorkflowDefinitionTransitionPersist> persistData) {
        List<PlanWorkflowDefinitionTransitionEntity> data = new ArrayList<>();
        if (persistData == null || persistData.isEmpty())
            return data;

        for (PlanWorkflowDefinitionTransitionPersist persist : persistData) {
            PlanWorkflowDefinitionTransitionEntity d = new PlanWorkflowDefinitionTransitionEntity();
            d.setFromStatusId(persist.getFromStatusId());
            d.setToStatusId(persist.getToStatusId());

            data.add(d);
        }

        return data;
    }
}
