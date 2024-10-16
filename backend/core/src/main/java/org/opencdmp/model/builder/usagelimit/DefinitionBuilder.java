package org.opencdmp.model.builder.usagelimit;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.usagelimit.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.usagelimit.Definition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("usagelimitdefinitionbuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionBuilder extends BaseBuilder<Definition, DefinitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DefinitionBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefinitionBuilder.class)));
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

        List<Definition> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            Definition m = new Definition();
            if (fields.hasField(this.asIndexer(Definition._periodicityRange))) m.setPeriodicityRange(d.getPeriodicityRange());
            if (fields.hasField(this.asIndexer(Definition._hasPeriodicity))) m.setHasPeriodicity(d.getHasPeriodicity());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
