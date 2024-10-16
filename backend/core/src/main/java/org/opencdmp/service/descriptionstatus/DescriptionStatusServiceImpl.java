package org.opencdmp.service.descriptionstatus;

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
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationEntity;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.deleter.DescriptionStatusDeleter;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionAuthorizationItemPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionAuthorizationPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusPersist;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DescriptionStatusServiceImpl implements DescriptionStatusService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionStatusServiceImpl.class));

    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;

    private final AuthorizationService authService;
    private final TenantEntityManager entityManager;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;

    public DescriptionStatusServiceImpl(BuilderFactory builderFactory, DeleterFactory deleterFactory, AuthorizationService authService, TenantEntityManager entityManager, ConventionService conventionService, MessageSource messageSource, XmlHandlingService xmlHandlingService) {
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;

        this.authService = authService;
        this.entityManager = entityManager;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
    }


    @Override
    public DescriptionStatus persist(DescriptionStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data description status").And("model", model).And("fields", fields));

        this.authService.authorizeForce(Permission.EditDescriptionStatus);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        DescriptionStatusEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionStatusEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        } else {
            data = new DescriptionStatusEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setInternalStatus(model.getInternalStatus());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDescriptionStatusDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());

        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(DescriptionStatusBuilder.class).build(BaseFieldSet.build(fields, DescriptionStatus._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting description status: {}", id);

        this.authService.authorizeForce(Permission.DeleteDescriptionStatus);

        this.deleterFactory.deleter(DescriptionStatusDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private DescriptionStatusDefinitionEntity buildDescriptionStatusDefinitionEntity(DescriptionStatusDefinitionPersist persist) {
        DescriptionStatusDefinitionEntity data = new DescriptionStatusDefinitionEntity();
        if (persist == null)
            return data;

        data.setAuthorization(this.buildDescriptionStatusDefinitionAuthorizationEntity(persist.getAuthorization()));

        return data;
    }

    private DescriptionStatusDefinitionAuthorizationEntity buildDescriptionStatusDefinitionAuthorizationEntity(DescriptionStatusDefinitionAuthorizationPersist persist) {
        DescriptionStatusDefinitionAuthorizationEntity data = new DescriptionStatusDefinitionAuthorizationEntity();
        if (persist == null)
            return data;

        data.setEdit(this.buildDescriptionStatusDefinitionAuthorizationItemEntity(persist.getEdit()));

        return data;
    }

    private DescriptionStatusDefinitionAuthorizationItemEntity buildDescriptionStatusDefinitionAuthorizationItemEntity(DescriptionStatusDefinitionAuthorizationItemPersist persist) {
        DescriptionStatusDefinitionAuthorizationItemEntity data = new DescriptionStatusDefinitionAuthorizationItemEntity();
        if (persist == null)
            return data;

        data.setPlanRoles(persist.getPlanRoles());
        data.setRoles(persist.getRoles());
        data.setAllowAnonymous(persist.getAllowAnonymous());
        data.setAllowAuthenticated(persist.getAllowAuthenticated());

        return data;
    }
}
