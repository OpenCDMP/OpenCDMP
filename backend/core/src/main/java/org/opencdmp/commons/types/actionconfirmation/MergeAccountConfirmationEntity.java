package org.opencdmp.commons.types.actionconfirmation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "merge-account-confirmation")
@XmlAccessorType(XmlAccessType.FIELD)
public class MergeAccountConfirmationEntity {

    @XmlAttribute(name = "email")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
