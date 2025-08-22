package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.ViewPreferencesConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.viewpreference.ViewPreferenceBuilder;
import org.opencdmp.model.tenantconfiguration.ViewPreferencesConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ViewPreferencesConfigurationBuilder extends BaseBuilder<ViewPreferencesConfiguration, ViewPreferencesConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;

    @Autowired
    public ViewPreferencesConfigurationBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory){
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ViewPreferencesConfigurationBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public ViewPreferencesConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }

    @Override
    public List<ViewPreferencesConfiguration> build(FieldSet fields, List<ViewPreferencesConfigurationEntity> data) throws MyApplicationException {

        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planPreferences = fields.extractPrefixed(this.asPrefix(ViewPreferencesConfiguration._planPreferences));
        FieldSet descriptionPreferences = fields.extractPrefixed(this.asPrefix(ViewPreferencesConfiguration._descriptionPreferences));

        List<ViewPreferencesConfiguration> models = new ArrayList<>();
        for(ViewPreferencesConfigurationEntity d : data){
            ViewPreferencesConfiguration m = new ViewPreferencesConfiguration();

            if (!planPreferences.isEmpty() && d.getPlanPreferences() != null) m.setPlanPreferences(this.builderFactory.builder(ViewPreferenceBuilder.class).authorize(this.authorize).build(planPreferences, d.getPlanPreferences()));
            if (!descriptionPreferences.isEmpty() && d.getDescriptionPreferences() != null) m.setDescriptionPreferences(this.builderFactory.builder(ViewPreferenceBuilder.class).authorize(this.authorize).build(planPreferences, d.getDescriptionPreferences()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
