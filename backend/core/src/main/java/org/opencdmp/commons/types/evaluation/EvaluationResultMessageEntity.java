package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationResultMessageEntity {

    @XmlElement(name = "title")
    private String title;
    public static final String _title = "title";

    @XmlElement(name = "message")
    private String message;
    public static final String _message = "message";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
