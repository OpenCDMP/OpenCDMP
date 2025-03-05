package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.evaluation.ValueRangeConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.interfaces.ValueRangeConfiguration;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ValueRangeConfigurationBuilder extends BaseBuilder<ValueRangeConfiguration, ValueRangeConfigurationEntity> {

    public static class ValueRangeConfigurationKnownFields {
        public static final String _numberType = "numberType";
        public static final String _min = "min";
        public static final String _max = "max";
        public static final String _minPassValue = "max";

    }

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    @Autowired
    public ValueRangeConfigurationBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService,  new LoggerService(LoggerFactory.getLogger(ValueRangeConfigurationBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.tenantScope = tenantScope;
        this.builderFactory = builderFactory;


    }

    public ValueRangeConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ValueRangeConfiguration> build(FieldSet fields, List<ValueRangeConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<ValueRangeConfiguration> models = new ArrayList<>();
        for (ValueRangeConfigurationEntity d : data) {
            ValueRangeConfiguration m = new ValueRangeConfiguration();
            if (fields.hasField(this.asIndexer(ValueRangeConfigurationKnownFields._numberType))) m.setNumberType(d.getNumberType());
            if (fields.hasField(this.asIndexer(ValueRangeConfigurationKnownFields._min))) m.setMin(d.getMin());
            if (fields.hasField(this.asIndexer(ValueRangeConfigurationKnownFields._max))) m.setMax(d.getMax());
            if (fields.hasField(this.asIndexer(ValueRangeConfigurationKnownFields._minPassValue))) m.setMinPassValue(d.getMinPassValue());

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
        }
}
