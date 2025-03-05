package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.DefaultPlanBlueprintConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.DefaultPlanBlueprintConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultPlanBlueprintConfigurationBuilder extends BaseBuilder<DefaultPlanBlueprintConfiguration, DefaultPlanBlueprintConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DefaultPlanBlueprintConfigurationBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefaultPlanBlueprintConfigurationBuilder.class)));
    }

    public DefaultPlanBlueprintConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DefaultPlanBlueprintConfiguration> build(FieldSet fields, List<DefaultPlanBlueprintConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DefaultPlanBlueprintConfiguration> models = new ArrayList<>();
        for (DefaultPlanBlueprintConfigurationEntity d : data) {
            DefaultPlanBlueprintConfiguration m = new DefaultPlanBlueprintConfiguration();
            if (fields.hasField(this.asIndexer(DefaultPlanBlueprintConfiguration._groupId))) m.setGroupId(d.getGroupId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
