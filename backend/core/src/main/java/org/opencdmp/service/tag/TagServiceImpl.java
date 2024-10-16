package org.opencdmp.service.tag;

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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TagEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.TagTouchedEvent;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.TagBuilder;
import org.opencdmp.model.deleter.TagDeleter;
import org.opencdmp.model.persist.TagPersist;
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
public class TagServiceImpl implements TagService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TagServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final EventBroker eventBroker;

    private final UserScope userScope;


    @Autowired
    public TagServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            UserScope userScope,
            EventBroker eventBroker) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.eventBroker = eventBroker;
        this.userScope = userScope;
    }

    public Tag persist(TagPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data tag").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditTag);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        TagEntity data;
        if (isUpdate) {
            data = this.entityManager.find(TagEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Tag.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new TagEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setCreatedById(this.userScope.getUserId());
        }

        data.setLabel(model.getLabel());
        data.setUpdatedAt(Instant.now());
        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        this.eventBroker.emit(new TagTouchedEvent(data.getId()));
        return this.builderFactory.builder(TagBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Tag._id), data);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting tag: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteTag);

        this.deleterFactory.deleter(TagDeleter.class).deleteAndSaveByIds(List.of(id));
    }

}

