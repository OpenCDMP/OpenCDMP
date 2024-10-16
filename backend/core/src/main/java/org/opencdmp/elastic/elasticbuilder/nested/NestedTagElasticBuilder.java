package org.opencdmp.elastic.elasticbuilder.nested;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TagEntity;
import org.opencdmp.elastic.data.nested.NestedTagElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedTagElasticBuilder extends BaseElasticBuilder<NestedTagElasticEntity, TagEntity> {

    @Autowired
    public NestedTagElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedTagElasticBuilder.class)));
    }

    @Override
    public List<NestedTagElasticEntity> build(List<TagEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedTagElasticEntity> models = new ArrayList<>();
        for (TagEntity d : data) {
            NestedTagElasticEntity m = new NestedTagElasticEntity();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
