package org.opencdmp.commons.types.descriptiontemplate.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateMultiplicityImportExport {
    @XmlAttribute(name="min")
    private Integer min;
    @XmlAttribute(name="max")
    private Integer max;
    @XmlElement(name="placeholder")
    private String placeholder;
    @XmlElement(name="tableView")
    private boolean tableView;
    @XmlElement(name="transposedTableView")
    private boolean transposedTableView;

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
        return this.placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean getTableView() {
        return this.tableView;
    }

    public void setTableView(boolean tableView) {
        this.tableView = tableView;
    }

    public boolean isTransposedTableView() {
        return transposedTableView;
    }

    public void setTransposedTableView(boolean transposedTableView) {
        this.transposedTableView = transposedTableView;
    }
}