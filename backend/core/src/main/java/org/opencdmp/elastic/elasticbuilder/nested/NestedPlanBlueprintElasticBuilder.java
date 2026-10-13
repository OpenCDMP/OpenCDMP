package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.elastic.data.nested.NestedPlanBlueprintElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanBlueprintElasticEntity;
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
public class NestedPlanBlueprintElasticBuilder extends BaseElasticBuilder<NestedPlanBlueprintElasticEntity, PlanBlueprintEntity> {

    @Autowired
    public NestedPlanBlueprintElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedPlanBlueprintElasticBuilder.class)));
    }

    @Override
    public List<NestedPlanBlueprintElasticEntity> build(List<PlanBlueprintEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedPlanBlueprintElasticEntity> models = new ArrayList<>();
        for (PlanBlueprintEntity d : data) {
            NestedPlanBlueprintElasticEntity m = new NestedPlanBlueprintElasticEntity();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setGroupId(d.getGroupId());
            m.setVersionStatus(d.getVersionStatus());
            m.setTypeId(d.getTypeId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}