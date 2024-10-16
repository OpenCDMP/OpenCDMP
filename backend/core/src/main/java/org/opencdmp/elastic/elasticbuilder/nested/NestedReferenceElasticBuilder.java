package org.opencdmp.elastic.elasticbuilder.nested;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedReferenceElasticBuilder extends BaseElasticBuilder<NestedReferenceElasticEntity, ReferenceEntity> {

    @Autowired
    public NestedReferenceElasticBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedReferenceElasticBuilder.class)));
    }

    @Override
    public List<NestedReferenceElasticEntity> build(List<ReferenceEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedReferenceElasticEntity> models = new ArrayList<>();
        for (ReferenceEntity d : data) {
            NestedReferenceElasticEntity m = new NestedReferenceElasticEntity();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
