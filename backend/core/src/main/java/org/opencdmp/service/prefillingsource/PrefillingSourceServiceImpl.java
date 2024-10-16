package org.opencdmp.service.prefillingsource;

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
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.ReferenceTypeDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
import org.opencdmp.commons.types.externalfetcher.*;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionEntity;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionFieldEntity;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionFixedValueFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.data.TagEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.Prefilling;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.builder.prefillingsource.PrefillingSourceBuilder;
import org.opencdmp.model.deleter.PrefillingSourceDeleter;
import org.opencdmp.model.description.*;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptionreference.DescriptionReferenceData;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.persist.DescriptionPrefillingRequest;
import org.opencdmp.model.persist.PrefillingSearchRequest;
import org.opencdmp.model.persist.PrefillingSourcePersist;
import org.opencdmp.model.persist.externalfetcher.*;
import org.opencdmp.model.persist.prefillingsourcedefinition.PrefillingSourceDefinitionFieldPersist;
import org.opencdmp.model.persist.prefillingsourcedefinition.PrefillingSourceDefinitionFixedValueFieldPersist;
import org.opencdmp.model.persist.prefillingsourcedefinition.PrefillingSourceDefinitionPersist;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.prefillingsource.PrefillingSourceDefinition;
import org.opencdmp.model.reference.Definition;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.PrefillingSourceQuery;
import org.opencdmp.query.TagQuery;
import org.opencdmp.query.lookup.ReferenceSearchLookup;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.externalfetcher.ExternalFetcherService;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.opencdmp.service.reference.ReferenceService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PrefillingSourceServiceImpl implements PrefillingSourceService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PrefillingSourceServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;
    private final ExternalFetcherService externalFetcherService;
    private final ErrorThesaurusProperties errors;
    private final JsonHandlingService jsonHandlingService;
    private final ReferenceService referenceService;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    
    private static final String Zenodo = "Zenodo";

    public PrefillingSourceServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            QueryFactory queryFactory, ConventionService conventionService, MessageSource messageSource,
            XmlHandlingService xmlHandlingService, ExternalFetcherService externalFetcherService, ErrorThesaurusProperties errors, JsonHandlingService jsonHandlingService, ReferenceService referenceService, UsageLimitService usageLimitService, AccountingService accountingService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.externalFetcherService = externalFetcherService;
        this.errors = errors;
	    this.jsonHandlingService = jsonHandlingService;
	    this.referenceService = referenceService;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
    }


    public PrefillingSource persist(PrefillingSourcePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPrefillingSource);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PrefillingSourceEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PrefillingSourceEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PrefillingSource.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT);

            data = new PrefillingSourceEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setCode(model.getCode());
        data.setLabel(model.getLabel());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());

        if (isUpdate) this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT.getValue());
        }

        this.entityManager.flush();

        if (!isUpdate) {
            Long prefillingSourcesWithThisCode = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(data.getCode())
                    .count();

            if (prefillingSourcesWithThisCode > 1) throw new MyValidationException(this.errors.getPrefillingSourceCodeExists().getCode(), this.errors.getPrefillingSourceCodeExists().getMessage());
        }


        return this.builderFactory.builder(PrefillingSourceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, PrefillingSource._id), data);
    }

    private @NotNull PrefillingSourceDefinitionEntity buildDefinitionEntity(PrefillingSourceDefinitionPersist persist) {
        PrefillingSourceDefinitionEntity data = new PrefillingSourceDefinitionEntity();
        if (persist == null)
            return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (PrefillingSourceDefinitionFieldPersist fieldPersist : persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(persist.getFixedValueFields())) {
            data.setFixedValueFields(new ArrayList<>());
            for (PrefillingSourceDefinitionFixedValueFieldPersist fieldPersist : persist.getFixedValueFields()) {
                data.getFixedValueFields().add(this.buildFixedValueFieldEntity(fieldPersist));
            }
        }
        if (persist.getSearchConfiguration() != null ) {
            data.setSearchConfiguration(this.buildExternalFetcherApiConfigEntity(persist.getSearchConfiguration()));
        }
        if (persist.getGetConfiguration() != null && persist.getGetEnabled()) {
            data.setGetConfiguration(this.buildExternalFetcherApiConfigEntity(persist.getGetConfiguration()));
        }

        return data;
    }

    private @NotNull PrefillingSourceDefinitionFieldEntity buildFieldEntity(PrefillingSourceDefinitionFieldPersist persist) {
        PrefillingSourceDefinitionFieldEntity data = new PrefillingSourceDefinitionFieldEntity();
        if (persist == null)
            return data;

        data.setCode(persist.getCode());
        data.setSemanticTarget(persist.getSemanticTarget());
        data.setSystemFieldTarget(persist.getSystemFieldTarget());
        data.setTrimRegex(persist.getTrimRegex());

        return data;
    }

    private @NotNull PrefillingSourceDefinitionFixedValueFieldEntity buildFixedValueFieldEntity(PrefillingSourceDefinitionFixedValueFieldPersist persist) {
        PrefillingSourceDefinitionFixedValueFieldEntity data = new PrefillingSourceDefinitionFixedValueFieldEntity();
        if (persist == null)
            return data;

        data.setSemanticTarget(persist.getSemanticTarget());
        data.setSystemFieldTarget(persist.getSystemFieldTarget());
        data.setFixedValue(persist.getFixedValue());
        data.setTrimRegex(persist.getTrimRegex());

        return data;
    }

    private @NotNull ExternalFetcherApiSourceConfigurationEntity buildExternalFetcherApiConfigEntity(ExternalFetcherApiSourceConfigurationPersist persist) {
        ExternalFetcherApiSourceConfigurationEntity data = new ExternalFetcherApiSourceConfigurationEntity();
        if (persist == null)
            return data;

        data.setUrl(persist.getUrl());
        if (persist.getResults() != null ) {
            data.setResults(this.buildResultsConfigEntity(persist.getResults()));
        }
        data.setKey(persist.getKey());
        data.setLabel(persist.getLabel());
        data.setOrdinal(persist.getOrdinal());
        data.setType(persist.getType());
        data.setReferenceTypeDependencyIds(persist.getReferenceTypeDependencyIds());
        data.setPaginationPath(persist.getPaginationPath());
        data.setContentType(persist.getContentType());
        data.setFirstPage(persist.getFirstPage());
        data.setHttpMethod(persist.getHttpMethod());
        data.setRequestBody(persist.getRequestBody());
        data.setFilterType(persist.getFilterType());
        if (persist.getAuth() != null) {
            data.setAuth(this.buildAuthConfigEntity(persist.getAuth()));
        }
        if (!this.conventionService.isListNullOrEmpty(persist.getQueries())){
            data.setQueries(new ArrayList<>());
            for (QueryConfigPersist queryConfigPersist: (persist.getQueries())){
                data.getQueries().add(this.buildQueryConfigEntity(queryConfigPersist));
            }
        }

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

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePrefillingSource);

        this.deleterFactory.deleter(PrefillingSourceDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    public List<Prefilling> searchPrefillings(PrefillingSearchRequest model) {
        PrefillingSourceEntity prefillingSourceEntity = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(model.getPrefillingSourceId()).isActive(IsActive.Active).first();
        if (prefillingSourceEntity == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPrefillingSourceId(), PrefillingSource.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PrefillingSourceDefinitionEntity prefillingSourceDefinition = this.xmlHandlingService.fromXmlSafe(PrefillingSourceDefinitionEntity.class, prefillingSourceEntity.getDefinition());
        if (prefillingSourceDefinition == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPrefillingSourceId(), PrefillingSourceDefinition.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        ExternalReferenceCriteria externalReferenceCriteria = new ExternalReferenceCriteria();
        externalReferenceCriteria.setLike(model.getLike());

        ExternalDataResult externalData = this.externalFetcherService.getExternalData(Stream.of(prefillingSourceDefinition.getSearchConfiguration()).collect(Collectors.toList()), externalReferenceCriteria, null);
        if (externalData == null || this.conventionService.isListNullOrEmpty(externalData.getResults())) {
            return null;
        }

        List<Prefilling> prefillings = new ArrayList<>();
        for (Map<String, String> result : externalData.getResults()) {
            Prefilling prefilling = new Prefilling();
            prefilling.setId(result.getOrDefault(Prefilling._id, null));
            prefilling.setLabel(result.getOrDefault(Prefilling._label, null));
            prefilling.setKey(result.getOrDefault(Prefilling._key, null));
            prefilling.setTag(result.getOrDefault(Prefilling._tag, null));
            if (prefillingSourceDefinition.getGetConfiguration() == null) prefilling.setData(result);

            prefillings.add(prefilling);
        }

        prefillings = prefillings.stream().sorted(Comparator.comparing(Prefilling::getLabel, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());

        return prefillings;
    }

    public Description getPrefilledDescription(DescriptionPrefillingRequest model, FieldSet fieldSet) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {

        PrefillingSourceEntity prefillingSourceEntity = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(model.getPrefillingSourceId()).first();
        if (prefillingSourceEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPrefillingSourceId(), PrefillingSource.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PrefillingSourceDefinitionEntity prefillingSourceDefinition = this.xmlHandlingService.fromXmlSafe(PrefillingSourceDefinitionEntity.class, prefillingSourceEntity.getDefinition());
        if (prefillingSourceDefinition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPrefillingSourceId(), PrefillingSourceDefinition.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        Map<String, String> data = new HashMap<>();
        if (prefillingSourceDefinition.getGetConfiguration() != null){
            ExternalReferenceCriteria externalReferenceCriteria = new ExternalReferenceCriteria();
            externalReferenceCriteria.setLike(model.getData().getId());
            ExternalDataResult externalData = this.externalFetcherService.getExternalData(Stream.of(prefillingSourceDefinition.getGetConfiguration()).collect(Collectors.toList()), externalReferenceCriteria, null);
            if (externalData != null && !this.conventionService.isListNullOrEmpty(externalData.getResults())) {
                data = externalData.getResults().getFirst();
            }
        } else {
            data = model.getData() == null ? new HashMap<>() : model.getData().getData();
        }
        
        DescriptionTemplateEntity descriptionTemplateEntity = this.entityManager.find(DescriptionTemplateEntity.class, model.getDescriptionTemplateId(), true);
        if (descriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity descriptionTemplateDefinition = this.xmlHandlingService.fromXml(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, descriptionTemplateEntity.getDefinition());

        Description description = new Description();
        FieldSet descriptionTemplateFields = fieldSet.extractPrefixed(this.conventionService.asPrefix(Description._descriptionTemplate));

        description.setDescriptionTemplate(this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(descriptionTemplateFields, descriptionTemplateEntity));
        return this.mapPrefilledEntityToDescription(description, descriptionTemplateDefinition, prefillingSourceDefinition, prefillingSourceEntity.getLabel(), data);
    }

    private Description mapPrefilledEntityToDescription(Description description, DefinitionEntity descriptionTemplateDefinition, PrefillingSourceDefinitionEntity prefillingSourceDefinition, String type, Map<String, String> externalData){
        List<DescriptionReference> descriptionReferences = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(prefillingSourceDefinition.getFields())) {
            for (PrefillingSourceDefinitionFieldEntity field : prefillingSourceDefinition.getFields()) {
                String sourceValue = externalData.get(field.getCode());
                this.prefillSystemValueToDescription(description, field.getSystemFieldTarget(), sourceValue);
                this.prefillSemanticValueToDescription(description, descriptionReferences, field.getSemanticTarget(), sourceValue, descriptionTemplateDefinition, type);
            }
        }

        if (!this.conventionService.isListNullOrEmpty(prefillingSourceDefinition.getFixedValueFields())){
            for (PrefillingSourceDefinitionFixedValueFieldEntity field: prefillingSourceDefinition.getFixedValueFields()) {
                this.prefillSystemValueToDescription(description, field.getSystemFieldTarget(), field.getFixedValue());
                this.prefillSemanticValueToDescription(description, descriptionReferences, field.getSemanticTarget(), field.getFixedValue(), descriptionTemplateDefinition, type);
            }
        }

        if (!this.conventionService.isListNullOrEmpty(prefillingSourceDefinition.getFields())) {
            for (PrefillingSourceDefinitionFieldEntity field : prefillingSourceDefinition.getFields()) {
                String sourceValue = externalData.get(field.getCode());
                this.ensureZenodoFields(description, descriptionReferences, field.getSemanticTarget(), sourceValue, descriptionTemplateDefinition, type);
            }
        }

        if (!this.conventionService.isListNullOrEmpty(prefillingSourceDefinition.getFixedValueFields())) {
            for (PrefillingSourceDefinitionFixedValueFieldEntity field : prefillingSourceDefinition.getFixedValueFields()) {
                this.ensureZenodoFields(description, descriptionReferences, field.getSemanticTarget(), field.getFixedValue(), descriptionTemplateDefinition, type);
            }
        }

        description.setDescriptionReferences(descriptionReferences);
        return description;
    }
    
    private void ensureZenodoFields(Description description, List<DescriptionReference> descriptionReferences,String semanticTarget, String value, DefinitionEntity definition, String type) {
        if (!this.conventionService.isNullOrEmpty(type)  && !this.conventionService.isNullOrEmpty(semanticTarget) &&  !this.conventionService.isNullOrEmpty(value) && type.equals(Zenodo)) {
            if ("rda.dataset.distribution.data_access".equals(semanticTarget)) {
                if ("open".equals(value)) {
                    List<FieldEntity> issuedFieldEntities = definition.getAllField().stream().filter(x -> x.getSemantics() != null && x.getSemantics().contains("rda.dataset.issued")).toList();
                    if (!this.conventionService.isListNullOrEmpty(issuedFieldEntities)) {
                        String issuedIdNode = issuedFieldEntities.getFirst().getId();
                        Instant issuedValue = this.conventionService.isNullOrEmpty(issuedIdNode) ? null : description.getProperties().getFieldSets().values().stream().map(PropertyDefinitionFieldSet::getItems).flatMap(List::stream)
                                .filter(x -> x.getFields() != null && x.getFields().containsKey(issuedIdNode)).map(x -> x.getFields().get(issuedIdNode).getDateValue()).filter(Objects::nonNull).findFirst().orElse(null);  

                        if (issuedValue != null) {
                            List<FieldSetEntity> licStartFieldSetsEntities = definition.getAllFieldSets().stream().filter(x -> x.getAllField() != null && x.getAllField().stream().anyMatch(y -> y.getSemantics() != null && y.getSemantics().contains("rda.dataset.distribution.license.start_date"))).toList();
                            for (FieldSetEntity licStartFieldSetEntity : licStartFieldSetsEntities) {
                                List<FieldEntity> licStartEntities = licStartFieldSetEntity.getAllField().stream().filter(x -> x.getSemantics() != null && x.getSemantics().contains("rda.dataset.distribution.license.start_date")).toList();
                                if (!this.conventionService.isListNullOrEmpty(licStartEntities)) {
                                    this.ensureFieldSetEntity(description, licStartFieldSetEntity);

                                    for (FieldEntity licStartDateNode : licStartEntities) {
                                        description.getProperties().getFieldSets().get(licStartFieldSetEntity.getId()).getItems().getFirst().getFields().put(licStartDateNode.getId(), this.buildPropertyDefinitionFieldItemValue(descriptionReferences,licStartDateNode, 0, semanticTarget, issuedValue.toString(), type));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void ensureFieldSetEntity(Description description, FieldSetEntity fieldSetEntity){
        if (description.getProperties() == null) description.setProperties(new PropertyDefinition());
        if (description.getProperties().getFieldSets() == null) description.getProperties().setFieldSets(new HashMap<>());
        if (!description.getProperties().getFieldSets().containsKey(fieldSetEntity.getId())) description.getProperties().getFieldSets().put(fieldSetEntity.getId(), new PropertyDefinitionFieldSet());
        if (description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems() == null) description.getProperties().getFieldSets().get(fieldSetEntity.getId()).setItems(new ArrayList<>());
        if (this.conventionService.isListNullOrEmpty(description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems())) description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().add(new PropertyDefinitionFieldSetItem());
        if (description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().getFirst().getFields() == null) description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().getFirst().setFields(new HashMap<>());
        if (description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().getFirst().getOrdinal() == null) description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().getFirst().setOrdinal(0);
    }

    private void prefillSemanticValueToDescription(Description description, List<DescriptionReference> descriptionReferences, String semanticTarget, String parsedValue, DefinitionEntity definition, String type) {
        if (this.conventionService.isNullOrEmpty(semanticTarget) || this.conventionService.isNullOrEmpty(parsedValue)) return;
        List<FieldSetEntity> fieldSetsEntities = definition.getAllFieldSets().stream().filter(x-> x.getAllField() != null && x.getAllField().stream().anyMatch(y->  !this.conventionService.isListNullOrEmpty(y.getSemantics()) && y.getSemantics().contains(semanticTarget))).toList();
        for (FieldSetEntity fieldSetEntity: fieldSetsEntities) {
            List<FieldEntity> fieldEntities = fieldSetEntity.getAllField().stream().filter(x->  !this.conventionService.isListNullOrEmpty(x.getSemantics()) &&  x.getSemantics().contains(semanticTarget)).toList();
            if (!this.conventionService.isListNullOrEmpty(fieldEntities)) {
                this.ensureFieldSetEntity(description, fieldSetEntity);
                for (FieldEntity fieldEntity : fieldEntities){
                    description.getProperties().getFieldSets().get(fieldSetEntity.getId()).getItems().getFirst().getFields().put(fieldEntity.getId() , this.buildPropertyDefinitionFieldItemValue(descriptionReferences, fieldEntity, 0, semanticTarget, parsedValue, type));
                }
            }
        }
    }

    private void prefillSystemValueToDescription(Description description, String systemFieldTarget, String parsedValue) {
        if (this.conventionService.isNullOrEmpty(systemFieldTarget) || this.conventionService.isNullOrEmpty(parsedValue)) return;
        
        switch (systemFieldTarget) {
            case Description._description -> description.setDescription(parsedValue);
            case Description._label -> description.setLabel(parsedValue);
            case Description._descriptionTags ->  {
                String[] valuesParsed = this.tryParseJsonAsObjectString(String[].class, parsedValue);
                List<String> finalValue = valuesParsed == null ? List.of(parsedValue) : Arrays.stream(valuesParsed).toList();
                for (String tagString : finalValue) {
                    if (description.getDescriptionTags() == null) description.setDescriptionTags(new ArrayList<>());
                    if (description.getDescriptionTags().stream().anyMatch(x -> x.getTag() != null && x.getTag().getLabel().equals(tagString))) continue;

                    DescriptionTag descriptionTag = new DescriptionTag();
                    Tag tag = new Tag();
                    tag.setLabel(tagString.trim());
                    descriptionTag.setTag(tag);
                    descriptionTag.setIsActive(IsActive.Active);
                    description.getDescriptionTags().add(descriptionTag);
                }
            }
        }
    }

    private Field buildPropertyDefinitionFieldItemValue(List<DescriptionReference> descriptionReferences, FieldEntity fieldEntity, int ordinal, String semanticTarget, String value, String type) {
        Field field = new Field();
        if (fieldEntity == null || fieldEntity.getData() == null || fieldEntity.getData().getFieldType() == null || this.conventionService.isNullOrEmpty(value)) return field;
        try{
            switch (fieldEntity.getData().getFieldType()){
                case FREE_TEXT, TEXT_AREA, RICH_TEXT_AREA, RADIO_BOX -> field.setTextValue(value);
                case CHECK_BOX, BOOLEAN_DECISION ->{
                    if (!this.conventionService.isNullOrEmpty(value)) {
                        field.setBooleanValue("true".equals(value.trim().toLowerCase(Locale.ROOT)));
                    }
                }
                case DATE_PICKER -> {
                    Instant instant = null;
                    try {
                        if (!this.conventionService.isNullOrEmpty(type) && type.equals(Zenodo) && "rda.dataset.distribution.available_until".equals(semanticTarget) ) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
                            LocalDate date = LocalDate.parse(value, formatter);
                            date = date.plusYears(20);
                            instant = date.atStartOfDay().toInstant(ZoneOffset.UTC);
                        } else {
                            instant = Instant.parse(value);
                        }
                    } catch (DateTimeParseException ex) {
                        instant = LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
                        if (!this.conventionService.isNullOrEmpty(type) && type.equals(Zenodo) && "rda.dataset.distribution.available_until".equals(semanticTarget) ) {
                            instant.plus(20, ChronoUnit.YEARS);
                        }
                    }
                    field.setDateValue(instant);
                }
                case DATASET_IDENTIFIER -> {
                    ExternalIdentifier externalIdentifier = new ExternalIdentifier();
                    externalIdentifier.setIdentifier(value);
                    if(!this.conventionService.isNullOrEmpty(type) && type.equals(Zenodo)) externalIdentifier.setType("doi"); 
                    field.setExternalIdentifier(externalIdentifier);
                }
                case SELECT -> {
                    String[] valuesParsed = this.tryParseJsonAsObjectString(String[].class, value);
                    List<String> finalValue = valuesParsed == null ? List.of(value) : Arrays.stream(valuesParsed).toList();
                    SelectDataEntity selectDataEntity = (SelectDataEntity) fieldEntity.getData();
                    if (selectDataEntity == null || selectDataEntity.getOptions() == null) throw new MyApplicationException("Can not cast fieldEntity data");
                    field.setTextListValue(new ArrayList<>());
                    
                    for (SelectDataEntity.OptionEntity entity : selectDataEntity.getOptions()){
                        if (finalValue.contains(entity.getValue()) || finalValue.contains(entity.getLabel())){
                            if (selectDataEntity.getMultipleSelect()) field.getTextListValue().add(entity.getValue());
                            else field.setTextValue(entity.getValue());
                        }
                    }
                }
                case TAGS -> {
                    String[] valuesParsed = this.tryParseJsonAsObjectString(String[].class, value);
                    List<String> finalValue = valuesParsed == null ? List.of(value) : Arrays.stream(valuesParsed).toList();
                    List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).isActive(IsActive.Active).tags(finalValue).disableTracking().authorize(AuthorizationFlags.All).collect();
                    List<Tag> tags = new ArrayList<>();
                    for (String like : finalValue) {
                        Tag tag = new Tag();
                        tag.setLabel(like);
                        TagEntity existingTag = existingTags.stream().filter(x-> like.equalsIgnoreCase(x.getLabel())).findFirst().orElse(null);
                        if (existingTag != null) {
                            tag.setId(existingTag.getId());
                            tag.setCreatedAt(existingTag.getCreatedAt());
                            tag.setIsActive(IsActive.Active);
                            tag.setUpdatedAt(existingTag.getUpdatedAt());
                            tag.setHash(this.conventionService.hashValue(existingTag.getUpdatedAt()));
                        }
                        tags.add(tag);
                    }
                    
                    field.setTags(tags);
                }
                case REFERENCE_TYPES -> {
                    String[] valuesParsed = this.tryParseJsonAsObjectString(String[].class, value);
                    List<String> finalValue = valuesParsed == null ? List.of(value) : Arrays.stream(valuesParsed).toList();

                    ReferenceTypeDataEntity selectDataEntity = (ReferenceTypeDataEntity) fieldEntity.getData();
                    if (selectDataEntity == null) throw new MyApplicationException("Can not cast fieldEntity data");
                    field.setReferences(new ArrayList<>());
                    for (String like : finalValue) {
                        ReferenceSearchLookup externalReferenceCriteria = new ReferenceSearchLookup();
                        externalReferenceCriteria.setLike(like);
                        externalReferenceCriteria.setTypeId(selectDataEntity.getReferenceTypeId());
                        externalReferenceCriteria.setProject((BaseFieldSet) new BaseFieldSet()
                                .ensure(Reference._id)
                                .ensure(Reference._label)
                                .ensure(Reference._type)
                                .ensure(this.conventionService.asIndexer(Reference._type, ReferenceType._id))
                                .ensure(Reference._description)
                                .ensure(this.conventionService.asIndexer(Reference._description, Definition._fields, org.opencdmp.model.reference.Field._code))
                                .ensure(this.conventionService.asIndexer(Reference._description, Definition._fields, org.opencdmp.model.reference.Field._dataType))
                                .ensure(this.conventionService.asIndexer(Reference._description, Definition._fields, org.opencdmp.model.reference.Field._value))
                                .ensure(Reference._reference)
                                .ensure(Reference._sourceType)
                                .ensure(Reference._abbreviation)
                                .ensure(Reference._source)
                                .ensure(Reference._isActive)
                                .ensure(Reference._createdAt)
                                .ensure(Reference._updatedAt)
                                .ensure(Reference._hash)
                        );
                        List<Reference> references = this.referenceService.searchReferenceData(externalReferenceCriteria);

                        for (Reference reference : references) {
                            if (reference.getReference().equals(like) || reference.getLabel().toUpperCase(Locale.ROOT).contains(like.toUpperCase(Locale.ROOT))) {
                                field.getReferences().add(reference);
                                DescriptionReference descriptionReference = new DescriptionReference();
                                descriptionReference.setReference(reference);
                                DescriptionReferenceData descriptionReferenceData = new DescriptionReferenceData();
                                descriptionReferenceData.setFieldId(fieldEntity.getId());
                                descriptionReferenceData.setOrdinal(ordinal);
                                descriptionReference.setData(descriptionReferenceData);
                                descriptionReference.setIsActive(IsActive.Active);
                                descriptionReferences.add(descriptionReference);
                            }
                        }
                    }
                }
                case VALIDATION, UPLOAD, INTERNAL_ENTRIES_PLANS, INTERNAL_ENTRIES_DESCRIPTIONS -> throw new MyApplicationException("invalid type " + fieldEntity.getData().getFieldType());
                default -> throw new MyApplicationException("invalid type " + fieldEntity.getData().getFieldType());
            }
        } catch (Exception e){
            logger.error("Could not parse value " + value + " of field " + fieldEntity.getId() + " with type " + fieldEntity.getData().getFieldType(), e);
        }
        
        return field;
    }

    private <T> T tryParseJsonAsObjectString(Class<T> type, String value){
        T item = this.jsonHandlingService.fromJsonSafe(type, value);
        if (item == null) item = this.jsonHandlingService.fromJsonSafe(type, StringEscapeUtils.unescapeJava(value));
        if (item == null) item = this.jsonHandlingService.fromJsonSafe(type, StringEscapeUtils.unescapeJson(value));
        return item;
    }

}
