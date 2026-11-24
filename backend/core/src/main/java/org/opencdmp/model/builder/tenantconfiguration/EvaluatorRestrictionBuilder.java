package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.EvaluatorRestrictionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.EvaluatorRestriction;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluatorRestrictionBuilder extends BaseBuilder<EvaluatorRestriction, EvaluatorRestrictionEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EvaluatorRestrictionBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluatorRestrictionBuilder.class)));
    }

    public EvaluatorRestrictionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EvaluatorRestriction> build(FieldSet fields, List<EvaluatorRestrictionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<EvaluatorRestriction> models = new ArrayList<>();
        for (EvaluatorRestrictionEntity d : data) {
            EvaluatorRestriction m = new EvaluatorRestriction();
            if (fields.hasField(this.asIndexer(EvaluatorRestriction._evaluatorId))) m.setEvaluatorId(d.getEvaluatorId());
            if (fields.hasField(this.asIndexer(EvaluatorRestriction._benchmarkIds))) m.setBenchmarkIds(d.getBenchmarkIds() != null ? d.getBenchmarkIds() : new ArrayList<>());
            if (fields.hasField(this.asIndexer(EvaluatorRestriction._testIds))) m.setTestIds(d.getTestIds() != null ? d.getTestIds() : new ArrayList<>());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
