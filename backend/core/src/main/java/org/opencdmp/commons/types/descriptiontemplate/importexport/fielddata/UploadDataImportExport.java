package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class UploadDataImportExport extends BaseFieldDataImportExport {
    public static final String XmlElementName = "uploadData";
    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    private List<UploadDataOption> types;

    @XmlElement(name = "maxFileSizeInMB")
    private Integer maxFileSizeInMB;

    public List<UploadDataOption> getTypes() {
        return this.types;
    }

    public void setTypes(List<UploadDataOption> types) {
        this.types = types;
    }

    public Integer getMaxFileSizeInMB() {
        return this.maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UploadDataOption {
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

