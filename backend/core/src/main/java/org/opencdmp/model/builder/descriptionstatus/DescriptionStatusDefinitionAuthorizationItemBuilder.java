package org.opencdmp.model.builder.descriptionstatus;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinitionAuthorizationItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusDefinitionAuthorizationItemBuilder extends BaseBuilder<DescriptionStatusDefinitionAuthorizationItem, DescriptionStatusDefinitionAuthorizationItemEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public DescriptionStatusDefinitionAuthorizationItemBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusDefinitionAuthorizationItemBuilder.class)));
    }

    public DescriptionStatusDefinitionAuthorizationItemBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionStatusDefinitionAuthorizationItem> build(FieldSet fields, List<DescriptionStatusDefinitionAuthorizationItemEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionStatusDefinitionAuthorizationItem> models = new ArrayList<>();
        for (DescriptionStatusDefinitionAuthorizationItemEntity d : data) {
            DescriptionStatusDefinitionAuthorizationItem m = new DescriptionStatusDefinitionAuthorizationItem();
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinitionAuthorizationItem._planRoles))) m.setPlanRoles(d.getPlanRoles());
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinitionAuthorizationItem._roles))) m.setRoles(d.getRoles());
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinitionAuthorizationItem._allowAnonymous))) m.setAllowAnonymous(d.getAllowAnonymous());
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinitionAuthorizationItem._allowAuthenticated))) m.setAllowAuthenticated(d.getAllowAuthenticated());

            models.add(m);
        }
        return models;
    }
}
