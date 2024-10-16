package org.opencdmp.elastic.elasticbuilder.nested;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.elastic.data.nested.NestedDoiElasticEntity;
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
public class NestedDoiElasticBuilder extends BaseElasticBuilder<NestedDoiElasticEntity, EntityDoiEntity> {

    @Autowired
    public NestedDoiElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedDoiElasticBuilder.class)));
    }

    @Override
    public List<NestedDoiElasticEntity> build(List<EntityDoiEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedDoiElasticEntity> models = new ArrayList<>();
        for (EntityDoiEntity d : data) {
            NestedDoiElasticEntity m = new NestedDoiElasticEntity();
            m.setId(d.getId());
            m.setDoi(d.getDoi());
            m.setRepositoryId(d.getRepositoryId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
