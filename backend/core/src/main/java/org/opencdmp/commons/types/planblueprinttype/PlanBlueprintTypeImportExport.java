package org.opencdmp.commons.types.planblueprinttype;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.UUID;

@XmlRootElement(name = "planBlueprintType")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanBlueprintTypeImportExport {

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
