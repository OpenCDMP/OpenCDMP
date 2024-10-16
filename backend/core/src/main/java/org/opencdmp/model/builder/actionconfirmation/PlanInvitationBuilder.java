package org.opencdmp.model.builder.actionconfirmation;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.actionconfirmation.PlanInvitationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.actionconfirmation.PlanInvitation;
import org.opencdmp.model.builder.BaseBuilder;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanInvitationBuilder extends BaseBuilder<PlanInvitation, PlanInvitationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanInvitationBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanInvitationBuilder.class)));
    }

    public PlanInvitationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanInvitation> build(FieldSet fields, List<PlanInvitationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<PlanInvitation> models = new ArrayList<>();
        for (PlanInvitationEntity d : data) {
            PlanInvitation m = new PlanInvitation();
            if (fields.hasField(this.asIndexer(PlanInvitation._email))) m.setEmail(d.getEmail());
            if (fields.hasField(this.asIndexer(PlanInvitation._planId))) m.setPlanId(d.getPlanId());
            if (fields.hasField(this.asIndexer(PlanInvitation._sectionId))) m.setSectionId(d.getSectionId());
            if (fields.hasField(this.asIndexer(PlanInvitation._role))) m.setRole(d.getRole());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
