package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationDataEntity {

    @XmlElement(name = "evaluatorId")
    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    @XmlElement(name = "rankConfig")
    private RankConfigEntity rankConfig;
    public static final String _rankConfig = "rankConfig";

    @XmlElement(name = "rankModel")
    private RankResultEntity rankResult;
    public static final String _rankResult = "rankResult";

    public String getEvaluatorId() {
        return evaluatorId;
    }


    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public RankConfigEntity getRankConfig() {
        return rankConfig;
    }

    public void setRankConfig(RankConfigEntity evaluatorConfig) {
        this.rankConfig = evaluatorConfig;
    }

    public RankResultEntity getRankResult() {
        return rankResult;
    }


    public void setRankResult(RankResultEntity rankResult) {
        this.rankResult = rankResult;
    }
}
