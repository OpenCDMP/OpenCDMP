package org.opencdmp.model.actionconfirmation;

import java.util.UUID;

public class RemoveCredentialRequest {

    private UUID credentialId;
    public static final String _credentialId = "credentialId";

    public UUID getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(UUID credentialId) {
        this.credentialId = credentialId;
    }

}

