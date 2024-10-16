package org.opencdmp.integrationevent.inbox;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "queue.task.listener.options")
//@ConstructorBinding
public class InboxProperties {

    @NotNull
    private final String exchange;

    private final List<String> annotationCreatedTopic;

    private final List<String> annotationStatusChangedTopic;

    public InboxProperties(
            String exchange, List<String> annotationCreatedTopic, List<String> annotationStatusChangedTopic) {
        this.exchange = exchange;
        this.annotationCreatedTopic = annotationCreatedTopic;
        this.annotationStatusChangedTopic = annotationStatusChangedTopic;
    }

    public String getExchange() {
        return exchange;
    }

    public List<String> getAnnotationCreatedTopic() {
        return annotationCreatedTopic;
    }

    public List<String> getAnnotationStatusChangedTopic() {
        return annotationStatusChangedTopic;
    }
}
