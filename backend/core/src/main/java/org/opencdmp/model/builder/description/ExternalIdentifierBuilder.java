package org.opencdmp.model.builder.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.description.ExternalIdentifierEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.description.ExternalIdentifier;
import org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("description.ExternalIdentifierBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalIdentifierBuilder extends BaseBuilder<ExternalIdentifier, ExternalIdentifierEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ExternalIdentifierBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, FieldDataHelperServiceProvider fieldDataHelperServiceProvider) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ExternalIdentifierBuilder.class)));
    }

    public ExternalIdentifierBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ExternalIdentifier> build(FieldSet fields, List<ExternalIdentifierEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<ExternalIdentifier> models = new ArrayList<>();
        for (ExternalIdentifierEntity d : data) {
            ExternalIdentifier m = new ExternalIdentifier();
            if (fields.hasField(this.asIndexer(ExternalIdentifier._identifier))) m.setIdentifier(d.getIdentifier());
            if (fields.hasField(this.asIndexer(ExternalIdentifier._type))) m.setType(d.getType());
            
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
