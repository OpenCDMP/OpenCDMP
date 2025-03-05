package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.evaluation.SelectionConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.interfaces.SelectionConfiguration;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectionConfigurationBuilder extends BaseBuilder<SelectionConfiguration, SelectionConfigurationEntity> {

    public static class SelectionConfigurationKnownFields {
        public static final String _valueSetList = "valueSetList";
    }

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    @Autowired
    public SelectionConfigurationBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService,  new LoggerService(LoggerFactory.getLogger(SelectionConfigurationBuilder.class)));

        this.builderFactory = builderFactory;


    }

    public SelectionConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<SelectionConfiguration> build(FieldSet fields, List<SelectionConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet valueSetListFields = fields.extractPrefixed(this.asPrefix(SelectionConfigurationKnownFields._valueSetList));


        List<SelectionConfiguration> models = new ArrayList<>();
        for (SelectionConfigurationEntity d : data) {
            SelectionConfiguration m = new SelectionConfiguration();

            if (!valueSetListFields.isEmpty() && d.getValueSetList() != null) m.setValueSetList(this.builderFactory.builder(ValueSetBuilder.class).authorize(this.authorize).build(valueSetListFields, d.getValueSetList()));

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
        }





}
