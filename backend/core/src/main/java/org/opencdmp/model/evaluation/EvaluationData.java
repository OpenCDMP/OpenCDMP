package org.opencdmp.model.evaluation;

import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.evaluatorbase.models.misc.RankModel;

public class EvaluationData {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private RankConfig rankConfig;
    public static final String _rankConfig = "rankConfig";

    private RankModel rankModel;
    public static final String _rankModel = "rankModel";

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

    public RankModel getRankModel() {
        return rankModel;
    }

    public void setRankModel(RankModel rankModel) {
        this.rankModel = rankModel;
    }
}
