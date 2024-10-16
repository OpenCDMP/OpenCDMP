package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionReferenceTypeImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "code")
    private String code;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

