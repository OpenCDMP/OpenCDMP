package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.DashboardSlidesTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.DashboardSlidesTenantConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DashboardSlidesTenantConfigurationBuilder extends BaseBuilder<DashboardSlidesTenantConfiguration, DashboardSlidesTenantConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;

    @Autowired
    public DashboardSlidesTenantConfigurationBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DashboardSlidesTenantConfigurationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public DashboardSlidesTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DashboardSlidesTenantConfiguration> build(FieldSet fields, List<DashboardSlidesTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet slidesFields = fields.extractPrefixed(this.asPrefix(DashboardSlidesTenantConfiguration._slides));

        List<DashboardSlidesTenantConfiguration> models = new ArrayList<>();
        for (DashboardSlidesTenantConfigurationEntity d : data) {
            DashboardSlidesTenantConfiguration m = new DashboardSlidesTenantConfiguration();
            if (fields.hasField(this.asIndexer(DashboardSlidesTenantConfiguration._version))) m.setVersion(d.getVersion());
            if (!slidesFields.isEmpty() && d.getSlides() != null) {
                m.setSlides(this.builderFactory.builder(DashboardSlideBuilder.class).authorize(this.authorize).build(slidesFields, d.getSlides()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
