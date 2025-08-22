package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintUploadFieldImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    private List<UploadOptionImportExport> types;
    @XmlElement(name = "maxFileSizeInMB")
    private Integer maxFileSizeInMB;
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
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<UploadOptionImportExport> getTypes() {
        return types;
    }

    public void setTypes(List<UploadOptionImportExport> types) {
        this.types = types;
    }

    public Integer getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSemantics() {
        return semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UploadOptionImportExport {
        @XmlAttribute(name = "label")
        private String label;
        @XmlAttribute(name = "value")
        private String value;
    
        public String getLabel() {
            return this.label;
        }
    
        public void setLabel(String label) {
            this.label = label;
        }
    
        public String getValue() {
            return this.value;
        }
    
        public void setValue(String value) {
            this.value = value;
        }
    }
}

