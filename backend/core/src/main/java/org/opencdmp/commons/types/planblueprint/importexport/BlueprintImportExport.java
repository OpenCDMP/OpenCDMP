package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.UUID;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintImportExport {

    @XmlElement(name = "id")
    private UUID id;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "code")
    private String code;

    @XmlElement(name = "definition")
    private BlueprintDefinitionImportExport planBlueprintDefinition;

    @XmlElement(name = "groupId")
    private UUID groupId;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BlueprintDefinitionImportExport getPlanBlueprintDefinition() {
        return this.planBlueprintDefinition;
    }

    public void setPlanBlueprintDefinition(BlueprintDefinitionImportExport planBlueprintDefinition) {
        this.planBlueprintDefinition = planBlueprintDefinition;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
}
