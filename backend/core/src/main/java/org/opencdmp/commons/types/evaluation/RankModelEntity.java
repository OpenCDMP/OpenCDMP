package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class RankModelEntity {

    @XmlElement(name = "rank")
    private double rank;
    public static final String _rank = "rank";

    @XmlElement(name = "messages")
    private Map<String, String> messages;
    public static final String _messages = "messages";

    @XmlElement(name = "details")
    private String details;
    public static final String _details = "details";

    public double getRank() {
        return rank;
    }


    public void setRank(double rank) {
        this.rank = rank;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
