package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.RankConfigEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RankConfigBuilder extends BaseBuilder<RankConfig, RankConfigEntity> {

    public static class RankConfigKnownFields {
        public static final String _rankType = "rankType";
        public static final String _valueRangeConfiguration = "valueRangeConfiguration";
        public static final String _selectionConfiguration = "selectionConfiguration";
    }

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public RankConfigBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RankConfigBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public RankConfigBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<RankConfig> build(FieldSet fields, List<RankConfigEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet valueRangeConfigurationFields = fields.extractPrefixed(this.asPrefix(RankConfigKnownFields._valueRangeConfiguration));
        FieldSet selectionConfigurationFields = fields.extractPrefixed(this.asPrefix(RankConfigKnownFields._selectionConfiguration));

        List<RankConfig> models = new ArrayList<>();
        for (RankConfigEntity d : data) {
            RankConfig m = new RankConfig();
            if (fields.hasField(this.asIndexer(RankConfigKnownFields._rankType))) m.setRankType(d.getRankType());
            if (!valueRangeConfigurationFields.isEmpty() && d.getValueRangeConfiguration() != null) m.setValueRangeConfiguration(this.builderFactory.builder(ValueRangeConfigurationBuilder.class).authorize(this.authorize).build(valueRangeConfigurationFields, d.getValueRangeConfiguration()));
            if (!selectionConfigurationFields.isEmpty() && d.getSelectionConfiguration() != null) m.setSelectionConfiguration(this.builderFactory.builder(SelectionConfigurationBuilder.class).authorize(this.authorize).build(selectionConfigurationFields, d.getSelectionConfiguration()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
