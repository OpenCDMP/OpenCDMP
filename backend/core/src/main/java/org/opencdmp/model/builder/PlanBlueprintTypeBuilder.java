package org.opencdmp.model.builder;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintTypeEntity;
import org.opencdmp.model.PlanBlueprintType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintTypeBuilder extends BaseBuilder<PlanBlueprintType, PlanBlueprintTypeEntity> {

    private final TenantScopeFactory tenantScopeFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanBlueprintTypeBuilder(
		    ConventionService conventionService, TenantScopeFactory tenantScopeFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintTypeBuilder.class)));
	    this.tenantScopeFactory = tenantScopeFactory;
    }

    public PlanBlueprintTypeBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanBlueprintType> build(FieldSet fields, List<PlanBlueprintTypeEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanBlueprintType> models = new ArrayList<>();
        for (PlanBlueprintTypeEntity d : data) {
            PlanBlueprintType m = new PlanBlueprintType();
            if (fields.hasField(this.asIndexer(PlanBlueprintType._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._code)))
                m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._name)))
                m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(PlanBlueprintType._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanBlueprintType._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
