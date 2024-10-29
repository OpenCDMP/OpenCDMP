package org.opencdmp.service.supportivematerial;

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
import jakarta.xml.bind.JAXBException;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.SupportiveMaterialEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.SupportiveMaterial;
import org.opencdmp.model.builder.SupportiveMaterialBuilder;
import org.opencdmp.model.deleter.SupportiveMaterialDeleter;
import org.opencdmp.model.persist.SupportiveMaterialPersist;
import org.opencdmp.query.SupportiveMaterialQuery;
import org.opencdmp.service.planblueprint.PlanBlueprintServiceImpl;
import org.opencdmp.service.storage.StorageFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class SupportiveMaterialServiceImpl implements SupportiveMaterialService{

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));
    private static final Logger log = LoggerFactory.getLogger(PlanBlueprintServiceImpl.class);
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final QueryFactory queryFactory;
    private final SupportiveMaterialCacheService supportiveMaterialCacheService;
    private final StorageFileService storageFileService;
    private final TenantEntityManager tenantEntityManager;

    public SupportiveMaterialServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            ConventionService conventionService, MessageSource messageSource, QueryFactory queryFactory,
            SupportiveMaterialCacheService supportiveMaterialCacheService, StorageFileService storageFileService, TenantEntityManager tenantEntityManager
    ) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
        this.supportiveMaterialCacheService = supportiveMaterialCacheService;
	    this.storageFileService = storageFileService;
        this.tenantEntityManager = tenantEntityManager;
    }

    public byte[] loadFromFile(String language, SupportiveMaterialFieldType type)  {
        SupportiveMaterialCacheService.SupportiveMaterialCacheValue supportiveMaterialCacheItem = this.supportiveMaterialCacheService.lookup(this.supportiveMaterialCacheService.buildKey(language, type));
        
        if(supportiveMaterialCacheItem == null){
            byte[] content = this.storageFileService.getSupportiveMaterial(type, language);
            if (content == null) throw new MyNotFoundException("Material not found");
            
            supportiveMaterialCacheItem = new SupportiveMaterialCacheService.SupportiveMaterialCacheValue(language, type, content);
            this.supportiveMaterialCacheService.put(supportiveMaterialCacheItem);
        }

        return supportiveMaterialCacheItem.getContent();
    }

    public SupportiveMaterial persist(SupportiveMaterialPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditSupportiveMaterial);

        List<SupportiveMaterialEntity> existingSupportiveMaterials;
        try {
            this.tenantEntityManager.loadExplicitTenantFilters();
            existingSupportiveMaterials = this.queryFactory.query(SupportiveMaterialQuery.class).disableTracking().isActive(IsActive.Active).collect();

        } catch (InvalidApplicationException e) {
            log.error(e.getMessage(), e);
            throw new MyApplicationException(e.getMessage());
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        SupportiveMaterialEntity d;
        if (isUpdate) {
            d = this.entityManager.find(SupportiveMaterialEntity.class, model.getId());
            if (d == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), SupportiveMaterial.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        } else {
            if (existingSupportiveMaterials != null && !existingSupportiveMaterials.isEmpty() && existingSupportiveMaterials.stream().filter(x -> x.getLanguageCode().equals(model.getLanguageCode()) && x.getType().equals(model.getType())).findFirst().orElse(null) != null) throw new MyApplicationException("Could not create a new Data with same type and lang code !");;

            d = new SupportiveMaterialEntity();
            d.setId(UUID.randomUUID());
            d.setIsActive(IsActive.Active);
            d.setCreatedAt(Instant.now());
        }

        d.setType(model.getType());
        d.setLanguageCode(model.getLanguageCode());
        d.setPayload(model.getPayload());
        d.setUpdatedAt(Instant.now());

        if (isUpdate) this.entityManager.merge(d);
        else this.entityManager.persist(d);

        this.entityManager.flush();

        return this.builderFactory.builder(SupportiveMaterialBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, SupportiveMaterial._id), d);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteSupportiveMaterial);

        this.deleterFactory.deleter(SupportiveMaterialDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}
