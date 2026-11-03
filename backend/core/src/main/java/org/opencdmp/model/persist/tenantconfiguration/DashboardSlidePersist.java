package org.opencdmp.model.persist.tenantconfiguration;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DashboardSlideButtonType;
import org.opencdmp.commons.enums.DashboardSlideImageFit;
import org.opencdmp.commons.enums.DashboardSlideKind;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DashboardSlidePersist {

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

    private UUID storageFileId;
    public static final String _storageFileId = "storageFileId";

    private DashboardSlideImageFit imageFit;
    public static final String _imageFit = "imageFit";

    private DashboardSlideButtonType buttonType;
    public static final String _buttonType = "buttonType";

    private String buttonTarget;
    public static final String _buttonTarget = "buttonTarget";

    private List<DashboardSlideTranslationPersist> translations;
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

    public List<DashboardSlideTranslationPersist> getTranslations() {
        return translations;
    }

    public void setTranslations(List<DashboardSlideTranslationPersist> translations) {
        this.translations = translations;
    }

    @Component(DashboardSlidePersist.DashboardSlidePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DashboardSlidePersistValidator extends BaseValidator<DashboardSlidePersist> {

        public static final String ValidatorName = "DashboardSlidePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected DashboardSlidePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DashboardSlidePersist> modelClass() {
            return DashboardSlidePersist.class;
        }

        @Override
        protected List<Specification> specifications(DashboardSlidePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getKind()))
                            .failOn(DashboardSlidePersist._kind).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlidePersist._kind}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getEnabled()))
                            .failOn(DashboardSlidePersist._enabled).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlidePersist._enabled}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> DashboardSlideKind.Content.equals(item.getKind()))
                            .must(() -> !this.isListNullOrEmpty(item.getTranslations()))
                            .failOn(DashboardSlidePersist._translations).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlidePersist._translations}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getShowFrom() != null && item.getShowUntil() != null)
                            .must(() -> !item.getShowUntil().isBefore(item.getShowFrom()))
                            .failOn(DashboardSlidePersist._showUntil).failWith(messageSource.getMessage("Validation.LargerThenEqual", new Object[]{DashboardSlidePersist._showUntil, DashboardSlidePersist._showFrom}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getButtonType()))
                            .must(() -> !this.isEmpty(item.getButtonTarget()))
                            .failOn(DashboardSlidePersist._buttonTarget).failWith(messageSource.getMessage("Validation_Required", new Object[]{DashboardSlidePersist._buttonTarget}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> DashboardSlideButtonType.Email.equals(item.getButtonType()) && !this.isEmpty(item.getButtonTarget()))
                            .must(() -> this.isValidEmail(item.getButtonTarget()))
                            .failOn(DashboardSlidePersist._buttonTarget).failWith(messageSource.getMessage("Validation_EmailRequired", new Object[]{DashboardSlidePersist._buttonTarget}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getTranslations()))
                            .on(DashboardSlidePersist._translations)
                            .over(item.getTranslations())
                            .using((itm) -> this.validatorFactory.validator(DashboardSlideTranslationPersist.DashboardSlideTranslationPersistValidator.class))
            );
        }
    }
}
