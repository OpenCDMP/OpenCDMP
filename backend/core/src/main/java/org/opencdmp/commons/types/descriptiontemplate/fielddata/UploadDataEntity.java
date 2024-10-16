package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class UploadDataEntity extends BaseFieldDataEntity {
    public static final String XmlElementName = "uploadData";

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UploadDataOptionEntity {
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
    private List<UploadDataOptionEntity> types;

    @XmlAttribute(name="maxFileSizeInMB")
    private Integer maxFileSizeInMB;


    public List<UploadDataOptionEntity> getTypes() {
        return types;
    }

    public void setTypes(List<UploadDataOptionEntity> types) {
        this.types = types;
    }

    public Integer getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }
}
