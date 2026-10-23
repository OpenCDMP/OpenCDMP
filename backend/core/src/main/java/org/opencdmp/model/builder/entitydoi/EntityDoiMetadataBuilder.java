package org.opencdmp.model.builder.entitydoi;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.entitydoi.EntityDoiMetadataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.entitydoi.EntityDoiMetadata;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityDoiMetadataBuilder extends BaseBuilder<EntityDoiMetadata, EntityDoiMetadataEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EntityDoiMetadataBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EntityDoiMetadataBuilder.class)));
    }

    public EntityDoiMetadataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EntityDoiMetadata> build(FieldSet fields, List<EntityDoiMetadataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<EntityDoiMetadata> models = new ArrayList<>();
        for (EntityDoiMetadataEntity d : data) {
            EntityDoiMetadata m = new EntityDoiMetadata();
            if (fields.hasField(this.asIndexer(EntityDoiMetadata._depositMethod))) m.setDepositMethod(d.getDepositMethod());
            if (fields.hasField(this.asIndexer(EntityDoiMetadata._userId))) m.setUserId(d.getUserId());
            if (fields.hasField(this.asIndexer(EntityDoiMetadata._accountId))) m.setAccountId(d.getAccountId());
            if (fields.hasField(this.asIndexer(EntityDoiMetadata._accountName))) m.setAccountName(d.getAccountName());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
