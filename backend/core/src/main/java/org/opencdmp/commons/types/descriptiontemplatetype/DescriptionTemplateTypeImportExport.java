package org.opencdmp.commons.types.descriptiontemplatetype;


import jakarta.xml.bind.annotation.*;

import java.util.UUID;

@XmlRootElement(name = "descriptionTemplateType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateTypeImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "code")
    private String code;
    @XmlElement(name = "name")
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
