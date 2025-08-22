package org.opencdmp.commons.types.plan.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opencdmp.commons.types.storagefile.importexport.StorageFileImportExport;
import org.opencdmp.commons.xmladapter.InstantXmlAdapter;

import java.time.Instant;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)

public class PlanBlueprintValueImportExport {

    @XmlElement(name = "fieldId")
    private UUID fieldId;

    @XmlElement(name="storageFile")
    private StorageFileImportExport storageFile;

    @XmlElement(name = "value")
    private String value;

    @XmlElement(name = "dateValue")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant dateValue;

    @XmlElement(name = "numberValue")
    private Double numberValue;

    public UUID getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public StorageFileImportExport getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFileImportExport storageFile) {
        this.storageFile = storageFile;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
    }
}
