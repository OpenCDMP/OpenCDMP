package org.opencdmp.model.builder.pluginconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationUser;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginConfigurationUserBuilder extends BaseBuilder<PluginConfigurationUser, PluginConfigurationUserEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PluginConfigurationUserBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginConfigurationUserBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PluginConfigurationUserBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PluginConfigurationUser> build(FieldSet fields, List<PluginConfigurationUserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet configurationUserFields = fields.extractPrefixed(this.asPrefix(PluginConfigurationUser._userFields));

        List<PluginConfigurationUser> models = new ArrayList<>();
        for (PluginConfigurationUserEntity d : data) {
            PluginConfigurationUser m = new PluginConfigurationUser();
            if (fields.hasField(this.asIndexer(PluginConfigurationUser._pluginCode))) m.setPluginCode(d.getPluginCode());
            if (fields.hasField(this.asIndexer(PluginConfigurationUser._pluginType))) m.setPluginType(d.getPluginType());
            if (!configurationUserFields.isEmpty() && d.getUserFields() != null) m.setUserFields(this.builderFactory.builder(PluginConfigurationUserFieldBuilder.class).authorize(this.authorize).build(configurationUserFields, d.getUserFields()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
