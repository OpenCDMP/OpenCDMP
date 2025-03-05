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
    private RankModelEntity rankModel;
    public static final String _rankModel = "rankModel";

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

    public RankModelEntity getRankModel() {
        return rankModel;
    }


    public void setRankModel(RankModelEntity rankModel) {
        this.rankModel = rankModel;
    }
}
