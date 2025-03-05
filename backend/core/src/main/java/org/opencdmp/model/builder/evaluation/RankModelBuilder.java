package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.RankModelEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.RankModel;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RankModelBuilder extends BaseBuilder<RankModel, RankModelEntity> {

    public static class RankModelKnownFields {
        public static final String _rank = "rank";
        public static final String _messages = "messages";
        public static final String _details = "details";
    }

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public RankModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RankModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public RankModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<RankModel> build(FieldSet fields, List<RankModelEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<RankModel> models = new ArrayList<>();
        for (RankModelEntity d : data) {
            RankModel m = new RankModel();
            if (fields.hasField(this.asIndexer(RankModelKnownFields._rank))) m.setRank(d.getRank());
            if (fields.hasField(this.asIndexer(RankModelKnownFields._details))) m.setDetails(d.getDetails());
            if (fields.hasField(this.asIndexer(RankModelKnownFields._messages))) m.setMessages(d.getMessages());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
