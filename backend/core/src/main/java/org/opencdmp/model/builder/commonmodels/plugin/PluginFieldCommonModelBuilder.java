package org.opencdmp.model.builder.commonmodels.plugin;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plugin.PluginFieldModel;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
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
public class PluginFieldCommonModelBuilder extends BaseCommonModelBuilder<PluginFieldModel, PluginConfigurationFieldEntity> {
    private final StorageFileService storageFileService;
    private final QueryFactory queryFactory;
    private final UserScope userScope;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileProperties;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PluginFieldCommonModelBuilder(
            ConventionService conventionService, StorageFileService storageFileService, QueryFactory queryFactory, UserScope userScope, ValidatorFactory validatorFactory, StorageFileProperties storageFileProperties
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginFieldCommonModelBuilder.class)));
        this.storageFileService = storageFileService;
        this.queryFactory = queryFactory;
        this.userScope = userScope;
        this.validatorFactory = validatorFactory;
        this.storageFileProperties = storageFileProperties;
    }

    public PluginFieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private boolean useSharedStorage;
    public PluginFieldCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PluginFieldModel, PluginConfigurationFieldEntity>> buildInternal(List<PluginConfigurationFieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginFieldModel, PluginConfigurationFieldEntity>> models = new ArrayList<>();
        for (PluginConfigurationFieldEntity d : data) {
            PluginFieldModel m = new PluginFieldModel();
            m.setCode(d.getCode());
            m.setTextValue(d.getTextValue());

            if (d.getFileValue() != null) {
                try {
                    byte[] bytes = this.storageFileService.readAsBytesSafe(d.getFileValue());
                    FileEnvelopeModel fileEnvelopeModel = new FileEnvelopeModel();
                    StorageFileEntity storageFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(d.getFileValue()).first();
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
}
