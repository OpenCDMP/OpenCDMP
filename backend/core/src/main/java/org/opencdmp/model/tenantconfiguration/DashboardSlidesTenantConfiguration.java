package org.opencdmp.model.tenantconfiguration;

import java.util.List;

public class DashboardSlidesTenantConfiguration {

    private List<DashboardSlide> slides;
    public static final String _slides = "slides";

    private Integer version;
    public static final String _version = "version";

    public List<DashboardSlide> getSlides() {
        return slides;
    }

    public void setSlides(List<DashboardSlide> slides) {
        this.slides = slides;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
