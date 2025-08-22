package org.opencdmp.model.builder.commonmodels.plan;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plan.PlanBlueprintValueModel;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.plan.PlanBlueprintValueEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.query.StorageFileQuery;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintValueCommonModelBuilder extends BaseCommonModelBuilder<PlanBlueprintValueModel, PlanBlueprintValueEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final StorageFileService storageFileService;
    private final UserScope userScope;
    private final QueryFactory queryFactory;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileProperties;
    private DefinitionEntity definition;

    @Autowired
    public PlanBlueprintValueCommonModelBuilder(
            ConventionService conventionService, StorageFileService storageFileService, UserScope userScope, QueryFactory queryFactory, ValidatorFactory validatorFactory, StorageFileProperties storageFileProperties
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintValueCommonModelBuilder.class)));
        this.storageFileService = storageFileService;
        this.userScope = userScope;
        this.queryFactory = queryFactory;
        this.validatorFactory = validatorFactory;
        this.storageFileProperties = storageFileProperties;
    }

    public PlanBlueprintValueCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintValueCommonModelBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    private boolean useSharedStorage;
    public PlanBlueprintValueCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanBlueprintValueModel, PlanBlueprintValueEntity>> buildInternal(List<PlanBlueprintValueEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanBlueprintValueModel, PlanBlueprintValueEntity>> models = new ArrayList<>();
        for (PlanBlueprintValueEntity d : data) {
            FieldEntity fieldEntity = this.definition != null ? this.definition.getFieldById(d.getFieldId()).stream().findFirst().orElse(null) : null;

            if (fieldEntity != null && fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) {
                ExtraFieldEntity extraFieldEntity = (ExtraFieldEntity) fieldEntity;
                PlanBlueprintValueModel m = new PlanBlueprintValueModel();
                m.setFieldId(d.getFieldId());
                if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isDateType(extraFieldEntity.getType())){
                    m.setDateValue(d.getDateValue());
                } else if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isNumberType(extraFieldEntity.getType())){
                    m.setNumberValue(d.getNumberValue());
                } else {
                    m.setValue(d.getValue());
                }
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            if (fieldEntity != null && fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Upload)) {
                PlanBlueprintValueModel m = new PlanBlueprintValueModel();
                m.setFieldId(d.getFieldId());
                m.setValue(d.getValue());

                if (d.getValue() != null && !d.getValue().isEmpty()) {
                    try {
                        byte[] bytes = this.storageFileService.readAsBytesSafe(UUID.fromString(d.getValue()));
                        FileEnvelopeModel fileEnvelopeModel = new FileEnvelopeModel();
                        StorageFileEntity storageFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(UUID.fromString(d.getValue())).first();
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
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }
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
        storageFilePersist.setLifetime(Duration.ofSeconds(this.storageFileProperties.getTempStoreLifetimeSeconds()));
        this.validatorFactory.validator(StorageFilePersist.StorageFilePersistValidator.class).validateForce(storageFilePersist);
        StorageFile persisted = this.storageFileService.persistBytes(storageFilePersist, bytes, new BaseFieldSet(StorageFile._id, StorageFile._fileRef));
        return persisted.getFileRef();
    }
}
