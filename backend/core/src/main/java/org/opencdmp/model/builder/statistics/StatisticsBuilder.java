package org.opencdmp.model.builder.statistics;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.types.StatisticsNameResult;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.StatisticsNameQueryResponse;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StatisticsBuilder extends BaseBuilder<StatisticsNameQueryResponse, StatisticsNameResult> {


    public StatisticsBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(StatisticsBuilder.class)));
    }

    @Override
    public List<StatisticsNameQueryResponse> build(FieldSet fields, List<StatisticsNameResult> data) throws MyApplicationException {
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        List<StatisticsNameQueryResponse> models = new ArrayList<>();
        for (StatisticsNameResult d : data) {
            StatisticsNameQueryResponse m = new StatisticsNameQueryResponse();
            if (fields.hasField(this.asIndexer(StatisticsNameQueryResponse._name))) {
                m.setName(d.getValue());
            }
            if (fields.hasField(this.asIndexer(StatisticsNameQueryResponse._id))) {
                m.setId(d.getKey());
            }
            models.add(m);
        }
        return models;
    }
}
