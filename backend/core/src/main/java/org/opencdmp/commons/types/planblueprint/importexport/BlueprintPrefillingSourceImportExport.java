package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintPrefillingSourceImportExport {
    @XmlElement(name = "id")
    private UUID id;

    @XmlElement(name = "code")
    private String code;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
