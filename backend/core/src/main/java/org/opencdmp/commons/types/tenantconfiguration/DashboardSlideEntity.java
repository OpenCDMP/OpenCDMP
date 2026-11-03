package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.enums.DashboardSlideButtonType;
import org.opencdmp.commons.enums.DashboardSlideImageFit;
import org.opencdmp.commons.enums.DashboardSlideKind;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DashboardSlideEntity {

    private UUID id;

    private DashboardSlideKind kind;

    private Boolean enabled;

    private LocalDate showFrom;

    private LocalDate showUntil;

    private UUID storageFileId;

    private DashboardSlideImageFit imageFit;

    private DashboardSlideButtonType buttonType;

    private String buttonTarget;

    private List<DashboardSlideTranslationEntity> translations;

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

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
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

    public List<DashboardSlideTranslationEntity> getTranslations() {
        return translations;
    }

    public void setTranslations(List<DashboardSlideTranslationEntity> translations) {
        this.translations = translations;
    }
}
