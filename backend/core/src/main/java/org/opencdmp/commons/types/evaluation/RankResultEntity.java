package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class RankResultEntity {

    @XmlElement(name = "rank")
    private double rank;
    public static final String _rank = "rank";

    @XmlElement(name = "details")
    private String details;
    public static final String _details = "details";

    @XmlElementWrapper(name = "results")
    @XmlElement(name = "result")
    private List<EvaluationResultEntity> results;
    public static final String _results = "results";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<EvaluationResultEntity> getResults() {
        return results;
    }

    public void setResults(List<EvaluationResultEntity> results) {
        this.results = results;
    }
}
