package org.opencdmp.service.pluginconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationFieldEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserFieldEntity;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationFieldImportExport;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationImportExport;
import org.opencdmp.commons.types.storagefile.importexport.StorageFileImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationFieldPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserFieldPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserPersist;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationField;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
public class PluginConfigurationServiceImpl implements PluginConfigurationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PluginConfigurationServiceImpl.class));

    private final ConventionService conventionService;

    private final StorageFileService storageFileService;

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private final EncryptionService encryptionService;

    private final TenantProperties tenantProperties;

    private final UserScope userScope;


    @Autowired
    public PluginConfigurationServiceImpl(
            ConventionService conventionService,
            StorageFileService storageFileService, QueryFactory queryFactory, BuilderFactory builderFactory, EncryptionService encryptionService, TenantProperties tenantProperties, UserScope userScope) {
        this.conventionService = conventionService;
        this.storageFileService = storageFileService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
        this.userScope = userScope;
    }


    public PluginConfigurationEntity buildPluginConfigurationEntity(PluginConfigurationPersist persist, List<PluginConfigurationEntity> oldPlugins) throws InvalidApplicationException {
        PluginConfigurationEntity data = new PluginConfigurationEntity();
        if (persist == null)
            return data;

        data.setPluginCode(persist.getPluginCode());
        data.setPluginType(persist.getPluginType());

        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (PluginConfigurationFieldPersist pluginConfigurationFieldPersist : persist.getFields()) {
                data.getFields().add(this.buildPluginConfigurationFieldEntity(pluginConfigurationFieldPersist, !this.conventionService.isListNullOrEmpty(oldPlugins) ? oldPlugins.stream().filter(x->x.getPluginCode().equals(persist.getPluginCode())).findFirst().orElse(null): null));
            }
        }

        return data;
    }


    private @NotNull PluginConfigurationFieldEntity buildPluginConfigurationFieldEntity(PluginConfigurationFieldPersist persist, PluginConfigurationEntity oldPlugin) throws InvalidApplicationException {
        PluginConfigurationFieldEntity data = new PluginConfigurationFieldEntity();
        if (persist == null)
            return data;

        data.setCode(persist.getCode());
        data.setTextValue(persist.getTextValue());

        PluginConfigurationFieldEntity oldField = null;
        if(oldPlugin != null && !this.conventionService.isListNullOrEmpty(oldPlugin.getFields())) {
            oldField =  oldPlugin.getFields().stream().filter(x -> x.getCode().equals(persist.getCode()) && x.getFileValue() != null).findFirst().orElse(null);
        }


        UUID existingFileId = oldField != null ? oldField.getFileValue(): null;
        if (persist.getFileValue() != null){
            if (!persist.getFileValue().equals(existingFileId)) {
                StorageFile storageFile = this.storageFileService.copyToStorage(persist.getFileValue(), StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
                data.setFileValue(storageFile.getId());
            } else {
                data.setFileValue(existingFileId);
            }
        } else {
            if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
            data.setFileValue(null);
        }

        return data;
    }

    // reAssign for clone
    public void reassignPluginConfiguration(PluginConfiguration model) {
        if (model == null)
            return;

        if (model.getFields() != null) {
            for (PluginConfigurationField field : model.getFields()) {
                if (field.getFileValue() != null) {
                    StorageFile newStorageFile = this.storageFileService.cloneStorageFile(field.getFileValue());
                    if (newStorageFile != null) field.setFileValue(newStorageFile);
                }
            }
        }
    }

    // reAssign for new version
    public void reassignNewStorageFilesIfEqualsWithOld(List<PluginConfigurationPersist> persists, List<PluginConfigurationEntity> entities) {
        if (!this.conventionService.isListNullOrEmpty(entities) && !this.conventionService.isListNullOrEmpty(persists)) {
            List<PluginConfigurationFieldEntity> oldFields = entities.stream().map(PluginConfigurationEntity::getFields).toList().stream()
                    .flatMap(List::stream)
                    .toList();

            StorageFileQuery storageFileQuery = this.queryFactory.query(StorageFileQuery.class).disableTracking();
            FieldSet storageFileFields = new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._name).ensure(StorageFile._extension).ensure(StorageFile._mimeType);
            List<StorageFile> storageFiles = this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(storageFileFields, storageFileQuery.collectAs(storageFileFields));

            for (PluginConfigurationPersist pluginConfigurationPersist : persists) {
                for (PluginConfigurationFieldPersist fieldPersist : pluginConfigurationPersist.getFields()) {
                    if (fieldPersist.getFileValue() != null && oldFields.stream().filter(x -> x.getFileValue().equals(fieldPersist.getFileValue())).findFirst().orElse(null) != null) {
                        StorageFile oldstorageFile = storageFiles.stream().filter(x -> x.getId().equals(fieldPersist.getFileValue())).findFirst().orElse(null);
                        if (oldstorageFile != null) {
                            StorageFile newTempStorageFile = this.storageFileService.cloneStorageFile(oldstorageFile);
                            if (newTempStorageFile != null && newTempStorageFile.getId() != null) {
                                fieldPersist.setFileValue(newTempStorageFile.getId());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public PluginConfigurationPersist xmlPluginConfigurationToPersist(PluginConfigurationImportExport importXml) throws IOException {
        if (importXml == null) return null;

        PluginConfigurationPersist persist = new PluginConfigurationPersist();
        persist.setPluginCode(importXml.getPluginCode());
        persist.setPluginType(importXml.getPluginType());


        List<PluginConfigurationFieldPersist> pluginConfigurationFieldPersists = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getFields())) {
            for (PluginConfigurationFieldImportExport pluginConfigurationFields : importXml.getFields()) {
                pluginConfigurationFieldPersists.add(this.xmlPluginConfigurationFieldToPersist(pluginConfigurationFields));
            }
        }

        persist.setFields(pluginConfigurationFieldPersists);

        return persist;
    }

    private  PluginConfigurationFieldPersist xmlPluginConfigurationFieldToPersist(PluginConfigurationFieldImportExport importXml) throws IOException {
        if (importXml == null) return null;

        PluginConfigurationFieldPersist persist = new PluginConfigurationFieldPersist();
        persist.setCode(importXml.getCode());
        persist.setTextValue(importXml.getTextValue());
        persist.setFileValue(this.storageFileService.xmlUploadFieldToPersist(importXml.getStorageFile()));

        return persist;
    }


    @Override
    public PluginConfigurationImportExport pluginConfigurationXmlToExport(PluginConfigurationEntity entity) {
        if (entity == null) return null;

        PluginConfigurationImportExport xml = new PluginConfigurationImportExport();
        xml.setPluginCode(entity.getPluginCode());
        xml.setPluginType(entity.getPluginType());

        List<PluginConfigurationFieldImportExport> pluginConfigurationFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFields())) {
            for(PluginConfigurationFieldEntity pluginConfigurationField : entity.getFields()) {
                pluginConfigurationFieldModels.add(this.uploadFieldXmlToExport(pluginConfigurationField));

            }
        }
        xml.setFields(pluginConfigurationFieldModels);

        return xml;
    }

    private PluginConfigurationFieldImportExport uploadFieldXmlToExport(PluginConfigurationFieldEntity entity) {
        if (entity == null) return null;

        PluginConfigurationFieldImportExport xml = new PluginConfigurationFieldImportExport();
        xml.setCode(entity.getCode());
        xml.setTextValue(entity.getTextValue());
        xml.setStorageFile(this.storageFileService.storageFileXmlToExport(entity.getFileValue()));

        return xml;
    }

    //
    // Plugin User Configuration
    //

    @Override
    public PluginConfigurationUserEntity buildPluginConfigurationUserEntity(PluginConfigurationUserPersist persist, List<PluginConfigurationUserEntity> oldPlugins) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        PluginConfigurationUserEntity data = new PluginConfigurationUserEntity();
        if (persist == null)
            return data;

        data.setPluginCode(persist.getPluginCode());
        data.setPluginType(persist.getPluginType());

        if(!this.conventionService.isListNullOrEmpty(persist.getUserFields())){
            data.setUserFields(new ArrayList<>());
            for (PluginConfigurationUserFieldPersist pluginConfigurationUserFieldPersist : persist.getUserFields()) {
                data.getUserFields().add(this.buildPluginConfigurationUserFieldEntity(pluginConfigurationUserFieldPersist, !this.conventionService.isListNullOrEmpty(oldPlugins) ? oldPlugins.stream().filter(x->x.getPluginCode().equals(persist.getPluginCode())).findFirst().orElse(null): null));
            }
        }

        return data;
    }

    private @NotNull PluginConfigurationUserFieldEntity buildPluginConfigurationUserFieldEntity(PluginConfigurationUserFieldPersist persist, PluginConfigurationUserEntity oldPlugin) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        PluginConfigurationUserFieldEntity data = new PluginConfigurationUserFieldEntity();
        if (persist == null)
            return data;

        data.setCode(persist.getCode());
        if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) data.setTextValue(this.encryptionService.encryptAES(persist.getTextValue(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));

        PluginConfigurationUserFieldEntity oldField = null;
        if(oldPlugin != null && !this.conventionService.isListNullOrEmpty(oldPlugin.getUserFields())) {
            oldField =  oldPlugin.getUserFields().stream().filter(x -> x.getCode().equals(persist.getCode()) && x.getFileValue() != null).findFirst().orElse(null);
        }

        UUID existingFileId = oldField != null ? oldField.getFileValue(): null;
        if (persist.getFileValue() != null){
            if (!persist.getFileValue().equals(existingFileId)) {
                StorageFile storageFile = this.storageFileService.copyToStorage(persist.getFileValue(), StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
                data.setFileValue(storageFile.getId());
            } else {
                data.setFileValue(existingFileId);
            }
        } else {
            if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
            data.setFileValue(null);
        }

        return data;
    }

}