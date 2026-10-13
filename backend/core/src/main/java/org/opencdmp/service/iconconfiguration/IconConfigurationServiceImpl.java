package org.opencdmp.service.iconconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;
import org.opencdmp.commons.types.iconconfiguration.importexport.IconConfigurationImportExport;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.iconconfiguration.IconConfiguration;
import org.opencdmp.model.persist.iconconfiguration.IconConfigurationPersist;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.storage.StorageFileService;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class IconConfigurationServiceImpl implements IconConfigurationService {

    private final QueryFactory queryFactory;
    private final StorageFileService storageFileService;
    private final BuilderFactory builderFactory;

    public IconConfigurationServiceImpl(QueryFactory queryFactory, StorageFileService storageFileService, BuilderFactory builderFactory) {
        this.queryFactory = queryFactory;
        this.storageFileService = storageFileService;
        this.builderFactory = builderFactory;
    }

    public IconConfigurationEntity buildIconConfigurationEntity(IconConfigurationPersist persist, UUID oldStorageFileId) throws InvalidApplicationException {
        IconConfigurationEntity data = new IconConfigurationEntity();
        if (persist == null)
            return data;

        data.setMatIconName(persist.getMatIconName());
        if (persist.getStorageFileId() != null) {
            if (!persist.getStorageFileId().equals(oldStorageFileId)) {
                StorageFileEntity storageFileEntity = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(persist.getStorageFileId()).firstAs(new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._storageType));
                if (storageFileEntity == null || storageFileEntity.getStorageType().equals(StorageType.Temp)) {
                    StorageFile storageFile = this.storageFileService.copyToStorage(persist.getStorageFileId(), StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                    this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                    if (oldStorageFileId != null) this.storageFileService.updatePurgeAt(oldStorageFileId, Instant.now().plusSeconds(60));
                    data.setStorageFileId(storageFile.getId());
                }
            } else {
                data.setStorageFileId(persist.getStorageFileId());
            }
        } else {
            if (oldStorageFileId != null) this.storageFileService.updatePurgeAt(oldStorageFileId, Instant.now().plusSeconds(60));
        }

        return data;
    }

    // reAssign for clone
    public void reassignIconConfiguration(IconConfiguration model) {
        if (model == null)
            return;

        if (model.getStorageFile() != null && model.getStorageFile().getId() != null) {
            StorageFile newStorageFile = this.storageFileService.cloneStorageFile(model.getStorageFile());
            if (newStorageFile != null) model.setStorageFile(newStorageFile);
        }
    }

    // reAssign for new version
    public void reassignNewStorageFilesIfEqualsWithOld(IconConfigurationPersist model) {
        if (model == null || model.getStorageFileId() == null)
            return;

        StorageFileQuery storageFileQuery = this.queryFactory.query(StorageFileQuery.class).disableTracking();
        FieldSet storageFileFields = new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._name).ensure(StorageFile._extension).ensure(StorageFile._mimeType);
        List<StorageFile> storageFiles = this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(storageFileFields, storageFileQuery.collectAs(storageFileFields));

        StorageFile oldstorageFile = storageFiles.stream().filter(x -> x.getId().equals(model.getStorageFileId())).findFirst().orElse(null);
        if (oldstorageFile != null) {
            StorageFile newTempStorageFile = this.storageFileService.cloneStorageFile(oldstorageFile);
            if (newTempStorageFile != null && newTempStorageFile.getId() != null) {
                model.setStorageFileId(newTempStorageFile.getId());
            }
        }

    }

    public IconConfigurationPersist xmlIconConfigurationToPersist(IconConfigurationImportExport importXml) throws IOException {
        if (importXml == null) return null;

        IconConfigurationPersist persist = new IconConfigurationPersist();
        persist.setMatIconName(importXml.getMatIconName());
        persist.setStorageFileId(this.storageFileService.xmlUploadFieldToPersist(importXml.getStorageFile()));

        return persist;
    }

    public IconConfigurationImportExport iconConfigurationXmlToExport(IconConfigurationEntity entity) {
        if (entity == null) return null;

        IconConfigurationImportExport xml = new IconConfigurationImportExport();
        xml.setMatIconName(entity.getMatIconName());
        xml.setStorageFile(this.storageFileService.storageFileXmlToExport(entity.getStorageFileId()));

        return xml;
    }

}
