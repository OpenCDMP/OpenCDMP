package org.opencdmp.model.publicapi.funder;

import java.util.UUID;

public class FunderPublicOverviewModel {
    private UUID id;
    private String label;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getHint() {
        return null;
    }
}
