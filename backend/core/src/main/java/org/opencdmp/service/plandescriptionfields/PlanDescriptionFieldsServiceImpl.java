package org.opencdmp.service.plandescriptionfields;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.*;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;

import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.ExternalIdentifierModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import org.opencdmp.commonmodels.models.reference.ReferenceFieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.types.description.*;
import org.opencdmp.commons.types.description.importexport.*;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateFieldImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateSectionImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.ReferenceTypeDataImportExport;
import org.opencdmp.commons.types.reference.importexport.ReferenceImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.Tag;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.persist.descriptionproperties.*;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tag.TagService;
import org.opencdmp.service.visibility.VisibilityService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanDescriptionFieldsServiceImpl implements PlanDescriptionFieldsService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanDescriptionFieldsServiceImpl.class));
    private final ConventionService conventionService;
    private final ErrorThesaurusProperties errors;
    private final MessageSource messageSource;
    private final QueryFactory queryFactory;
    private final StorageFileService storageFileService;
    private final TagService tagService;
    @Autowired
    public PlanDescriptionFieldsServiceImpl(
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            QueryFactory queryFactory,
            StorageFileService storageFileService,
            TagService tagService) {
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
	    this.storageFileService = storageFileService;
	    this.tagService = tagService;
    }


    //region Persist

    @Override
    public @NotNull PropertyDefinitionFieldSetEntity cleanPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetEntity persist, FieldSetEntity fieldSetEntity) {
        PropertyDefinitionFieldSetEntity cleanData = new PropertyDefinitionFieldSetEntity();
        if (persist == null) return cleanData;
        if (!this.conventionService.isListNullOrEmpty(persist.getItems())){
            cleanData.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemEntity itemsPersist: persist.getItems()) {
                cleanData.getItems().add(this.cleanPropertyDefinitionFieldSetItemEntity(itemsPersist, fieldSetEntity));
            }
        }
        return cleanData;
    }

    private @NotNull PropertyDefinitionFieldSetItemEntity cleanPropertyDefinitionFieldSetItemEntity(PropertyDefinitionFieldSetItemEntity data, FieldSetEntity fieldSetEntity) {
        PropertyDefinitionFieldSetItemEntity cleanData = new PropertyDefinitionFieldSetItemEntity();
        if (data == null) return cleanData;
        if (data.getFields() != null && !data.getFields().isEmpty()){
            cleanData.setOrdinal(data.getOrdinal());
            cleanData.setFields(new HashMap<>());
            for (String key: data.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                if (fieldEntity != null){
                    cleanData.getFields().put(key,data.getFields().get(key));
                }
            }
        }
        return cleanData;
    }

    @Override
    public @NotNull PropertyDefinitionFieldSetEntity buildPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetPersist persist, FieldSetEntity fieldSetEntity, Map<String, List<UUID>> fieldToReferenceMap, VisibilityService visibilityService) throws InvalidApplicationException {
        PropertyDefinitionFieldSetEntity data = new PropertyDefinitionFieldSetEntity();
        if (persist == null) return data;
        data.setComment(persist.getComment());
        if (!this.conventionService.isListNullOrEmpty(persist.getItems())){
            data.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemPersist itemsPersist: persist.getItems()) {
                data.getItems().add(this.buildPropertyDefinitionFieldSetItemEntity(itemsPersist, fieldSetEntity, fieldToReferenceMap, visibilityService));
            }
        }
        return data;
    }

    private @NotNull PropertyDefinitionFieldSetItemEntity buildPropertyDefinitionFieldSetItemEntity(PropertyDefinitionFieldSetItemPersist persist, FieldSetEntity fieldSetEntity,  Map<String, List<UUID>> fieldToReferenceMap, VisibilityService visibilityService) throws InvalidApplicationException {
        PropertyDefinitionFieldSetItemEntity data = new PropertyDefinitionFieldSetItemEntity();
        if (persist == null) return data;
        if (persist.getFields() != null && !persist.getFields().isEmpty()){
            data.setOrdinal(persist.getOrdinal());
            data.setFields(new HashMap<>());
            for (String key: persist.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                boolean isVisible = fieldEntity != null ? visibilityService.isVisible(fieldEntity.getId(), persist.getOrdinal()) : false;
                data.getFields().put(key, this.buildFieldEntity(persist.getFields().get(key), fieldEntity, fieldToReferenceMap, persist.getOrdinal(), isVisible));
            }
        }
        return data;
    }
    
    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist, org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity,  Map<String, List<UUID>> fieldToReferenceMap, int ordinal, boolean isVisible) throws InvalidApplicationException {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        FieldEntity data = new FieldEntity();
        if (persist == null || !isVisible) return data;

        if (FieldType.isTextType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType)){
                UUID newFileId = this.conventionService.isValidUUID(persist.getTextValue()) ? UUID.fromString(persist.getTextValue()) : null;
                if (newFileId != null){
                    StorageFileEntity storageFileEntity = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(newFileId).firstAs(new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._storageType));
                    if (storageFileEntity == null || storageFileEntity.getStorageType().equals(StorageType.Temp)){
                        StorageFile storageFile = this.storageFileService.copyToStorage(newFileId, StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                        this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                        data.setTextValue(storageFile.getId().toString());
                    } else {
                        if (storageFileEntity.getId() != null){
                            //DO NOT Remove we can not be sure uf the description is copied
                            //this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().minusSeconds(60));
                        }
                        data.setTextValue(newFileId.toString());
                    }
                } else {
                    data.setTextValue(null);
                }
            } else {
                data.setTextValue(persist.getTextValue());
            }
        }
        else if (FieldType.isTextListType(fieldType)) {
            List<UUID> ids = new ArrayList<>();
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType)) {
                if (!this.conventionService.isListNullOrEmpty(persist.getTextListValue())) ids = persist.getTextListValue().stream().map(UUID::fromString).toList();
                else if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) ids.add(UUID.fromString(persist.getTextValue()));

                if (!ids.isEmpty()){
                    Set<UUID> existingIds = this.queryFactory.query(PlanQuery.class).disableTracking().ids(ids).collectAs(new BaseFieldSet().ensure(Plan._id)).stream().map(PlanEntity::getId).collect(Collectors.toSet());
                    for (UUID id : ids){
                        if (!existingIds.contains(id)) throw new MyValidationException(this.errors.getInternalPlanEntryNotFound().getCode(), id.toString());
                    }
                }

            } if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType)){
                if ( !this.conventionService.isListNullOrEmpty(persist.getTextListValue())) ids = persist.getTextListValue().stream().map(UUID::fromString).toList();
                else if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) ids.add(UUID.fromString(persist.getTextValue()));

                if (!ids.isEmpty()) {
                    Set<UUID> existingIds = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(ids).collectAs(new BaseFieldSet().ensure(Description._id)).stream().map(DescriptionEntity::getId).collect(Collectors.toSet());
                    for (UUID id : ids){
                        if (!existingIds.contains(id)) throw new MyValidationException(this.errors.getInternalDescriptionEntryNotFound().getCode(), id.toString());
                    }
                }
            }
            if (!ids.isEmpty()){
                if (ids.size() > 1) data.setTextListValue(persist.getTextListValue());
                else if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) data.setTextListValue(List.of(persist.getTextValue()));
                else if (!this.conventionService.isListNullOrEmpty(persist.getTextListValue())) data.setTextListValue(persist.getTextListValue());
            }else{
                data.setTextListValue(persist.getTextListValue());
            }
            if (fieldType.equals(FieldType.SELECT) && this.conventionService.isListNullOrEmpty(persist.getTextListValue()) && !this.conventionService.isNullOrEmpty(persist.getTextValue())){
                data.setTextListValue(List.of(persist.getTextValue()));
            } else if (fieldType.equals(FieldType.SELECT)){
                data.setTextListValue(persist.getTextListValue());
            }
        } else if (FieldType.isReferenceType(fieldType) && fieldEntity != null ) {
            String key = this.fieldToReferenceMapKey(fieldEntity.getId(), ordinal);
            List<UUID> referenceIds = fieldToReferenceMap.getOrDefault(key, null);
            if (referenceIds != null) data.setTextListValue(referenceIds.stream().map(UUID::toString).toList());
        } else if (FieldType.isTagType(fieldType)) {
            
            if (!this.conventionService.isListNullOrEmpty(persist.getTags())){
                List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).authorize(AuthorizationFlags.AllExceptPublic).disableTracking().tags(persist.getTags().stream().distinct().toList()).collectAs(new BaseFieldSet().ensure(Tag._id).ensure(Tag._label));
                
                List<String> values = new ArrayList<>();
                for (String tag : persist.getTags().stream().distinct().toList()){
                    TagEntity existingTag = existingTags.stream().filter(x-> x.getLabel().equalsIgnoreCase(tag)).findFirst().orElse(null);
                    if (existingTag == null) {
                        TagPersist tagPersist = new TagPersist();
                        tagPersist.setLabel(tag);
                        values.add(this.tagService.persist(tagPersist, new BaseFieldSet().ensure(Tag._id)).getId().toString());
                    } else values.add(existingTag.getId().toString());
                }
                if (!this.conventionService.isListNullOrEmpty(values)) data.setTextListValue(values);
            }
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isNumberType(fieldType))  data.setNumberValue(persist.getNumberValue());
        else if (FieldType.isExternalIdentifierType(fieldType) && persist.getExternalIdentifier() != null)  data.setExternalIdentifier(this.buildExternalIdentifierEntity(persist.getExternalIdentifier()));
       
        return data;
    }

    private @NotNull ExternalIdentifierEntity buildExternalIdentifierEntity(ExternalIdentifierPersist persist){
        ExternalIdentifierEntity data = new ExternalIdentifierEntity();
        if (persist == null) return data;

        data.setIdentifier(persist.getIdentifier());
        data.setType(persist.getType());
        return data;
    }

    
    private String fieldToReferenceMapKey(String fieldId, int ordinal){
        return fieldId + "_" + ordinal;
    }


    private @NotNull org.opencdmp.commons.types.reference.FieldEntity buildFieldEntity(org.opencdmp.model.persist.referencedefinition.FieldPersist persist){
        org.opencdmp.commons.types.reference.FieldEntity data = new org.opencdmp.commons.types.reference.FieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setDataType(persist.getDataType());
        data.setValue(persist.getValue());

        return data;
    }

    //endregion


    //region build persist

    @Override
    public @NotNull PropertyDefinitionFieldSetPersist buildPropertyDefinitionFieldSetPersist(PropertyDefinitionFieldSetEntity data, FieldSetEntity fieldSetEntity, List<ReferenceEntity> references){
        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        if (data == null) return persist;
        persist.setComment(data.getComment());
        if (!this.conventionService.isListNullOrEmpty(data.getItems())){
            persist.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemEntity itemsPersist: data.getItems()) {
                persist.getItems().add(this.buildPropertyDefinitionFieldSetItemPersist(itemsPersist, fieldSetEntity, references));
            }
        }
        return persist;
    }

    private @NotNull PropertyDefinitionFieldSetItemPersist buildPropertyDefinitionFieldSetItemPersist(PropertyDefinitionFieldSetItemEntity data, FieldSetEntity fieldSetEntity, List<ReferenceEntity> references){
        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();
        if (data == null) return persist;
        if (data.getFields() != null && !data.getFields().isEmpty()){
            persist.setOrdinal(data.getOrdinal());
            persist.setFields(new HashMap<>());
            for (String key: data.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                persist.getFields().put(key, this.buildFieldPersist(data.getFields().get(key), fieldEntity, references));
            }
        }
        return persist;
    }

    public @NotNull FieldPersist buildFieldPersist(FieldEntity data, org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity, List<ReferenceEntity> references) {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;

        FieldPersist persist = new FieldPersist();
        if (data == null) return persist;

        if (FieldType.isTextType(fieldType)) persist.setTextValue(data.getTextValue());
        else if (FieldType.isTextListType(fieldType)) persist.setTextListValue(data.getTextListValue());
        else if (FieldType.isDateType(fieldType))  persist.setDateValue(data.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  persist.setBooleanValue(data.getBooleanValue());
        else if (FieldType.isNumberType(fieldType))  persist.setNumberValue(data.getNumberValue());
        else if (FieldType.isExternalIdentifierType(fieldType) && data.getExternalIdentifier() != null)  persist.setExternalIdentifier(this.buildExternalIdentifierPersist(data.getExternalIdentifier()));
        else if (FieldType.isReferenceType(fieldType) && fieldEntity != null ) {
            if (!this.conventionService.isListNullOrEmpty(data.getTextListValue()) && !this.conventionService.isListNullOrEmpty(references)){
                List<UUID> referenceIdsInField = data.getTextListValue().stream().filter(x -> this.conventionService.isValidGuid(UUID.fromString(x))).toList().stream().map(UUID::fromString).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(referenceIdsInField)){
                    List<ReferenceEntity> referencesInField = references.stream().filter(x -> referenceIdsInField.contains(x.getId())).collect(Collectors.toList());
                    if (!this.conventionService.isListNullOrEmpty(referencesInField)){
                        persist.setReferences(this.buildReferencePersist(referencesInField));
                    }
                }
            }
        }
        else if (FieldType.isTagType(fieldType) && fieldEntity != null ) {
            if (!this.conventionService.isListNullOrEmpty(data.getTextListValue())){
                List<UUID> tagIdsInField = data.getTextListValue().stream().filter(x -> this.conventionService.isValidGuid(UUID.fromString(x))).toList().stream().map(UUID::fromString).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(tagIdsInField)){
                    List<TagEntity> tagsInField = this.queryFactory.query(TagQuery.class).isActive(IsActive.Active).ids(tagIdsInField).disableTracking().authorize(AuthorizationFlags.All).collect();
                    if (!this.conventionService.isListNullOrEmpty(tagsInField)){
                        persist.setTags(tagsInField.stream().map(TagEntity::getLabel).toList());
                    }
                }
            }
        }
        

        return persist;
    }

    private @NotNull ExternalIdentifierPersist buildExternalIdentifierPersist(ExternalIdentifierEntity data){
        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();
        if (data == null) return persist;

        persist.setIdentifier(data.getIdentifier());
        persist.setType(data.getType());
        return persist;
    }

    private @NotNull List<ReferencePersist> buildReferencePersist(List<ReferenceEntity> referenceEntities){
        List<ReferencePersist> referencesPersist = new ArrayList<>();
        if (this.conventionService.isListNullOrEmpty(referenceEntities)) return referencesPersist;

        for (ReferenceEntity entity: referenceEntities) {
            ReferencePersist persist = new ReferencePersist();
            persist.setId(entity.getId());
            persist.setLabel(entity.getLabel());
            persist.setDescription(entity.getDescription());
            persist.setReference(entity.getReference());
            persist.setSource(entity.getSource());
            persist.setSourceType(entity.getSourceType());
            persist.setTypeId(entity.getTypeId());
            persist.setAbbreviation(entity.getAbbreviation());

            referencesPersist.add(persist);
        }

        return referencesPersist;
    }

    //region Export

    @Override
    public DescriptionPropertyDefinitionFieldSetImportExport descriptionPropertyDefinitionFieldSetToExport(PropertyDefinitionFieldSetEntity entity, String fieldSetId, List<? extends SectionEntity> sections, Map<UUID, TagEntity> tagEntityMap) {
        DescriptionPropertyDefinitionFieldSetImportExport xml = new DescriptionPropertyDefinitionFieldSetImportExport();
        xml.setFieldSetId(fieldSetId);
        if (entity == null) return xml;

        xml.setComment(entity.getComment());
        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            List<DescriptionPropertyDefinitionFieldSetItemImportExport> exports = new LinkedList<>();
            for (PropertyDefinitionFieldSetItemEntity propertyDefinitionFieldSetItemEntity : entity.getItems()) {
                exports.add(this.descriptionPropertyDefinitionFieldSetItemToExport(propertyDefinitionFieldSetItemEntity, sections, tagEntityMap));
            }
            xml.setItems(exports);
        }

        return xml;
    }

    private DescriptionPropertyDefinitionFieldSetItemImportExport descriptionPropertyDefinitionFieldSetItemToExport(PropertyDefinitionFieldSetItemEntity entity, List<? extends SectionEntity> sections, Map<UUID, TagEntity> tagEntityMap) {
        DescriptionPropertyDefinitionFieldSetItemImportExport xml = new DescriptionPropertyDefinitionFieldSetItemImportExport();
        if (entity == null) return xml;

        xml.setOrdinal(entity.getOrdinal());
        if (entity.getFields() != null && !entity.getFields().isEmpty()) {
            List<DescriptionFieldImportExport> exports = new LinkedList<>();
            for (Map.Entry<String, FieldEntity> fieldSetEntityEntry : entity.getFields().entrySet()) {
                exports.add(this.descriptionFieldImportExportToExport(fieldSetEntityEntry.getValue(), fieldSetEntityEntry.getKey(), sections, tagEntityMap));
            }
            xml.setFields(exports);
        }

        return xml;
    }

    private DescriptionFieldImportExport descriptionFieldImportExportToExport(FieldEntity entity, String fieldId, List<? extends SectionEntity> sections, Map<UUID, TagEntity> tagEntityMap) {
        DescriptionFieldImportExport xml = new DescriptionFieldImportExport();
        xml.setFieldId(fieldId);
        if (entity == null) return xml;

        org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = this.getFieldById(fieldId, sections).stream().findFirst().orElse(null);
        if (fieldEntity == null) return xml;

        if (fieldEntity.getData().getFieldType().equals(FieldType.UPLOAD)){
            if (entity.getTextValue() != null){
                xml.setStorageFile(this.storageFileService.storageFileXmlToExport(UUID.fromString(entity.getTextValue())));
            }
        } else if (fieldEntity.getData().getFieldType().equals(FieldType.TAGS)){
            List<String> descriptionTagImportExports = new LinkedList<>();
            if (entity.getTextListValue() != null){
                for (String value : entity.getTextListValue()) {
                    descriptionTagImportExports.add(this.descriptionTagToExport(UUID.fromString(value), tagEntityMap));
                }
            } else if (entity.getTextValue() != null) descriptionTagImportExports.add(this.descriptionTagToExport(UUID.fromString(entity.getTextValue()), tagEntityMap));
            xml.setTags(descriptionTagImportExports);
        }
        else{
            xml.setDateValue(entity.getDateValue());
            xml.setBooleanValue(entity.getBooleanValue());
            xml.setNumberValue(entity.getNumberValue());
            xml.setTextValue(entity.getTextValue());
            xml.setTextListValue(entity.getTextListValue());

            if (entity.getExternalIdentifier() != null) {
                xml.setExternalIdentifier(this.descriptionExternalIdentifierToExport(entity.getExternalIdentifier()));
            }
        }

        return xml;
    }

    private DescriptionExternalIdentifierImportExport descriptionExternalIdentifierToExport(ExternalIdentifierEntity entity) {
        DescriptionExternalIdentifierImportExport xml = new DescriptionExternalIdentifierImportExport();
        if (entity == null) return xml;

        xml.setIdentifier(entity.getIdentifier());
        xml.setType(entity.getType());

        return xml;
    }

    private String descriptionTagToExport(UUID tagId, Map<UUID, TagEntity> tagEntityMap) {
        if (tagId == null) return null;

        TagEntity tag = tagEntityMap.getOrDefault(tagId, null);

        if (tag != null) return tag.getLabel();
        return null;
    }

    //endregion


    //region Import xml

    @Override
    public PropertyDefinitionFieldSetPersist xmlPropertyDefinitionFieldSetToPersist(DescriptionPropertyDefinitionFieldSetImportExport importXml, List<ReferenceImportExport> references, List<? extends DescriptionTemplateSectionImportExport> sections) {

        if (importXml == null)
            return null;

        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        persist.setComment(importXml.getComment());
        if (!this.conventionService.isListNullOrEmpty(importXml.getItems())){
            List<PropertyDefinitionFieldSetItemPersist> items = new ArrayList<>();
            for (DescriptionPropertyDefinitionFieldSetItemImportExport fieldSetItem: importXml.getItems()) {
                items.add(this.xmlPropertyDefinitionFieldSetItemToPersist(fieldSetItem, references, sections));
            }
            persist.setItems(items);
            return persist;

        }
        return null;
    }

    private PropertyDefinitionFieldSetItemPersist xmlPropertyDefinitionFieldSetItemToPersist(DescriptionPropertyDefinitionFieldSetItemImportExport importXml, List<ReferenceImportExport> references, List<? extends DescriptionTemplateSectionImportExport> sections) {
        if (importXml == null)
            return null;

        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();
        persist.setOrdinal(importXml.getOrdinal());

        Map<String, FieldPersist> fields = new HashMap<>();

        if (!this.conventionService.isListNullOrEmpty(importXml.getFields())){
            for (DescriptionFieldImportExport field: importXml.getFields()) {
                fields.put(field.getFieldId(), this.xmlFieldToPersist(field, importXml.getOrdinal(), references, sections));
            }
        }

        persist.setFields(fields);

        return persist;
    }

    private FieldPersist xmlFieldToPersist(DescriptionFieldImportExport importXml, int ordinal, List<ReferenceImportExport> references, List<? extends DescriptionTemplateSectionImportExport> sections) {
        if (importXml == null || sections == null)
            return null;

        DescriptionTemplateFieldImportExport descriptionTemplateField = this.getFieldByIdFromXml(importXml.getFieldId(), sections).stream().findFirst().orElse(null);
        if (descriptionTemplateField == null){
            return null;
        }

        FieldPersist persist = new FieldPersist();

        if (!this.conventionService.isListNullOrEmpty(references) && descriptionTemplateField.getData().getFieldType().equals(FieldType.REFERENCE_TYPES)){
            ReferenceTypeDataImportExport referenceTypeDataImportExport = (ReferenceTypeDataImportExport) descriptionTemplateField.getData();
            if (referenceTypeDataImportExport != null){
                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(referenceTypeDataImportExport.getReferenceTypeId()).first();//TODO: optimize
                if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(referenceTypeDataImportExport.getReferenceTypeCode()).first();
                if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referenceTypeDataImportExport.getReferenceTypeCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                List<ReferenceImportExport> referencesByField = references.stream().filter(x -> x.getOrdinal() == ordinal && x.getFieldId().equals(importXml.getFieldId())).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(referencesByField)){
                    if (referenceTypeDataImportExport.getMultipleSelect()){
                        List<ReferencePersist> referencePersists = new ArrayList<>();
                        for (ReferenceImportExport referenceImportExport: referencesByField) {
                            referencePersists.add(this.xmlDescriptionReferenceToPersist(referenceImportExport, referenceTypeEntity));
                        }
                        persist.setReferences(referencePersists);
                    }else {
                        persist.setReference(this.xmlDescriptionReferenceToPersist(referencesByField.stream().findFirst().orElse(null), referenceTypeEntity));
                    }
                }
            }
        } else {
            persist.setBooleanValue(importXml.getBooleanValue());
            persist.setDateValue(importXml.getDateValue());
            persist.setTextValue(importXml.getTextValue());
            persist.setTextListValue(importXml.getTextListValue());
            persist.setTags(importXml.getTags());
            persist.setNumberValue(importXml.getNumberValue());

            try {
                UUID fileId = this.storageFileService.xmlUploadFieldToPersist(importXml.getStorageFile());
                if(fileId != null) persist.setTextValue(fileId.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (importXml.getExternalIdentifier() != null){
                persist.setExternalIdentifier(this.xmlExternalIdentifierToPersist(importXml.getExternalIdentifier()));
            }
        }

        return persist;
    }

    private ReferencePersist xmlDescriptionReferenceToPersist(ReferenceImportExport importXml, ReferenceTypeEntity referenceTypeEntity) {
        if (importXml == null)
            return null;

        if (!referenceTypeEntity.getCode().equals(importXml.getType().getCode())) throw new MyApplicationException(this.errors.getInvalidReferenceTypeImportXml().getCode(), this.errors.getInvalidReferenceTypeImportXml().getMessage());

        ReferenceEntity referenceEntity = this.queryFactory.query(ReferenceQuery.class).ids(importXml.getId()).first(); //TODO: optimize
        if (referenceEntity == null) referenceEntity = this.queryFactory.query(ReferenceQuery.class).references(importXml.getReference()).typeIds(referenceTypeEntity.getId()).sources(importXml.getSource()).first();

        ReferencePersist persist = new ReferencePersist();

        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setLabel(importXml.getLabel());
            persist.setReference(importXml.getReference());
            persist.setSource("internal");
            persist.setSourceType(ReferenceSourceType.Internal);

            if (!this.conventionService.isListNullOrEmpty(importXml.getFields())) {
                DefinitionPersist definitionPersist = new DefinitionPersist();
                List<org.opencdmp.model.persist.referencedefinition.FieldPersist> fieldPersists = new LinkedList<>();
                for (ReferenceImportExport.ReferenceFieldImportExport fieldImportExport : importXml.getFields()) {
                    fieldPersists.add(this.xmlReferenceFieldToPersist(fieldImportExport));
                }
                definitionPersist.setFields(fieldPersists);
                persist.setDefinition(definitionPersist);
            }
        } else {
            persist.setId(referenceEntity.getId());
        }

        return persist;
    }

    private org.opencdmp.model.persist.referencedefinition.FieldPersist xmlReferenceFieldToPersist(ReferenceImportExport.ReferenceFieldImportExport importXml) {
        if (importXml == null)
            return null;

        org.opencdmp.model.persist.referencedefinition.FieldPersist persist = new org.opencdmp.model.persist.referencedefinition.FieldPersist();
        persist.setCode(importXml.getCode());
        persist.setValue(importXml.getValue());
        persist.setDataType(importXml.getDataType());

        return persist;
    }

    private ExternalIdentifierPersist xmlExternalIdentifierToPersist(DescriptionExternalIdentifierImportExport importXml) {
        if (importXml == null)
            return null;

        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();

        persist.setType(importXml.getType());
        persist.setIdentifier(importXml.getIdentifier());

        return persist;
    }

    //endregion

    //region Import Common Model 


    @Override
    public PropertyDefinitionFieldSetPersist commonModelPropertyDefinitionFieldSetToPersist(PropertyDefinitionFieldSetModel commonModel, List<? extends SectionModel> sections) {

        if (commonModel == null)
            return null;

        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        persist.setComment(commonModel.getComment());
        if (!this.conventionService.isListNullOrEmpty(commonModel.getItems())){
            List<PropertyDefinitionFieldSetItemPersist> items = new ArrayList<>();
            for (PropertyDefinitionFieldSetItemModel fieldSetItem: commonModel.getItems()) {
                PropertyDefinitionFieldSetItemPersist fieldSetItemPersist = this.commonModelPropertyDefinitionFieldSetItemToPersist(fieldSetItem, sections);
                if (fieldSetItemPersist != null && !fieldSetItemPersist.getFields().isEmpty()) items.add(fieldSetItemPersist);
            }
            persist.setItems(items);
            return persist;

        }
        return null;
    }

    private PropertyDefinitionFieldSetItemPersist commonModelPropertyDefinitionFieldSetItemToPersist(PropertyDefinitionFieldSetItemModel commonModel, List<? extends SectionModel> sections) {
        if (commonModel == null)
            return null;

        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();

        persist.setOrdinal(commonModel.getOrdinal());

        Map<String, FieldPersist> fields = new HashMap<>();

        if (commonModel.getFields() != null && !commonModel.getFields().isEmpty()){
            for (String fieldId: commonModel.getFields().keySet()) {
                fields.put(fieldId, this.commonModelFieldToPersist(commonModel.getFields().get(fieldId), sections));
            }
        }

        persist.setFields(fields);

        return persist;
    }

    public FieldPersist commonModelFieldToPersist(FieldModel model, List<? extends SectionModel> sections) {
        if (model == null || sections == null || sections.isEmpty())
            return null;

        org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel descriptionTemplateField = this.getFieldModelById(model.getId(), sections).stream().findFirst().orElse(null);
        if (descriptionTemplateField == null){
            return null;
        }

        FieldPersist persist = new FieldPersist();

        if (descriptionTemplateField.getData().getFieldType().equals(org.opencdmp.commonmodels.enums.FieldType.REFERENCE_TYPES)) {
            ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) descriptionTemplateField.getData();
            if (referenceTypeDataModel != null) {
                if (!this.conventionService.isListNullOrEmpty(model.getReferences())){
                    ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(referenceTypeDataModel.getReferenceType().getId()).first();//TODO: optimize
                    if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(referenceTypeDataModel.getReferenceType().getCode()).first();
                    if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referenceTypeDataModel.getReferenceType().getCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    if (referenceTypeDataModel.getMultipleSelect()) {
                        List<ReferencePersist> referencePersists = new ArrayList<>();
                        for (ReferenceModel referenceModel : model.getReferences()) {
                            referencePersists.add(this.commonReferenceToPersist(referenceModel, referenceTypeEntity));
                        }
                        persist.setReferences(referencePersists);
                    } else {
                        persist.setReference(this.commonReferenceToPersist(model.getReferences().stream().findFirst().orElse(null), referenceTypeEntity));
                    }
            }
        }
        } else {
            persist.setBooleanValue(model.getBooleanValue());
            persist.setDateValue(model.getDateValue());
            persist.setTextValue(model.getTextValue());
            persist.setTextListValue(model.getTextListValue());
            persist.setNumberValue(model.getNumberValue());

            if (model.getExternalIdentifier() != null){
                persist.setExternalIdentifier(this.commonModelExternalIdentifierToPersist(model.getExternalIdentifier()));
            }
        }

        return persist;
    }

    private ReferencePersist commonReferenceToPersist(ReferenceModel model, ReferenceTypeEntity referenceTypeEntity) {
        if (model == null)
            return null;

        if (!referenceTypeEntity.getCode().equals(model.getType().getCode())) throw new MyApplicationException("Invalid reference for field " + model.getId());

        ReferenceEntity referenceEntity = model.getId() != null ? this.queryFactory.query(ReferenceQuery.class).ids(model.getId()).first(): null; //TODO: optimize
        if (referenceEntity == null && !this.conventionService.isNullOrEmpty(model.getReference())) {
            List<ReferenceEntity> referenceEntities = this.queryFactory.query(ReferenceQuery.class).references(model.getReference()).typeIds(referenceTypeEntity.getId()).collect();
            if (referenceEntities != null && referenceEntities.size() == 1) referenceEntity = referenceEntities.getFirst();
        }

        ReferencePersist persist = new ReferencePersist();

        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setReference(!this.conventionService.isNullOrEmpty(model.getReference()) ? model.getReference() : UUID.randomUUID().toString());
            persist.setLabel(!this.conventionService.isNullOrEmpty(model.getLabel()) ? model.getLabel() : persist.getReference());
            persist.setSource("internal");
            persist.setSourceType(ReferenceSourceType.Internal);

            if (model.getDefinition() != null && !this.conventionService.isListNullOrEmpty(model.getDefinition().getFields())) {
                DefinitionPersist definitionPersist = new DefinitionPersist();
                List<org.opencdmp.model.persist.referencedefinition.FieldPersist> fieldPersists = new LinkedList<>();
                for (ReferenceFieldModel fieldModel : model.getDefinition().getFields()) {
                    fieldPersists.add(this.commonModelReferenceFieldToPersist(fieldModel));
                }
                definitionPersist.setFields(fieldPersists);
                persist.setDefinition(definitionPersist);
            }
        } else {
            persist.setId(referenceEntity.getId());
        }
        return persist;
    }

    private org.opencdmp.model.persist.referencedefinition.FieldPersist commonModelReferenceFieldToPersist(ReferenceFieldModel commonModel) {
        if (commonModel == null)
            return null;

        org.opencdmp.model.persist.referencedefinition.FieldPersist persist = new org.opencdmp.model.persist.referencedefinition.FieldPersist();
        persist.setCode(commonModel.getCode());
        persist.setValue(commonModel.getValue());
        switch (commonModel.getDataType()){
            case Text -> persist.setDataType(ReferenceFieldDataType.Text);
            case Date -> persist.setDataType(ReferenceFieldDataType.Date);
            case null -> persist.setDataType(null);
            default -> throw new MyApplicationException("unrecognized type " + commonModel.getDataType());
        }

        return persist;
    }

    private ExternalIdentifierPersist commonModelExternalIdentifierToPersist(ExternalIdentifierModel commonModel) {
        if (commonModel == null)
            return null;

        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();

        persist.setType(commonModel.getType());
        persist.setIdentifier(commonModel.getIdentifier());

        return persist;
    }

    //endregion


    //helper methods
    private List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity> getAllFields(List<? extends SectionEntity> sections) {
        List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity> result = new ArrayList<>();
        if (sections != null) {
            for (SectionEntity section : sections) {
                result.addAll(section.getAllField());
            }
        }
        return result;
    }

    private List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity> getFieldById(String id, List<? extends SectionEntity> sections) {
        return this.getAllFields(sections).stream()
                .filter(x -> id != null && id.equals(x.getId()))
                .toList();
    }

    private List<DescriptionTemplateFieldImportExport> getAllFieldsFromXml(List<? extends DescriptionTemplateSectionImportExport> sections) {
        List<DescriptionTemplateFieldImportExport> result = new ArrayList<>();
        if (sections != null) {
            for (DescriptionTemplateSectionImportExport section : sections) {
                result.addAll(section.getAllField());
            }
        }
        return result;
    }

    //helper methods for common model
    private List<org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel> getAllFieldModels(List<? extends SectionModel> sections) {
        List<org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel> result = new ArrayList<>();
        if (sections != null) {
            for (SectionModel section : sections) {
                result.addAll(section.getAllField());
            }
        }
        return result;
    }

    private List<org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel> getFieldModelById(String id, List<? extends SectionModel> sections) {
        return this.getAllFieldModels(sections).stream()
                .filter(x -> id != null && id.equals(x.getId()))
                .toList();
    }

    //helper methods for xml
    private List<DescriptionTemplateFieldImportExport> getFieldByIdFromXml(String id, List<? extends DescriptionTemplateSectionImportExport> sections) {
        return this.getAllFieldsFromXml(sections).stream()
                .filter(x -> id != null && id.equals(x.getId()))
                .toList();
    }
}
