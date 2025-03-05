package org.opencdmp.model.builder.tenantconfiguration;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.CssColorsTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.CssColorsTenantConfiguration;
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
public class CssColorsTenantConfigurationBuilder extends BaseBuilder<CssColorsTenantConfiguration, CssColorsTenantConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public CssColorsTenantConfigurationBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(CssColorsTenantConfigurationBuilder.class)));
    }

    public CssColorsTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<CssColorsTenantConfiguration> build(FieldSet fields, List<CssColorsTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<CssColorsTenantConfiguration> models = new ArrayList<>();
        for (CssColorsTenantConfigurationEntity d : data) {
            CssColorsTenantConfiguration m = new CssColorsTenantConfiguration();
            if (fields.hasField(this.asIndexer(CssColorsTenantConfiguration._primaryColor))) m.setPrimaryColor(d.getPrimaryColor());
            if (fields.hasField(this.asIndexer(CssColorsTenantConfiguration._cssOverride))) m.setCssOverride(d.getCssOverride());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
