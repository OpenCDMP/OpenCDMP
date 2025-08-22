package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.*;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationResultEntity {

    @XmlElement(name = "rank")
    private double rank;
    public static final String _rank = "rank";

    @XmlElement(name = "benchmarkTitle")
    private String benchmarkTitle;
    public static final String _benchmarkTitle = "benchmarkTitle";

    @XmlElement(name = "benchmarkDetails")
    private String benchmarkDetails;
    public static final String _benchmarkDetails = "benchmarkDetails";

    @XmlElementWrapper(name = "metrics")
    @XmlElement(name = "metric")
    private List<EvaluationResultMetricEntity> metrics;
    public static final String _metrics = "metrics";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    public String getBenchmarkDetails() {
        return benchmarkDetails;
    }

    public void setBenchmarkDetails(String benchmarkDetails) {
        this.benchmarkDetails = benchmarkDetails;
    }

    public List<EvaluationResultMetricEntity> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<EvaluationResultMetricEntity> metrics) {
        this.metrics = metrics;
    }
}
