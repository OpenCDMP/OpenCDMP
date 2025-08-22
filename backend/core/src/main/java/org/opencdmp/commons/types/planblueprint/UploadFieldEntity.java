package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFieldEntity extends FieldEntity {

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UploadOptionEntity {
        @XmlAttribute(name="label")
        private String label;
        @XmlAttribute(name="value")
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    private List<UploadOptionEntity> types;

    @XmlAttribute(name="maxFileSizeInMB")
    private Integer maxFileSizeInMB;


    public List<UploadOptionEntity> getTypes() {
        return types;
    }

    public void setTypes(List<UploadOptionEntity> types) {
        this.types = types;
    }

    public Integer getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }
}
