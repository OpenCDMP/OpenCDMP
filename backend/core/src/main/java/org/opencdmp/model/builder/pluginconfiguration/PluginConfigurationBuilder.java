package org.opencdmp.model.builder.pluginconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginConfigurationBuilder extends BaseBuilder<PluginConfiguration, PluginConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PluginConfigurationBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginConfigurationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PluginConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PluginConfiguration> build(FieldSet fields, List<PluginConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet configurationFields = fields.extractPrefixed(this.asPrefix(PluginConfiguration._fields));

        List<PluginConfiguration> models = new ArrayList<>();
        for (PluginConfigurationEntity d : data) {
            PluginConfiguration m = new PluginConfiguration();
            if (fields.hasField(this.asIndexer(PluginConfiguration._pluginCode))) m.setPluginCode(d.getPluginCode());
            if (fields.hasField(this.asIndexer(PluginConfiguration._pluginType))) m.setPluginType(d.getPluginType());
            if (!configurationFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(PluginConfigurationFieldBuilder.class).authorize(this.authorize).build(configurationFields, d.getFields()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
