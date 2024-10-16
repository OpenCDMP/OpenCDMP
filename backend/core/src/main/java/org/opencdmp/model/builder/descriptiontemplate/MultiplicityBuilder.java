package org.opencdmp.model.builder.descriptiontemplate;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.MultiplicityEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.Multiplicity;
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

@Component("descriptiontemplatedefinitionmultiplicitybuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MultiplicityBuilder extends BaseBuilder<Multiplicity, MultiplicityEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public MultiplicityBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(MultiplicityBuilder.class)));
    }

    public MultiplicityBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Multiplicity> build(FieldSet fields, List<MultiplicityEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Multiplicity> models = new ArrayList<>();
        for (MultiplicityEntity d : data) {
            Multiplicity m = new Multiplicity();
            if (fields.hasField(this.asIndexer(Multiplicity._min))) m.setMin(d.getMin());
            if (fields.hasField(this.asIndexer(Multiplicity._max))) m.setMax(d.getMax());
            if (fields.hasField(this.asIndexer(Multiplicity._placeholder))) m.setPlaceholder(d.getPlaceholder());
            if (fields.hasField(this.asIndexer(Multiplicity._tableView))) m.setTableView(d.getTableView());
            
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
