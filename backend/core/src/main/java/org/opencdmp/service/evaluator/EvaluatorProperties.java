package org.opencdmp.service.evaluator;

import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "evaluator")
public class EvaluatorProperties {

    private List<EvaluatorSourceEntity> sources;

    public List<EvaluatorSourceEntity> getSources() {
        return sources;
    }

    public void setSources(List<EvaluatorSourceEntity> sources) {
        this.sources = sources;
    }
}

