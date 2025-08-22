package org.opencdmp.model.evaluator;

import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.evaluatorbase.models.misc.RankConfig;

import java.util.List;

public class EvaluatorConfiguration {
    private String evaluatorId;
    private RankConfig rankConfig;
    private List<PluginEntityType> evaluatorEntityTypes;
    private boolean useSharedStorage;
    private boolean hasLogo;

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public RankConfig getRankConfig() {
        return rankConfig;
    }

    public void setRankConfig(RankConfig rankConfig) {
        this.rankConfig = rankConfig;
    }

    public List<PluginEntityType> getEvaluatorEntityTypes() {
        return evaluatorEntityTypes;
    }

    public void setEvaluatorEntityTypes(List<PluginEntityType> evaluatorEntityTypes) {
        this.evaluatorEntityTypes = evaluatorEntityTypes;
    }

    public boolean isUseSharedStorage() {
        return useSharedStorage;
    }

    public void setUseSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }

    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }
}
