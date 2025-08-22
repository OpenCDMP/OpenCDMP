package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.PluginTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.pluginconfiguration.PluginConfigurationBuilder;
import org.opencdmp.model.tenantconfiguration.PluginTenantConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginTenantConfigurationBuilder extends BaseBuilder<PluginTenantConfiguration, PluginTenantConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;

    @Autowired
    public PluginTenantConfigurationBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginTenantConfigurationBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public PluginTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PluginTenantConfiguration> build(FieldSet fields, List<PluginTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        FieldSet pluginConfigurationFields = fields.extractPrefixed(this.asPrefix(org.opencdmp.model.planblueprint.Definition._pluginConfigurations));

        List<PluginTenantConfiguration> models = new ArrayList<>();
        for (PluginTenantConfigurationEntity d : data) {

            PluginTenantConfiguration m = new PluginTenantConfiguration();
            if (!pluginConfigurationFields.isEmpty() && d.getPluginConfigurations() != null) m.setPluginConfigurations(this.builderFactory.builder(PluginConfigurationBuilder.class).authorize(this.authorize).build(pluginConfigurationFields, d.getPluginConfigurations()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
