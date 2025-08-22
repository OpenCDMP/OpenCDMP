package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationResultMetricEntity {

    @XmlElement(name = "rank")
    private double rank;
    public static final String _rank = "rank";

    @XmlElement(name = "metricTitle")
    private String metricTitle;
    public static final String _metricTitle = "metricTitle";

    @XmlElement(name = "metricDetails")
    private String metricDetails;
    public static final String _metricDetails = "metricDetails";

    @XmlElementWrapper(name = "messages")
    @XmlElement(name = "message")
    private List<EvaluationResultMessageEntity> messages;
    public static final String _messages = "messages";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getMetricTitle() {
        return metricTitle;
    }

    public void setMetricTitle(String metricTitle) {
        this.metricTitle = metricTitle;
    }

    public String getMetricDetails() {
        return metricDetails;
    }

    public void setMetricDetails(String metricDetails) {
        this.metricDetails = metricDetails;
    }

    public List<EvaluationResultMessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<EvaluationResultMessageEntity> messages) {
        this.messages = messages;
    }
}
