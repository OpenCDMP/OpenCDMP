package org.opencdmp.integrationevent.outbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gr.cite.rabbitmq.IntegrationEvent;
import org.opencdmp.integrationevent.TrackedEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxIntegrationEvent extends IntegrationEvent {

    public static final String FORGET_ME_COMPLETED = "FORGET_ME_COMPLETED";

    public static final String NOTIFY = "NOTIFY";
    public static final String TENANT_DEFAULT_LOCALE_REMOVAL = "TENANT_DEFAULT_LOCALE_REMOVAL";
    public static final String TENANT_DEFAULT_LOCALE_TOUCHED = "TENANT_DEFAULT_LOCALE_TOUCHED";

    public static final String TENANT_REACTIVATE = "TENANT_REACTIVATE";

    public static final String TENANT_REMOVE = "TENANT_REMOVE";

    public static final String TENANT_TOUCH = "TENANT_TOUCH";

    public static final String TENANT_USER_INVITE = "TENANT_USER_INVITE";

    public static final String USER_TOUCH = "USER_TOUCH";

    public static final String USER_REMOVE = "USER_REMOVE";

    public static final String ANNOTATION_ENTITY_TOUCH = "ANNOTATION_ENTITY_TOUCH";
    public static final String ANNOTATION_ENTITY_REMOVE = "ANNOTATION_ENTITY_REMOVE";

    public static final String WHAT_YOU_KNOW_ABOUT_ME_COMPLETED = "WHAT_YOU_KNOW_ABOUT_ME_COMPLETED";

    public static final String ACCOUNTING_ENTRY_CREATED = "ACCOUNTING_ENTRY_CREATED";

    public static final String GENERATE_FILE = "GENERATE_FILE";

    public static final String INDICATOR_POINT_ENTRY = "INDICATOR_POINT_ENTRY";

    public static final String INDICATOR_ACCESS_ENTRY = "INDICATOR_ACCESS_ENTRY";

    public static final String INDICATOR_ENTRY = "INDICATOR_ENTRY";

    public static final String INDICATOR_RESET_ENTRY = "INDICATOR_RESET_ENTRY";

    private TrackedEvent event;

    public TrackedEvent getEvent() {
        return this.event;
    }

    public void setEvent(TrackedEvent event) {
        this.event = event;
    }
}
