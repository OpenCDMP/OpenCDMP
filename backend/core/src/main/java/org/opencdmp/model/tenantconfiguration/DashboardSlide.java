package org.opencdmp.model.tenantconfiguration;

import org.opencdmp.commons.enums.DashboardSlideButtonType;
import org.opencdmp.commons.enums.DashboardSlideImageFit;
import org.opencdmp.commons.enums.DashboardSlideKind;
import org.opencdmp.model.StorageFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DashboardSlide {

    private UUID id;
    public static final String _id = "id";

    private DashboardSlideKind kind;
    public static final String _kind = "kind";

    private Boolean enabled;
    public static final String _enabled = "enabled";

    private LocalDate showFrom;
    public static final String _showFrom = "showFrom";

    private LocalDate showUntil;
    public static final String _showUntil = "showUntil";

    private StorageFile storageFile;
    public static final String _storageFile = "storageFile";

    private DashboardSlideImageFit imageFit;
    public static final String _imageFit = "imageFit";

    private DashboardSlideButtonType buttonType;
    public static final String _buttonType = "buttonType";

    private String buttonTarget;
    public static final String _buttonTarget = "buttonTarget";

    private List<DashboardSlideTranslation> translations;
    public static final String _translations = "translations";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DashboardSlideKind getKind() {
        return kind;
    }

    public void setKind(DashboardSlideKind kind) {
        this.kind = kind;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDate getShowFrom() {
        return showFrom;
    }

    public void setShowFrom(LocalDate showFrom) {
        this.showFrom = showFrom;
    }

    public LocalDate getShowUntil() {
        return showUntil;
    }

    public void setShowUntil(LocalDate showUntil) {
        this.showUntil = showUntil;
    }

    public StorageFile getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFile storageFile) {
        this.storageFile = storageFile;
    }

    public DashboardSlideImageFit getImageFit() {
        return imageFit;
    }

    public void setImageFit(DashboardSlideImageFit imageFit) {
        this.imageFit = imageFit;
    }

    public DashboardSlideButtonType getButtonType() {
        return buttonType;
    }

    public void setButtonType(DashboardSlideButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public String getButtonTarget() {
        return buttonTarget;
    }

    public void setButtonTarget(String buttonTarget) {
        this.buttonTarget = buttonTarget;
    }

    public List<DashboardSlideTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<DashboardSlideTranslation> translations) {
        this.translations = translations;
    }
}
