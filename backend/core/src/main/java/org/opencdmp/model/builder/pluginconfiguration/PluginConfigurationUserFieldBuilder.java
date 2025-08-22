package org.opencdmp.model.builder.pluginconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationUserField;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginConfigurationUserFieldBuilder extends BaseBuilder<PluginConfigurationUserField, PluginConfigurationUserFieldEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PluginConfigurationUserFieldBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory, EncryptionService encryptionService, TenantProperties tenantProperties) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginConfigurationUserFieldBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
    }

    public PluginConfigurationUserFieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    public List<PluginConfigurationUserField> build(FieldSet fields, List<PluginConfigurationUserFieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet valueFields = fields.extractPrefixed(this.asPrefix(PluginConfigurationUserField._fileValue));
        Map<UUID, StorageFile> storageFileItemsMap = this.collectStorageFiles(valueFields, data);


        List<PluginConfigurationUserField> models = new ArrayList<>();
        for (PluginConfigurationUserFieldEntity d : data) {
            PluginConfigurationUserField m = new PluginConfigurationUserField();
            if (fields.hasField(this.asIndexer(PluginConfigurationUserField._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(PluginConfigurationUserField._textValue))){
                try {
                    if (!this.conventionService.isNullOrEmpty(d.getTextValue())) m.setTextValue(this.encryptionService.decryptAES(d.getTextValue(), tenantProperties.getConfigEncryptionAesKey(), tenantProperties.getConfigEncryptionAesIv()));
                } catch (Exception e) {
                    this.logger.error( e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!valueFields.isEmpty() && storageFileItemsMap != null && storageFileItemsMap.containsKey(d.getFileValue()))  m.setFileValue(storageFileItemsMap.get(d.getFileValue()));

            models.add(m);


        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, StorageFile> collectStorageFiles(FieldSet fields, List<PluginConfigurationUserFieldEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", StorageFile.class.getSimpleName());

        Map<UUID, StorageFile> itemMap;
        if (!fields.hasOtherField(this.asIndexer(StorageFile._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PluginConfigurationUserFieldEntity::getFileValue).distinct().collect(Collectors.toList()),
                    x -> {
                        StorageFile item = new StorageFile();
                        item.setId(x);
                        return item;
                    },
                    StorageFile::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(StorageFile._id);
            if (fields.hasField(StorageFile._fullName)) clone.ensure(StorageFile._name).ensure(StorageFile._extension);
            StorageFileQuery q = this.queryFactory.query(StorageFileQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PluginConfigurationUserFieldEntity::getFileValue).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(StorageFileBuilder.class).authorize(this.authorize).asForeignKey(q, clone, StorageFile::getId);
        }
        if (!fields.hasField(StorageFile._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        if (!fields.hasField(StorageFile._name)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setName(null);
            });
        }

        if (!fields.hasField(StorageFile._extension)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setExtension(null);
            });
        }

        return itemMap;
    }


}
