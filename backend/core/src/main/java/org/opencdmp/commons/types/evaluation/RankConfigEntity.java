package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.evaluatorbase.enums.RankType;

@XmlAccessorType(XmlAccessType.FIELD)
public class RankConfigEntity {

    @XmlElement(name = "rankType")
    private RankType rankType;
    public static final String _rankType = "rankType";

    @XmlElement(name = "valueRangeConfiguration")
    private ValueRangeConfigurationEntity valueRangeConfiguration;
    public static final String _valueRangeConfiguration = "valueRangeConfiguration";

    @XmlElement(name = "selectionConfiguration")
    private SelectionConfigurationEntity selectionConfiguration;
    public static final String _selectionConfiguration = "selectionConfiguration";

    public RankType getRankType() {
        return rankType;
    }

    public void setRankType(RankType rankType) {
        this.rankType = rankType;
    }

    public ValueRangeConfigurationEntity getValueRangeConfiguration() {
        return valueRangeConfiguration;
    }

    public void setValueRangeConfiguration(ValueRangeConfigurationEntity valueRangeConfiguration) {
        this.valueRangeConfiguration = valueRangeConfiguration;
    }

    public SelectionConfigurationEntity getSelectionConfiguration() {
        return selectionConfiguration;
    }

    public void setSelectionConfiguration(SelectionConfigurationEntity selectionConfiguration) {
        this.selectionConfiguration = selectionConfiguration;
    }
}
