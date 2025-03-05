package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;

import java.util.List;

public class EvaluatorTenantConfigurationEntity {

    private List<EvaluatorSourceEntity> sources;
    private boolean disableSystemSources;

    public List<EvaluatorSourceEntity> getSources() {
        return sources;
    }

    public void setSources(List<EvaluatorSourceEntity> sources) {
        this.sources = sources;
    }

    public boolean getDisableSystemSources(){
        return disableSystemSources;
    }
    public void setDisableSystemSources(boolean disableSystemSources) {
        this.disableSystemSources = disableSystemSources;
    }
}
