package org.opencdmp.commons.types.actionconfirmation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.UUID;

@XmlRootElement(name = "remove-credential-confirmation")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoveCredentialRequestEntity {

    @XmlAttribute(name = "credential-id")
    private UUID credentialId;

    public UUID getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(UUID credentialId) {
        this.credentialId = credentialId;
    }
}
