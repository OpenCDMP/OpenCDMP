package org.opencdmp.model.builder.planstatus;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planstatus.PlanStatusDefinitionAuthorizationItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusDefinitionAuthorizationItemBuilder extends BaseBuilder<PlanStatusDefinitionAuthorizationItem, PlanStatusDefinitionAuthorizationItemEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanStatusDefinitionAuthorizationItemBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusDefinitionAuthorizationItemBuilder.class)));
    }

    public PlanStatusDefinitionAuthorizationItemBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanStatusDefinitionAuthorizationItem> build(FieldSet fields, List<PlanStatusDefinitionAuthorizationItemEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanStatusDefinitionAuthorizationItem> models = new ArrayList<>();
        for (PlanStatusDefinitionAuthorizationItemEntity d : data) {
            PlanStatusDefinitionAuthorizationItem m = new PlanStatusDefinitionAuthorizationItem();
            if (fields.hasField(this.asIndexer(PlanStatusDefinitionAuthorizationItem._planRoles))) m.setPlanRoles(d.getPlanRoles());
            if (fields.hasField(this.asIndexer(PlanStatusDefinitionAuthorizationItem._roles))) m.setRoles(d.getRoles());
            if (fields.hasField(this.asIndexer(PlanStatusDefinitionAuthorizationItem._allowAuthenticated))) m.setAllowAuthenticated(d.getAllowAuthenticated());
            if (fields.hasField(this.asIndexer(PlanStatusDefinitionAuthorizationItem._allowAnonymous))) m.setAllowAnonymous(d.getAllowAnonymous());
            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
