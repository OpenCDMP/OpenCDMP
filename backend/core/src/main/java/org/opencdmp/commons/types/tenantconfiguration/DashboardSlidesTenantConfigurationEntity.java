package org.opencdmp.commons.types.tenantconfiguration;

import java.util.List;

public class DashboardSlidesTenantConfigurationEntity {

    private List<DashboardSlideEntity> slides;

    private Integer version;

    public List<DashboardSlideEntity> getSlides() {
        return slides;
    }

    public void setSlides(List<DashboardSlideEntity> slides) {
        this.slides = slides;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
