package org.opencdmp.model.builder.tenantconfiguration;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.DefaultUserLocaleTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.DefaultUserLocaleTenantConfiguration;
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
public class DefaultUserLocaleTenantConfigurationBuilder extends BaseBuilder<DefaultUserLocaleTenantConfiguration, DefaultUserLocaleTenantConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DefaultUserLocaleTenantConfigurationBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefaultUserLocaleTenantConfigurationBuilder.class)));
    }

    public DefaultUserLocaleTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DefaultUserLocaleTenantConfiguration> build(FieldSet fields, List<DefaultUserLocaleTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DefaultUserLocaleTenantConfiguration> models = new ArrayList<>();
        for (DefaultUserLocaleTenantConfigurationEntity d : data) {
            DefaultUserLocaleTenantConfiguration m = new DefaultUserLocaleTenantConfiguration();
            if (fields.hasField(this.asIndexer(DefaultUserLocaleTenantConfiguration._culture))) m.setCulture(d.getCulture());
            if (fields.hasField(this.asIndexer(DefaultUserLocaleTenantConfiguration._timezone))) m.setTimezone(d.getTimezone());
            if (fields.hasField(this.asIndexer(DefaultUserLocaleTenantConfiguration._language))) m.setLanguage(d.getLanguage());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
