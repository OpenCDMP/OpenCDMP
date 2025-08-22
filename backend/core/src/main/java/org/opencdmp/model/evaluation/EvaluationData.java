package org.opencdmp.model.evaluation;

import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.evaluatorbase.models.misc.RankResultModel;

public class EvaluationData {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private RankConfig rankConfig;
    public static final String _rankConfig = "rankConfig";

    private RankResultModel rankResult;
    public static final String _rankResult = "rankResult";

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

    public RankResultModel getRankResult() {
        return rankResult;
    }

    public void setRankResult(RankResultModel rankResult) {
        this.rankResult = rankResult;
    }
}
