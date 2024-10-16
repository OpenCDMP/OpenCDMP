package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.model.Tenant;
import gr.cite.tools.data.builder.BuilderFactory;
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
public class TenantBuilder extends BaseBuilder<Tenant, TenantEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize  = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public TenantBuilder(ConventionService conventionService, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TenantBuilder.class)));
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
    }

    public TenantBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }
    @Override
    public List<Tenant> build(FieldSet fields, List<TenantEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0),Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size) .orElse(0));
        this.logger.trace(new DataLogEntry("requested fields",fields));
        if(fields == null || data == null || fields.isEmpty()) return new ArrayList<>();


        List<Tenant> models =  new ArrayList<>();
        for(TenantEntity d : data){
            Tenant m = new Tenant();
            if(fields.hasField(this.asIndexer(Tenant._id))) m.setId(d.getId());
            if(fields.hasField(this.asIndexer(Tenant._code))) m.setCode(d.getCode());
            if(fields.hasField(this.asIndexer(Tenant._name))) m.setName(d.getName());
            if(fields.hasField(this.asIndexer(Tenant._description))) m.setDescription(d.getDescription());
            if(fields.hasField(this.asIndexer(Tenant._isActive))) m.setIsActive(d.getIsActive());
            if(fields.hasField(this.asIndexer(Tenant._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if(fields.hasField(this.asIndexer(Tenant._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if(fields.hasField(this.asIndexer(Tenant._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            models.add(m);
        }
        this.logger.debug("build {} items",Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
