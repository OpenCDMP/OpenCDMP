package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.model.*;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicEntityDoiBuilder extends BaseBuilder<PublicEntityDoi, EntityDoiEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicEntityDoiBuilder(
            ConventionService conventionService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicEntityDoiBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public PublicEntityDoiBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicEntityDoi> build(FieldSet fields, List<EntityDoiEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PublicEntityDoi> models = new ArrayList<>();
        for (EntityDoiEntity d : data) {
            PublicEntityDoi m = new PublicEntityDoi();
            if (fields.hasField(this.asIndexer(PublicEntityDoi._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicEntityDoi._entityType)))  m.setEntityType(d.getEntityType());
            if (fields.hasField(this.asIndexer(PublicEntityDoi._repositoryId))) m.setRepositoryId(d.getRepositoryId());
            if (fields.hasField(this.asIndexer(PublicEntityDoi._doi))) m.setDoi(d.getDoi());
            if (fields.hasField(this.asIndexer(PublicEntityDoi._entityId))) m.setEntityId(d.getEntityId());

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
