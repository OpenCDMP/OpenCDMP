package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.elastic.data.nested.NestedPlanStatusElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedPlanStatusElasticBuilder extends BaseElasticBuilder<NestedPlanStatusElasticEntity, PlanStatusEntity> {

    @Autowired
    public NestedPlanStatusElasticBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedPlanStatusElasticBuilder.class)));
    }

    @Override
    public List<NestedPlanStatusElasticEntity> build(List<PlanStatusEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedPlanStatusElasticEntity> models = new ArrayList<>();
        for (PlanStatusEntity d : data) {
            NestedPlanStatusElasticEntity m = new NestedPlanStatusElasticEntity();
            m.setId(d.getId());
            m.setName(d.getName());
            m.setInternalStatus(d.getInternalStatus());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
