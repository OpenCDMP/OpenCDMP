package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.SourceStaticOptionConfiguration;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

public class ExternalFetcherStaticOptionSourceConfigurationEntity extends ExternalFetcherBaseSourceConfigurationEntity implements SourceStaticOptionConfiguration<StaticEntity> {

    List<StaticEntity> items;

    public List<StaticEntity> getItems() {
        return items;
    }

    @XmlElementWrapper
    @XmlElement(name = "item")
    public void setItems(List<StaticEntity> options) {
        this.items = options;
    }
}
