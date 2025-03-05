package org.opencdmp.service.lock;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LockEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.Lock;
import org.opencdmp.model.LockStatus;
import org.opencdmp.model.builder.LockBuilder;
import org.opencdmp.model.deleter.LockDeleter;
import org.opencdmp.model.persist.LockPersist;
import org.opencdmp.model.persist.UnlockMultipleTargetsPersist;
import org.opencdmp.query.LockQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LockServiceImpl implements LockService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(LockServiceImpl.class));
    private final Comparator<LockEntity> compareByTouchedAt = Comparator.comparing(o ->  o.getTouchedAt());
    private final TenantEntityManager entityManager;
    private final UserScope userScope;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final LockProperties lockProperties;

    @Autowired
    public LockServiceImpl(
            TenantEntityManager entityManager,
            UserScope userScope,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            QueryFactory queryFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            ErrorThesaurusProperties errors, AuthorizationContentResolver authorizationContentResolver, LockProperties lockProperties) {
        this.entityManager = entityManager;
        this.userScope = userScope;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
	    this.authorizationContentResolver = authorizationContentResolver;
        this.lockProperties = lockProperties;
    }

    public Lock persist(LockPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(model.getTarget());
        AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(model.getTarget());
        AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(model.getTarget());
        this.authorizationService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.EditLock);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        LockEntity data;
        if (isUpdate) {
            data = this.entityManager.find(LockEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Lock.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!data.getLockedBy().equals(this.userScope.getUserId())) throw new MyApplicationException("Is not locked by that user");
            if (!this.conventionService.hashValue(data.getTouchedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new LockEntity();
            data.setId(UUID.randomUUID());
            data.setLockedAt(Instant.now());
            data.setLockedBy(this.userScope.getUserId());
        }

        data.setTarget(model.getTarget());
        data.setTargetType(model.getTargetType());
        data.setTouchedAt(Instant.now());

        if (isUpdate) this.entityManager.merge(data);
        else  this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(LockBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Lock._id), data);
    }

    public LockStatus isLocked(UUID target, FieldSet fields) throws InvalidApplicationException {
        LockStatus lockStatus = new LockStatus();
        LockEntity lock = this.queryFactory.query(LockQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).targetIds(target).first();

        if (lock == null) {
            lockStatus.setStatus(false);
            return lockStatus;
        }

        if (lock.getLockedBy().equals(this.userScope.getUserId())) lockStatus.setStatus(false);
        else {
            if (new Date().getTime() - Date.from(lock.getTouchedAt()).getTime() > this.lockProperties.getLockInterval()) {
                lockStatus.setStatus(false);
                this.deleterFactory.deleter(LockDeleter.class).deleteAndSaveByIds(List.of(lock.getId()));
                //this.deleteAndSave(lock.getId(), lock.getTarget());
            } else lockStatus.setStatus(true);
        }

        lockStatus.setLock(this.builderFactory.builder(LockBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Lock._id), lock));
        return lockStatus;
    }

    public void lock(UUID target, LockTargetType targetType) throws InvalidApplicationException {
        LockEntity lock = this.queryFactory.query(LockQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).targetIds(target).first();
        if (lock == null) {
            this.persist(new LockPersist(target, targetType), null);
        }else{
            if (!lock.getLockedBy().equals(this.userScope.getUserId())) throw new MyApplicationException("Entity is already locked");
            this.touch(target);
        }

    }

    public boolean checkLock(UUID target, LockTargetType targetType) throws InvalidApplicationException {
        LockEntity lock = this.queryFactory.query(LockQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).targetIds(target).first();
        if (lock == null) {
            this.persist(new LockPersist(target, targetType), null);
        }else{
            if (!lock.getLockedBy().equals(this.userScope.getUserId())) return false;
            this.touch(target);
        }

        return true;

    }

    public void touch(UUID target) throws InvalidApplicationException {
        LockEntity lock = this.queryFactory.query(LockQuery.class).authorize(AuthorizationFlags.AllExceptPublic).targetIds(target).first();

        if (lock == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{target, Lock.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!lock.getLockedBy().equals(this.userScope.getUserId())) throw new MyApplicationException("Only the user who created that lock can touch it");

        lock.setTouchedAt(Instant.now());
        this.entityManager.merge(lock);
        this.entityManager.flush();
    }

    public void unlock(UUID target) throws InvalidApplicationException {
        LockEntity lock = this.queryFactory.query(LockQuery.class).authorize(AuthorizationFlags.AllExceptPublic).targetIds(target).first();

        if (lock == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{target, Lock.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!lock.getLockedBy().equals(this.userScope.getUserId())) {
            throw new InvalidApplicationException("Only the user who created that lock can delete it");
        }
        this.deleteAndSave(lock.getId(), lock.getTarget());
    }

    public void unlockMultipleTargets(UnlockMultipleTargetsPersist model) throws InvalidApplicationException {
        for (UUID target: model.getTargetIds()) {
            AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(target);
            AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(target);
            AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(target);
            this.authorizationService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.DeleteLock);
        }

        List<LockEntity> locks = this.queryFactory.query(LockQuery.class).authorize(AuthorizationFlags.AllExceptPublic).targetIds(model.getTargetIds()).collect();

        if (locks == null || this.conventionService.isListNullOrEmpty(locks)) throw new MyApplicationException("Locks not found");

        if (locks.stream().filter(x -> x.getLockedBy().equals(this.userScope.getUserIdSafe())).findFirst().orElse(null) == null) {
            throw new MyApplicationException("Only the user who created lock can delete it");
        }

        this.deleterFactory.deleter(LockDeleter.class).deleteAndSave(locks);
    }

    public void deleteAndSave(UUID id, UUID target) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(target);
        AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(target);
        AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(target);
        this.authorizationService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.DeleteLock);

        this.deleterFactory.deleter(LockDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}

