package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;

import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintSystemFieldImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "type")
    private PlanBlueprintSystemFieldType type;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "placeholder")
    private String placeholder;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name="semantics")
    private List<String> semantics;
    @XmlAttribute(name = "ordinal")
    private int ordinal;
    @XmlAttribute(name = "required")
    private boolean required;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlanBlueprintSystemFieldType getType() {
        return this.type;
    }

    public void setType(PlanBlueprintSystemFieldType type) {
        this.type = type;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<String> getSemantics() {
        return this.semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }
}
