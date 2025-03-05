package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.evaluatorbase.enums.NumberType;


@XmlAccessorType(XmlAccessType.FIELD)
public class ValueRangeConfigurationEntity {

    @XmlElement(name = "numberType")
    private NumberType numberType;
    public static final String _numberType = "numberType";

    @XmlElement(name = "min")
    private double min;
    public static final String _min = "min";


    @XmlElement(name = "max")
    private double max;
    public static final String _max = "max";

    @XmlElement(name = "minPassValue")
    private double minPassValue;
    public static final String _minPassValue = "minPassValue";

    public NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMinPassValue() {
        return minPassValue;
    }

    public void setMinPassValue(double minPassValue) {
        this.minPassValue = minPassValue;
    }
}
