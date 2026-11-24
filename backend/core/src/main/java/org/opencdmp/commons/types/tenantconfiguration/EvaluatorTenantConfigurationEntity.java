package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;

import java.util.List;

public class EvaluatorTenantConfigurationEntity {

    private List<EvaluatorSourceEntity> sources;
    private boolean disableSystemSources;
    /** Sources (installation-provided or the tenant's own) switched off for this tenant, by plugin id. */
    private List<String> disabledSourceIds;
    /** Evaluators limited to some of their benchmarks and tests for this tenant; an evaluator without an entry offers all. */
    private List<EvaluatorRestrictionEntity> restrictions;

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

    public List<EvaluatorRestrictionEntity> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<EvaluatorRestrictionEntity> restrictions) {
        this.restrictions = restrictions;
    }
}
