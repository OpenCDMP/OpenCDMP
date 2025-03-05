package org.opencdmp.service.reference;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceFieldDataType;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.commons.types.reference.FieldEntity;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.deleter.ReferenceDeleter;
import org.opencdmp.model.persist.ReferencePersist;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.opencdmp.model.persist.referencedefinition.FieldPersist;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.lookup.ReferenceSearchLookup;
import org.opencdmp.query.lookup.ReferenceTestLookup;
import org.opencdmp.service.externalfetcher.ExternalFetcherService;
import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReferenceServiceImpl implements ReferenceService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final QueryFactory queryFactory;
    private final XmlHandlingService xmlHandlingService;
    private final JsonHandlingService jsonHandlingService;
    private final ErrorThesaurusProperties errors;

    public final ExternalFetcherService externalFetcherService;
    public ReferenceServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            QueryFactory queryFactory,
            XmlHandlingService xmlHandlingService, JsonHandlingService jsonHandlingService, ErrorThesaurusProperties errors, ExternalFetcherService externalFetcherService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
        this.jsonHandlingService = jsonHandlingService;
        this.errors = errors;
        this.externalFetcherService = externalFetcherService;
    }

    @Override
    public Reference persist(ReferencePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, TransformerException, ParserConfigurationException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditReference);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        ReferenceEntity data;
        if (isUpdate) {
            data = this.entityManager.find(ReferenceEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Reference.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        } else {
            data = new ReferenceEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setLabel(model.getLabel());
        data.setTypeId(model.getTypeId());
        data.setDescription(model.getDescription());
        data.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDefinitionEntity(model.getDefinition())));
        data.setUpdatedAt(Instant.now());
        data.setReference(model.getReference());
        data.setAbbreviation(model.getAbbreviation());
        data.setSource(model.getSource());
        data.setSourceType(model.getSourceType());
        
        if (isUpdate) this.entityManager.merge(data);
        else  this.entityManager.persist(data);

        this.entityManager.flush();

        return this.builderFactory.builder(ReferenceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Reference._id), data);
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist){
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())){
            data.setFields(new ArrayList<>());
            for (FieldPersist fieldPersist: persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }

        return data;
    }

    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist){
        FieldEntity data = new FieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setDataType(persist.getDataType());
        data.setValue(persist.getValue());

        return data;
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteReference);

        this.deleterFactory.deleter(ReferenceDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    public Boolean findReference(String reference, UUID referenceTypeId){
        if (this.conventionService.isNullOrEmpty(reference) || !this.conventionService.isValidGuid(referenceTypeId)) return false;
        ReferenceQuery query = this.queryFactory.query(ReferenceQuery.class).disableTracking().references(reference).typeIds(referenceTypeId).isActive(IsActive.Active);

        if (query != null && query.count() > 0) return true;
        return false;
    }

    @Override
    public List<Reference> searchReferenceData(ReferenceSearchLookup lookup) throws MyNotFoundException, InvalidApplicationException {
        int initialOffset = 0;
        if (lookup.getPage() != null && !lookup.getPage().isEmpty()){
            initialOffset =  lookup.getPage().getOffset();
            lookup.getPage().setOffset(0);
        }

        ReferenceTypeEntity data = this.entityManager.find(ReferenceTypeEntity.class, lookup.getTypeId(), true);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{lookup.getTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String like;
        if (!this.conventionService.isNullOrEmpty(lookup.getLike())){
            like = lookup.getLike().replaceAll("%", "");
        } else {
            like = null;
        }
        ExternalReferenceCriteria externalReferenceCriteria = new ExternalReferenceCriteria(like, lookup.getDependencyReferences());

        ExternalDataResult remoteRepos = this.getReferenceData(data, externalReferenceCriteria, lookup.getKey());

        List<Reference> externalModels = this.getReferences(remoteRepos, this.jsonHandlingService.toJsonSafe(data), this.jsonHandlingService.toJsonSafe(lookup), lookup.getProject());
        
        List<Reference> models = this.fetchReferenceFromDb(lookup);
        models.addAll(externalModels);
        models = models.stream().sorted(Comparator.comparing(Reference::getLabel, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());

        if (lookup.getPage() != null && !lookup.getPage().isEmpty()){
            models = models.stream().skip(initialOffset).limit(lookup.getPage().getSize()).toList();
        }

        return models;
    }

    @Override
    public List<Reference> testReferenceData(ReferenceTestLookup lookup) throws MyNotFoundException {
        if (this.conventionService.isListNullOrEmpty(lookup.getSources()))
            throw new MyValidationException(this.errors.getInvalidReferenceTypeDefinitionSource().getCode(), this.errors.getInvalidReferenceTypeDefinitionSource().getMessage());

        int initialOffset = 0;
        if (lookup.getPage() != null && !lookup.getPage().isEmpty()){
            initialOffset =  lookup.getPage().getOffset();
            lookup.getPage().setOffset(0);
        }

        String like;
        if (!this.conventionService.isNullOrEmpty(lookup.getLike())){
            like = lookup.getLike().replaceAll("%", "");
        } else {
            like = null;
        }
        ExternalReferenceCriteria externalReferenceCriteria = new ExternalReferenceCriteria(like, lookup.getDependencyReferences());
        ExternalDataResult remoteRepos = this.externalFetcherService.getExternalData(lookup.getSources().stream().map(x -> (SourceBaseConfiguration)x).collect(Collectors.toList()), externalReferenceCriteria,  lookup.getKey(), true);
        List<Reference> models = this.getReferences(remoteRepos, null, this.jsonHandlingService.toJsonSafe(lookup), lookup.getProject());

        if (lookup.getPage() != null && !lookup.getPage().isEmpty()){
            models = models.stream().skip(initialOffset).limit(lookup.getPage().getSize()).toList();
        }

        return models;
    }

    private List<Reference> getReferences(ExternalDataResult remoteRepos, String data, String lookup, BaseFieldSet fieldSet) {
        List<Reference> models = new ArrayList<>();
        if (remoteRepos != null && !this.conventionService.isListNullOrEmpty(remoteRepos.getResults())){
            List<ReferenceEntity> referenceEntities = new ArrayList<>();
            for (Map<String, String> result : remoteRepos.getResults()){
                if (result == null || result.isEmpty()) continue;
                ReferenceEntity referenceEntity = this.buildReferenceEntityFromExternalData(result, new ReferenceTypeEntity());
                //filter elements if label or reference don't exist
                if (this.conventionService.isNullOrEmpty(referenceEntity.getLabel()) || this.conventionService.isNullOrEmpty(referenceEntity.getReference())) {
                    logger.warn("reference or label not found " + data + lookup);
                    continue;
                }
                referenceEntities.add(referenceEntity);
            }
            models = this.builderFactory.builder(ReferenceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, referenceEntities);
        }

        return models;
    }

    @NotNull
    private ReferenceEntity buildReferenceEntityFromExternalData(Map<String, String> result, ReferenceTypeEntity data) {
        ReferenceEntity referenceEntity = new ReferenceEntity();
        referenceEntity.setTypeId(data.getId());
        referenceEntity.setIsActive(IsActive.Active);
        referenceEntity.setReference(result.getOrDefault(ReferenceEntity.KnownFields.ReferenceId, null));
        referenceEntity.setSource(result.getOrDefault(ReferenceEntity.KnownFields.Key, null));
        referenceEntity.setAbbreviation(result.getOrDefault(ReferenceEntity.KnownFields.Abbreviation, null));
        referenceEntity.setDescription(result.getOrDefault(ReferenceEntity.KnownFields.Description, null));
        referenceEntity.setLabel(result.getOrDefault(ReferenceEntity.KnownFields.Label, null));
        referenceEntity.setSourceType(ReferenceSourceType.External);
        DefinitionEntity definitionEntity = new DefinitionEntity();
        definitionEntity.setFields(new ArrayList<>());
        for (Map.Entry<String, String> resultValue : result.entrySet()){
            if (resultValue.getKey().equals(ReferenceEntity.KnownFields.ReferenceId) 
                    || resultValue.getKey().equals(ReferenceEntity.KnownFields.Abbreviation)
                    || resultValue.getKey().equals(ReferenceEntity.KnownFields.Description)
                    || resultValue.getKey().equals(ReferenceEntity.KnownFields.Label)
                    || resultValue.getKey().equals(ReferenceEntity.KnownFields.SourceLabel)) continue;
            FieldEntity fieldEntity = new FieldEntity();
            fieldEntity.setCode(resultValue.getKey());
            fieldEntity.setValue(resultValue.getValue());
            fieldEntity.setDataType(ReferenceFieldDataType.Text);
            definitionEntity.getFields().add(fieldEntity);
        }
        if (!definitionEntity.getFields().isEmpty()) referenceEntity.setDefinition(this.xmlHandlingService.toXmlSafe(definitionEntity));
        return referenceEntity;
    }

    private List<Reference> fetchReferenceFromDb(ReferenceSearchLookup lookup){
        ReferenceQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic).sourceTypes(ReferenceSourceType.Internal).typeIds(lookup.getTypeId());
        if (!this.conventionService.isNullOrEmpty(lookup.getLike())) query.like(lookup.getLike());
        List<ReferenceEntity> data = query.collectAs(lookup.getProject());
        return this.builderFactory.builder(ReferenceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
    }

    private ExternalDataResult getReferenceData(ReferenceTypeEntity referenceType, ExternalReferenceCriteria externalReferenceCriteria, String key)  {
        ReferenceTypeDefinitionEntity referenceTypeDefinition = this.xmlHandlingService.fromXmlSafe(ReferenceTypeDefinitionEntity.class, referenceType.getDefinition());
        if (referenceTypeDefinition == null || this.conventionService.isListNullOrEmpty(referenceTypeDefinition.getSources())) return new ExternalDataResult();

        ExternalDataResult results = this.externalFetcherService.getExternalData(referenceTypeDefinition.getSources().stream().map(x -> (SourceBaseConfiguration)x).collect(Collectors.toList()), externalReferenceCriteria,  key, false);
        for (Map<String, String> result: results.getResults()) {
            result.put("referenceType", referenceType.getName());
        }
        return results;
    }


}
