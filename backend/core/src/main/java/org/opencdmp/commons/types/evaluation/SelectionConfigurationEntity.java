package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.evaluatorbase.enums.SuccessStatus;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SelectionConfigurationEntity {

    @XmlElementWrapper(name = "valueSetLists")
    @XmlElement(name = "valueSetList")
    private List<ValueSetEntity> valueSetList;
    public static final String _numberType = "numberType";

    public SelectionConfigurationEntity() {
    }

    public List<ValueSetEntity> getValueSetList() {
        return valueSetList;
    }

    public void setValueSetList(List<ValueSetEntity> valueSetList) {
        this.valueSetList = valueSetList;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ValueSetEntity {

        @XmlElement(name = "key")
        private double key;
        public static final String _key = "key";

        @XmlElement(name = "successStatus")
        private SuccessStatus successStatus;
        public static final String _successStatus = "successStatus";

        public ValueSetEntity() {
        }

        public double getKey() {
            return this.key;
        }

        public void setKey(double key) {
            this.key = key;
        }

        public SuccessStatus getSuccessStatus() {
            return this.successStatus;
        }

        public void setSuccessStatus(SuccessStatus successStatus) {
            this.successStatus = successStatus;
        }
    }
}
