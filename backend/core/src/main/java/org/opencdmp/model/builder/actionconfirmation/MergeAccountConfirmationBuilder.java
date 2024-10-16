package org.opencdmp.model.builder.actionconfirmation;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.actionconfirmation.MergeAccountConfirmationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.actionconfirmation.MergeAccountConfirmation;
import org.opencdmp.model.builder.BaseBuilder;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MergeAccountConfirmationBuilder extends BaseBuilder<MergeAccountConfirmation, MergeAccountConfirmationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public MergeAccountConfirmationBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(MergeAccountConfirmationBuilder.class)));
    }

    public MergeAccountConfirmationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<MergeAccountConfirmation> build(FieldSet fields, List<MergeAccountConfirmationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<MergeAccountConfirmation> models = new ArrayList<>();
        for (MergeAccountConfirmationEntity d : data) {
            MergeAccountConfirmation m = new MergeAccountConfirmation();
            if (fields.hasField(this.asIndexer(MergeAccountConfirmation._email))) m.setEmail(d.getEmail());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
