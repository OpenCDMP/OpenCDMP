package org.opencdmp.model.builder.commonmodels.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.FieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.data.TagEntity;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.reference.ReferenceCommonModelBuilder;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.query.TagQuery;
import org.opencdmp.service.storage.StorageFileProperties;
import org.opencdmp.service.storage.StorageFileService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLConnection;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldCommonModelBuilder extends BaseCommonModelBuilder<FieldModel, FieldEntity> {
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity;
    private final StorageFileService storageFileService;
    private final UserScope userScope;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileProperties;
    @Autowired
    public FieldCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory, StorageFileService storageFileService, UserScope userScope, ValidatorFactory validatorFactory, StorageFileProperties storageFileProperties
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
	    this.storageFileService = storageFileService;
	    this.userScope = userScope;
	    this.validatorFactory = validatorFactory;
	    this.storageFileProperties = storageFileProperties;
    }

    public FieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    public FieldCommonModelBuilder withFieldEntity(org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity) {
        this.fieldEntity = fieldEntity;
        return this;
    }


    private boolean useSharedStorage;
    public FieldCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }
    @Override
    protected List<CommonModelBuilderItemResponse<FieldModel, FieldEntity>> buildInternal(List<FieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;

        Map<UUID, ReferenceModel> referenceItemsMap = FieldType.isReferenceType(fieldType) ? this.collectReferences(data) : null;
        Map<UUID, String> tagItemsMap = FieldType.isTagType(fieldType) ? this.collectTags(data) : null;

        List<CommonModelBuilderItemResponse<FieldModel, FieldEntity>> models = new ArrayList<>();
        for (FieldEntity d : data) {
            FieldModel m = new FieldModel();
            if (this.fieldEntity != null) m.setId(this.fieldEntity.getId());
            if (FieldType.isDateType(fieldType)) m.setDateValue(d.getDateValue());
            if (FieldType.isBooleanType(fieldType)) m.setBooleanValue(d.getBooleanValue());
            if (FieldType.isTextType(fieldType)) m.setTextValue(d.getTextValue());
            if (FieldType.isTagType(fieldType)) m.setTextListValue(d.getTextListValue());
            if (FieldType.isTextListType(fieldType)) m.setTextListValue(d.getTextListValue());
            if (FieldType.isReferenceType(fieldType)  && referenceItemsMap != null && d.getTextListValue() != null && !d.getTextListValue().isEmpty()) {
                m.setReferences(new ArrayList<>());
                for (UUID referenceId : d.getTextListValue().stream().map(UUID::fromString).distinct().toList()){
                    if (referenceItemsMap.containsKey(referenceId)) m.getReferences().add(referenceItemsMap.get(referenceId));
                }
            }
            if (FieldType.isTagType(fieldType)  && tagItemsMap != null && d.getTextListValue() != null && !d.getTextListValue().isEmpty()) {
                m.setTextListValue(new ArrayList<>());
                for (UUID tagId : d.getTextListValue().stream().map(UUID::fromString).distinct().toList()){
                    if (tagItemsMap.containsKey(tagId)) m.getTextListValue().add(tagItemsMap.get(tagId));
                }
            }

            if (FieldType.UPLOAD.equals(fieldType)  && d.getTextValue() != null && !d.getTextValue().isEmpty()) {
                try {
                    byte[] bytes = this.storageFileService.readAsBytesSafe(UUID.fromString(d.getTextValue()));
                    FileEnvelopeModel fileEnvelopeModel = new FileEnvelopeModel();
                    StorageFileEntity storageFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(UUID.fromString(d.getTextValue())).first();
                    fileEnvelopeModel.setFile(bytes);
                    fileEnvelopeModel.setFilename(storageFile.getName() + (storageFile.getExtension().startsWith(".") ? "" : ".") + storageFile.getExtension());
                    fileEnvelopeModel.setMimeType(storageFile.getMimeType());
                    if (!this.useSharedStorage){
                        fileEnvelopeModel.setFile(bytes);
                    } else {
                        fileEnvelopeModel.setFileRef(this.addFileToSharedStorage(bytes, storageFile));
                    }
                    m.setFile(fileEnvelopeModel);
                }catch (Exception e){
	                this.logger.error(e.getMessage());
                }
            }
            if (d.getExternalIdentifier() != null && FieldType.isExternalIdentifierType(fieldType))  m.setExternalIdentifier(this.builderFactory.builder(ExternalIdentifierCommonModelBuilder.class).authorize(this.authorize).build(d.getExternalIdentifier()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private String addFileToSharedStorage(byte[] bytes, StorageFileEntity storageFile) throws IOException {
        StorageFilePersist storageFilePersist = new StorageFilePersist();
        storageFilePersist.setName(FilenameUtils.removeExtension(storageFile.getName()));
        storageFilePersist.setExtension(FilenameUtils.getExtension(storageFile.getExtension()));
        storageFilePersist.setMimeType(URLConnection.guessContentTypeFromName(storageFile.getName() + (storageFile.getExtension().startsWith(".") ? "" : ".") + storageFile.getExtension()));
        storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
        storageFilePersist.setStorageType(StorageType.Temp);
        storageFilePersist.setLifetime(Duration.ofSeconds(this.storageFileProperties.getTempStoreLifetimeSeconds())); //TODO
        this.validatorFactory.validator(StorageFilePersist.StorageFilePersistValidator.class).validateForce(storageFilePersist);
        StorageFile persisted = this.storageFileService.persistBytes(storageFilePersist, bytes, new BaseFieldSet(StorageFile._id, StorageFile._fileRef));
        return persisted.getFileRef();
    }

    private Map<UUID, String> collectTags(List<FieldEntity> data){
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", TagEntity.class.getSimpleName());

        List<UUID> tagIds = data.stream().map(FieldEntity::getTextListValue).filter(Objects::nonNull).flatMap(List::stream).filter(x-> !this.conventionService.isNullOrEmpty(x)).map(UUID::fromString).distinct().collect(Collectors.toList());
        List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).authorize(this.authorize).disableTracking().ids(tagIds).collectAs(new BaseFieldSet().ensure(Tag._id).ensure(Tag._label));

        Map<UUID, String> itemMap = new HashMap<>();
        for (UUID tag : tagIds){
            existingTags.stream().filter(x -> x.getId().equals(tag)).findFirst().ifPresent(existingTag -> itemMap.put(tag, existingTag.getLabel()));
        }
        return itemMap;
    }

    private Map<UUID, ReferenceModel> collectReferences(List<FieldEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Reference.class.getSimpleName());

        Map<UUID, ReferenceModel> itemMap;
        
        ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(FieldEntity::getTextListValue).filter(Objects::nonNull).flatMap(List::stream).filter(x-> !this.conventionService.isNullOrEmpty(x)).map(UUID::fromString).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(ReferenceCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, ReferenceEntity::getId);

        return itemMap;
    }

}
