package org.opencdmp.model.evaluation;

import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.evaluatorbase.models.misc.RankResultModel;

import java.util.List;

public class EvaluationData {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private RankConfig rankConfig;
    public static final String _rankConfig = "rankConfig";

    private List<RankResultModel> rankResult;
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

    public List<RankResultModel> getRankResult() {
        return rankResult;
    }

    public void setRankResult(List<RankResultModel> rankResult) {
        this.rankResult = rankResult;
    }
}
