package org.opencdmp.service.entitydoi;

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
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EntityDoiTouchedEvent;
import org.opencdmp.event.EventBroker;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.builder.EntityDoiBuilder;
import org.opencdmp.model.deleter.EntityDoiDeleter;
import org.opencdmp.model.persist.EntityDoiPersist;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EntityDoiServiceImpl implements EntityDoiService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EntityDoiServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final EventBroker eventBroker;

    @Autowired
    public EntityDoiServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            EventBroker eventBroker) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.eventBroker = eventBroker;
    }

    public EntityDoi persist(EntityDoiPersist model, boolean disableAuthorization, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data EntityDoi").And("model", model).And("fields", fields));

        if (!disableAuthorization) this.authorizationService.authorizeForce(Permission.EditEntityDoi);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        EntityDoiEntity data;
        if (isUpdate) {
            data = this.entityManager.find(EntityDoiEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), EntityDoi.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new EntityDoiEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setEntityType(EntityType.Plan);
        data.setEntityId(model.getEntityId());
        data.setRepositoryId(model.getRepositoryId());
        data.setDoi(model.getDoi());
        data.setUpdatedAt(Instant.now());
        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        this.eventBroker.emit(new EntityDoiTouchedEvent(data.getId()));
        return this.builderFactory.builder(EntityDoiBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, EntityDoi._id), data);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting dataset: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteEntityDoi);

        this.deleterFactory.deleter(EntityDoiDeleter.class).deleteAndSaveByIds(List.of(id));
    }

}
