package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanDescriptionTemplateEntity;
import org.opencdmp.elastic.data.nested.NestedPlanDescriptionTemplateElasticEntity;
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
public class NestedPlanDescriptionTemplateElasticBuilder extends BaseElasticBuilder<NestedPlanDescriptionTemplateElasticEntity, PlanDescriptionTemplateEntity> {

    @Autowired
    public NestedPlanDescriptionTemplateElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedPlanDescriptionTemplateElasticBuilder.class)));
    }

    @Override
    public List<NestedPlanDescriptionTemplateElasticEntity> build(List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedPlanDescriptionTemplateElasticEntity> models = new ArrayList<>();
        for (PlanDescriptionTemplateEntity d : data) {
            NestedPlanDescriptionTemplateElasticEntity m = new NestedPlanDescriptionTemplateElasticEntity();
            m.setId(d.getId());
            m.setPlanId(d.getPlanId());
            m.setSectionId(d.getSectionId());
            m.setDescriptionTemplateGroupId(d.getDescriptionTemplateGroupId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
