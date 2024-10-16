package org.opencdmp.model.externalfetcher;


import java.util.List;

public class ExternalFetcherStaticOptionSourceConfiguration extends ExternalFetcherBaseSourceConfiguration {

    public final static String _items = "items";
    List<Static> items;

    public List<Static> getItems() {
        return items;
    }

    public void setItems(List<Static> items) {
        this.items = items;
    }
}
