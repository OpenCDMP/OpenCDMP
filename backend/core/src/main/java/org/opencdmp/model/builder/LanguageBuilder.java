package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LanguageEntity;
import org.opencdmp.model.Language;
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
public class LanguageBuilder extends BaseBuilder<Language, LanguageEntity>{
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public LanguageBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LanguageBuilder.class)));
	    this.tenantScope = tenantScope;
    }

    public LanguageBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Language> build(FieldSet fields, List<LanguageEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Language> models = new ArrayList<>();
        for (LanguageEntity d : data) {
            Language m = new Language();
            if (fields.hasField(this.asIndexer(Language._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Language._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(Language._payload))) m.setPayload(d.getPayload());
            if (fields.hasField(this.asIndexer(Language._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Language._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Language._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Language._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(Language._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(Language._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
