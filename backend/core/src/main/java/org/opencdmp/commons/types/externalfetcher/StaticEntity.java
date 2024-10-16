package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.Static;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

public class StaticEntity implements Static {

    private List<StaticOptionEntity> options;

    public List<StaticOptionEntity> getOptions() {
        return options;
    }

    @XmlElementWrapper
    @XmlElement(name = "option")
    public void setOptions(List<StaticOptionEntity> options) {
        this.options = options;
    }
}
