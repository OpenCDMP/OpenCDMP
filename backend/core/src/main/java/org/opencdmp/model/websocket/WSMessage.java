package org.opencdmp.model.websocket;

import org.opencdmp.commons.enums.WSActionType;
import org.opencdmp.model.user.User;

public class WSMessage<P> {

    private User producer;

    private WSActionType actionType;

    private P payload;

    public WSMessage() {
    }

    public WSMessage(User producer, WSActionType actionType, P payload) {
        this.producer = producer;
        this.actionType = actionType;
        this.payload = payload;
    }

    public User getProducer() {
        return producer;
    }

    public void setProducer(User producer) {
        this.producer = producer;
    }

    public WSActionType getActionType() {
        return actionType;
    }

    public void setActionType(WSActionType actionType) {
        this.actionType = actionType;
    }

    public P getPayload() {
        return payload;
    }

    public void setPayload(P payload) {
        this.payload = payload;
    }
}
