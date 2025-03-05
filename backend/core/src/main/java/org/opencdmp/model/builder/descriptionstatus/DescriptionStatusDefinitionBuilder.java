package org.opencdmp.model.builder.descriptionstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinition;
import org.opencdmp.query.StorageFileQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusDefinitionBuilder extends BaseBuilder<DescriptionStatusDefinition, DescriptionStatusDefinitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    public DescriptionStatusDefinitionBuilder(ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusDefinitionBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public DescriptionStatusDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionStatusDefinition> build(FieldSet fields, List<DescriptionStatusDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionStatusDefinition> models = new ArrayList<>();

        FieldSet storageFileFields = fields.extractPrefixed(this.asPrefix(DescriptionStatusDefinition._storageFile));
        Map<UUID, StorageFile> storageFileItemsMap = this.collectStorageFiles(storageFileFields, data);

        FieldSet authorizationFields = fields.extractPrefixed(this.asPrefix(DescriptionStatusDefinition._authorization));
        for (DescriptionStatusDefinitionEntity d : data) {
            DescriptionStatusDefinition m = new DescriptionStatusDefinition();
            if (!authorizationFields.isEmpty() && d.getAuthorization() != null) {
                m.setAuthorization(this.builderFactory.builder(DescriptionStatusDefinitionAuthorizationBuilder.class).authorize(authorize).build(authorizationFields, d.getAuthorization()));
            }
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinition._availableActions))) m.setAvailableActions(d.getAvailableActions());

            if(fields.hasField(this.asIndexer(DescriptionStatusDefinition._matIconName))) m.setMatIconName(d.getMatIconName());

            if(fields.hasField(this.asIndexer(DescriptionStatusDefinition._statusColor))) m.setStatusColor(d.getStatusColor());

            if (!storageFileFields.isEmpty() && storageFileItemsMap != null && storageFileItemsMap.containsKey(d.getStorageFileId()))  m.setStorageFile(storageFileItemsMap.get(d.getStorageFileId()));

            models.add(m);
        }

        return models;
    }

    private Map<UUID, StorageFile> collectStorageFiles(FieldSet fields, List<DescriptionStatusDefinitionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", StorageFile.class.getSimpleName());

        Map<UUID, StorageFile> itemMap;
        if (!fields.hasOtherField(this.asIndexer(StorageFile._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionStatusDefinitionEntity::getStorageFileId).distinct().collect(Collectors.toList()),
                    x -> {
                        StorageFile item = new StorageFile();
                        item.setId(x);
                        return item;
                    },
                    StorageFile::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(StorageFile._id);
            if (fields.hasField(StorageFile._fullName)) clone.ensure(StorageFile._name).ensure(StorageFile._extension);
            StorageFileQuery q = this.queryFactory.query(StorageFileQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionStatusDefinitionEntity::getStorageFileId).distinct().collect(Collectors.toList()));
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
