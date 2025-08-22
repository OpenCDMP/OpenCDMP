package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.pluginconfiguration.PluginConfigurationBuilder;
import org.opencdmp.model.descriptiontemplate.Definition;
import gr.cite.tools.data.builder.BuilderFactory;
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

@Component("descriptiontemplatedefinitionbuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionBuilder extends BaseBuilder<Definition, DefinitionEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DefinitionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefinitionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public DefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Definition> build(FieldSet fields, List<DefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet pagesFields = fields.extractPrefixed(this.asPrefix(Definition._pages));
        FieldSet pluginConfigurationFields = fields.extractPrefixed(this.asPrefix(org.opencdmp.model.planblueprint.Definition._pluginConfigurations));

        List<Definition> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            Definition m = new Definition();
            if (!pluginConfigurationFields.isEmpty() && d.getPluginConfigurations() != null) m.setPluginConfigurations(this.builderFactory.builder(PluginConfigurationBuilder.class).authorize(this.authorize).build(pluginConfigurationFields, d.getPluginConfigurations()));
            if (!pagesFields.isEmpty() && d.getPages() != null) m.setPages(this.builderFactory.builder(PageBuilder.class).authorize(this.authorize).build(pagesFields, d.getPages()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
