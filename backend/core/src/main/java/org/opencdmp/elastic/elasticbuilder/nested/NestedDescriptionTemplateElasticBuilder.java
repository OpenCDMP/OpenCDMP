package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.elastic.data.nested.NestedDescriptionTemplateElasticEntity;
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
public class NestedDescriptionTemplateElasticBuilder extends BaseElasticBuilder<NestedDescriptionTemplateElasticEntity, DescriptionTemplateEntity> {

    @Autowired
    public NestedDescriptionTemplateElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedDescriptionTemplateElasticBuilder.class)));
    }

    @Override
    public List<NestedDescriptionTemplateElasticEntity> build(List<DescriptionTemplateEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedDescriptionTemplateElasticEntity> models = new ArrayList<>();
        for (DescriptionTemplateEntity d : data) {
            NestedDescriptionTemplateElasticEntity m = new NestedDescriptionTemplateElasticEntity();
            m.setId(d.getId());
            m.setVersionStatus(d.getVersionStatus());
            m.setLabel(d.getLabel());
            m.setGroupId(d.getGroupId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
