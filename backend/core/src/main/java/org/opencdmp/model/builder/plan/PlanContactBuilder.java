package org.opencdmp.model.builder.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.plan.PlanContactEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.plan.PlanContact;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("plancontactbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanContactBuilder extends BaseBuilder<PlanContact, PlanContactEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanContactBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanContactBuilder.class)));
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public PlanContactBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanContact> build(FieldSet fields, List<PlanContactEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db

        List<PlanContact> models = new ArrayList<>();
        for (PlanContactEntity d : data) {
            PlanContact m = new PlanContact();
            if (fields.hasField(this.asIndexer(PlanContact._email))) m.setEmail(d.getEmail());
            if (fields.hasField(this.asIndexer(PlanContact._firstName))) m.setFirstName(d.getFirstName());
            if (fields.hasField(this.asIndexer(PlanContact._lastName))) m.setLastName(d.getLastName());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
