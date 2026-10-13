package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.PlanCreationConsentEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.PlanCreationConsent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planCreationConsentBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanCreationConsentBuilder extends BaseBuilder<PlanCreationConsent, PlanCreationConsentEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanCreationConsentBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanCreationConsentBuilder.class)));
    }

    public PlanCreationConsentBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanCreationConsent> build(FieldSet fields, List<PlanCreationConsentEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanCreationConsent> models = new ArrayList<>();
        for (PlanCreationConsentEntity d : data) {
            PlanCreationConsent m = new PlanCreationConsent();
            if (fields.hasField(this.asIndexer(PlanCreationConsent._enabled))) m.setEnabled(d.getEnabled());
            if (fields.hasField(this.asIndexer(PlanCreationConsent._message))) m.setMessage(d.getMessage());
            if (fields.hasField(this.asIndexer(PlanCreationConsent._details))) m.setDetails(d.getDetails());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
