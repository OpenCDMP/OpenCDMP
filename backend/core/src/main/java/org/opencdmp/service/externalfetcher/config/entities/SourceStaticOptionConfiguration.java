package org.opencdmp.service.externalfetcher.config.entities;

import java.util.List;

public interface SourceStaticOptionConfiguration <Item extends Static> extends  SourceBaseConfiguration {
    List<Item> getItems();
}
