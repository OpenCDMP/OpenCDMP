package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.PageEntity;

public class Page implements Comparable<Object> {
    private String id;
    private Integer ordinal;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PageEntity toDatabaseDefinition(PageEntity item) {
        item.setId(this.id);
        item.setOrdinal(this.ordinal);
        item.setTitle(this.title);
        return item;
    }

    public void fromDatabaseDefinition(PageEntity item) {
        this.title = item.getTitle();
        this.ordinal = item.getOrdinal();
        this.id = item.getId();
    }

    @Override
    public int compareTo(Object o) {
        return this.ordinal.compareTo((Integer) o);
    }
}
