package org.opencdmp.service.language;

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
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LanguageEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.Language;
import org.opencdmp.model.builder.LanguageBuilder;
import org.opencdmp.model.deleter.LanguageDeleter;
import org.opencdmp.model.persist.LanguagePersist;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.planblueprint.PlanBlueprintServiceImpl;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final StorageFileService storageFileService;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;


    public LanguageServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            ConventionService conventionService, MessageSource messageSource, ErrorThesaurusProperties errors, StorageFileService storageFileService, UsageLimitService usageLimitService, AccountingService accountingService){
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
        this.storageFileService = storageFileService;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
    }


    public Language persist(LanguagePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditLanguage);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        LanguageEntity data;
        if (isUpdate) {
            data = this.entityManager.find(LanguageEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Language.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.LANGUAGE_COUNT);

            data = new LanguageEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }
        data.setCode(model.getCode());
        data.setPayload(model.getPayload() != null && !model.getPayload().isEmpty() ? model.getPayload() : null);
        data.setOrdinal(model.getOrdinal());
        data.setUpdatedAt(Instant.now());
        if (isUpdate) this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.LANGUAGE_COUNT.getValue());
        }

        this.entityManager.flush();

        return this.builderFactory.builder(LanguageBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Language._id), data);
    }

    public String getPayload(String code) throws IOException {
        this.authorizationService.authorizeForce(Permission.BrowseLanguage);

        byte[] content = this.storageFileService.getLanguage(code);

        return new String(content, StandardCharsets.UTF_8);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteLanguage);

        this.deleterFactory.deleter(LanguageDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}
