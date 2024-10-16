package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MultiplicityEntity {

    @XmlAttribute(name="min")
    private Integer min;
    @XmlAttribute(name="max")
    private Integer max;
    @XmlAttribute(name="placeholder")
    private String placeholder;
    @XmlAttribute(name="tableView")
    private boolean tableView;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean getTableView() {
        return tableView;
    }

    public void setTableView(boolean tableView) {
        this.tableView = tableView;
    }
}
