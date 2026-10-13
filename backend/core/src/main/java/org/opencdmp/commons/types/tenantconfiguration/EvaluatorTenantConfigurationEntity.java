package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;

import java.util.List;

public class EvaluatorTenantConfigurationEntity {

    private List<EvaluatorSourceEntity> sources;
    private boolean disableSystemSources;
    /** Sources (installation-provided or the tenant's own) switched off for this tenant, by plugin id. */
    private List<String> disabledSourceIds;

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
    public List<String> getDisabledSourceIds() {
        return disabledSourceIds;
    }

    public void setDisabledSourceIds(List<String> disabledSourceIds) {
        this.disabledSourceIds = disabledSourceIds;
    }
}
