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
public class ValueSetBuilder extends BaseBuilder<SelectionConfiguration.ValueSet, SelectionConfigurationEntity.ValueSetEntity> {

    public static class ValueSetKnownFields {
        public static final String _key = "key";
        public static final String _successStatus = "successStatus";
    }

    @Autowired
    public ValueSetBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService,  new LoggerService(LoggerFactory.getLogger(ValueSetBuilder.class)));
    }

    public ValueSetBuilder authorize(EnumSet<AuthorizationFlags> values) {
        return this;
    }

    @Override
    public List<SelectionConfiguration.ValueSet> build(FieldSet fields, List<SelectionConfigurationEntity.ValueSetEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<SelectionConfiguration.ValueSet> models = new ArrayList<>();
        for (SelectionConfigurationEntity.ValueSetEntity d : data) {
            SelectionConfiguration.ValueSet m = new SelectionConfiguration.ValueSet();

            if(fields.hasField(this.asIndexer(ValueSetKnownFields._key))) m.setKey(d.getKey());
            if(fields.hasField(this.asIndexer(ValueSetKnownFields._successStatus))) m.setSuccessStatus(d.getSuccessStatus());

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
        }





}
