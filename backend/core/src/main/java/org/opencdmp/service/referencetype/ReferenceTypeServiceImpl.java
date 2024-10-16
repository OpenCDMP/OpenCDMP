package org.opencdmp.service.referencetype;

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
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.types.externalfetcher.*;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.commons.types.referencetype.ReferenceTypeFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.deleter.ReferenceTypeDeleter;
import org.opencdmp.model.persist.ReferenceTypePersist;
import org.opencdmp.model.persist.externalfetcher.*;
import org.opencdmp.model.persist.referencetypedefinition.ReferenceTypeDefinitionPersist;
import org.opencdmp.model.persist.referencetypedefinition.ReferenceTypeFieldPersist;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.ReferenceTypeQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.usagelimit.UsageLimitService;
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
public class ReferenceTypeServiceImpl implements ReferenceTypeService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceTypeServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;
    private final ErrorThesaurusProperties errors;
    private final QueryFactory queryFactory;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;

    public ReferenceTypeServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            ConventionService conventionService, MessageSource messageSource,
            XmlHandlingService xmlHandlingService, ErrorThesaurusProperties errors, QueryFactory queryFactory, UsageLimitService usageLimitService, AccountingService accountingService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.errors = errors;
	    this.queryFactory = queryFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
    }


    public ReferenceType persist(ReferenceTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditReferenceType);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        ReferenceTypeEntity data;
        if (isUpdate) {
            data = this.entityManager.find(ReferenceTypeEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT);

            data = new ReferenceTypeEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setCode(model.getCode());
        data.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());

        if (isUpdate) this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT.getValue());
        }

        this.entityManager.flush();

        if (!isUpdate) {
            Long referenceTypesWithThisCode = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(data.getCode())
                    .count();

            if (referenceTypesWithThisCode > 0) throw new MyValidationException(this.errors.getReferenceTypeCodeExists().getCode(), this.errors.getReferenceTypeCodeExists().getMessage());
        }

        return this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, ReferenceType._id), data);
    }

    private @NotNull ReferenceTypeDefinitionEntity buildDefinitionEntity(ReferenceTypeDefinitionPersist persist){
        ReferenceTypeDefinitionEntity data = new ReferenceTypeDefinitionEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())){
            data.setFields(new ArrayList<>());
            for (ReferenceTypeFieldPersist fieldPersist: persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(persist.getSources())){
            data.setSources(new ArrayList<>());
            for (ExternalFetcherBaseSourceConfigurationPersist sourceBaseConfigPersist: persist.getSources()) {
                data.getSources().add(this.buildSourceBaseConfigEntity(sourceBaseConfigPersist));
            }
        }

        return data;
    }

    private @NotNull ReferenceTypeFieldEntity buildFieldEntity(ReferenceTypeFieldPersist persist){
        ReferenceTypeFieldEntity data = new ReferenceTypeFieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setLabel(persist.getLabel());
        data.setDescription(persist.getDescription());
        data.setDataType(persist.getDataType());

        return data;
    }

    private @NotNull ExternalFetcherBaseSourceConfigurationEntity buildSourceBaseConfigEntity(ExternalFetcherBaseSourceConfigurationPersist persist){
        if (persist == null) return new ExternalFetcherApiSourceConfigurationEntity();

        ExternalFetcherBaseSourceConfigurationEntity data;

        if (ExternalFetcherSourceType.API.equals(persist.getType())) {
            ExternalFetcherApiSourceConfigurationEntity apiEntity = new ExternalFetcherApiSourceConfigurationEntity();

            apiEntity.setUrl(((ExternalFetcherApiSourceConfigurationPersist) persist).getUrl());
            if (((ExternalFetcherApiSourceConfigurationPersist) persist).getResults() != null ) {
                apiEntity.setResults(this.buildResultsConfigEntity(((ExternalFetcherApiSourceConfigurationPersist) persist).getResults()));
            }
            apiEntity.setPaginationPath(((ExternalFetcherApiSourceConfigurationPersist) persist).getPaginationPath());
            apiEntity.setContentType(((ExternalFetcherApiSourceConfigurationPersist) persist).getContentType());
            apiEntity.setFirstPage(((ExternalFetcherApiSourceConfigurationPersist) persist).getFirstPage());
            apiEntity.setHttpMethod(((ExternalFetcherApiSourceConfigurationPersist) persist).getHttpMethod());
            apiEntity.setRequestBody(((ExternalFetcherApiSourceConfigurationPersist) persist).getRequestBody());
            apiEntity.setFilterType(((ExternalFetcherApiSourceConfigurationPersist) persist).getFilterType());
            if (((ExternalFetcherApiSourceConfigurationPersist) persist).getAuth() != null) {
                apiEntity.setAuth(this.buildAuthConfigEntity(((ExternalFetcherApiSourceConfigurationPersist) persist).getAuth()));
            }
            if (!this.conventionService.isListNullOrEmpty(((ExternalFetcherApiSourceConfigurationPersist) persist).getQueries())){
                apiEntity.setQueries(new ArrayList<>());
                for (QueryConfigPersist queryConfigPersist: ((ExternalFetcherApiSourceConfigurationPersist) persist).getQueries()) {
                    apiEntity.getQueries().add(this.buildQueryConfigEntity(queryConfigPersist));
                }
            }

            data = apiEntity;
        }else {
            ExternalFetcherStaticOptionSourceConfigurationEntity staticEntity = new ExternalFetcherStaticOptionSourceConfigurationEntity();

            if (!this.conventionService.isListNullOrEmpty(((ExternalFetcherStaticOptionSourceConfigurationPersist) persist).getItems())){
                staticEntity.setItems(new ArrayList<>());
                for (StaticPersist optionPersist: ((ExternalFetcherStaticOptionSourceConfigurationPersist) persist).getItems()) {
                    staticEntity.getItems().add(this.buildStaticEntity(optionPersist));
                }
            }

            data = staticEntity;
        }

        data.setType(persist.getType());
        data.setKey(persist.getKey());
        data.setLabel(persist.getLabel());
        data.setOrdinal(persist.getOrdinal());
        data.setReferenceTypeDependencyIds(persist.getReferenceTypeDependencyIds());

        return data;
    }

    private @NotNull ResultsConfigurationEntity buildResultsConfigEntity(ResultsConfigurationPersist persist){
        ResultsConfigurationEntity data = new ResultsConfigurationEntity();
        if (persist == null) return data;

        data.setResultsArrayPath(persist.getResultsArrayPath());

        if (!this.conventionService.isListNullOrEmpty(persist.getFieldsMapping())){
            data.setFieldsMapping(new ArrayList<>());
            for (ResultFieldsMappingConfigurationPersist fieldsMappingPersist: persist.getFieldsMapping()) {
                data.getFieldsMapping().add(this.buildResultFieldsMappingConfigEntity(fieldsMappingPersist));
            }
        }

        return data;
    }

    private @NotNull ResultFieldsMappingConfigurationEntity buildResultFieldsMappingConfigEntity(ResultFieldsMappingConfigurationPersist persist){
        ResultFieldsMappingConfigurationEntity data = new ResultFieldsMappingConfigurationEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setResponsePath(persist.getResponsePath());

        return data;
    }

    private @NotNull AuthenticationConfigurationEntity buildAuthConfigEntity(AuthenticationConfigurationPersist persist){
        AuthenticationConfigurationEntity data = new AuthenticationConfigurationEntity();
        if (persist == null) return data;

        data.setEnabled(persist.getEnabled());
        data.setAuthUrl(persist.getAuthUrl());
        data.setAuthMethod(persist.getAuthMethod());
        data.setAuthRequestBody(persist.getAuthRequestBody());
        data.setType(persist.getType());
        data.setAuthTokenPath(persist.getAuthTokenPath());

        return data;
    }

    private @NotNull QueryConfigEntity buildQueryConfigEntity(QueryConfigPersist persist){
        QueryConfigEntity data = new QueryConfigEntity();
        if (persist == null) return data;

        data.setName(persist.getName());
        data.setDefaultValue(persist.getDefaultValue());
        if (!this.conventionService.isListNullOrEmpty(persist.getCases())){
            data.setCases(new ArrayList<>());
            for (QueryCaseConfigPersist queryCaseConfigPersist: persist.getCases()) {
                data.getCases().add(this.buildQueryCaseConfigEntity(queryCaseConfigPersist));
            }
        }

        return data;
    }

    private @NotNull QueryCaseConfigEntity buildQueryCaseConfigEntity(QueryCaseConfigPersist persist){
        QueryCaseConfigEntity data = new QueryCaseConfigEntity();
        if (persist == null) return data;

        data.setReferenceTypeId(persist.getReferenceTypeId());
        data.setReferenceTypeSourceKey(persist.getReferenceTypeSourceKey());
        data.setSeparator(persist.getSeparator());
        data.setValue(persist.getValue());
        data.setLikePattern(persist.getLikePattern());

        return data;
    }

    private @NotNull StaticEntity buildStaticEntity(StaticPersist persist){
        StaticEntity data = new StaticEntity();
        if (persist == null) return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getOptions())){
            data.setOptions(new ArrayList<>());
            for (StaticOptionPersist staticOptionPersist: persist.getOptions()) {
                data.getOptions().add(this.buildStaticOptionEntity(staticOptionPersist));
            }
        }

        return data;
    }

    private @NotNull StaticOptionEntity buildStaticOptionEntity(StaticOptionPersist persist){
        StaticOptionEntity data = new StaticOptionEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setValue(persist.getValue());

        return data;
    }


    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteReferenceType);

        this.deleterFactory.deleter(ReferenceTypeDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}
