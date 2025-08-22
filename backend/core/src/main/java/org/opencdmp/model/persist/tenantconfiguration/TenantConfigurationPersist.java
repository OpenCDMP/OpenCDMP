package org.opencdmp.model.persist.tenantconfiguration;

import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TenantConfigurationPersist {

    private UUID id;

    public static final String _id = "id";

    private TenantConfigurationType type;

    public static final String _type = "type";

    private CssColorsTenantConfigurationPersist cssColors;

    public static final String _cssColors = "cssColors";

    private DefaultUserLocaleTenantConfigurationPersist defaultUserLocale;

    public static final String _defaultUserLocale = "defaultUserLocale";

    private DepositTenantConfigurationPersist depositPlugins;

    public static final String _depositPlugins = "depositPlugins";

    private EvaluatorTenantConfigurationPersist evaluatorPlugins;

    public static final String _evaluatorPlugins = "evaluatorPlugins";

    private FileTransformerTenantConfigurationPersist fileTransformerPlugins;

    public static final String _fileTransformerPlugins = "fileTransformerPlugins";

    private LogoTenantConfigurationPersist logo;

    public static final String _logo = "logo";

    private PluginTenantConfigurationPersist pluginConfiguration;

    public static final String _pluginConfiguration = "pluginConfiguration";

    private FeaturedEntitiesPersist featuredEntities;

    public static final String _featuredEntities = "featuredEntities";

    private DefaultPlanBlueprintConfigurationPersist defaultPlanBlueprint;

    public static final String _defaultPlanBlueprint = "defaultPlanBlueprint";

    private ViewPreferencesConfigurationPersist viewPreferences;

    public static final String _viewPreferences = "viewPreferences";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CssColorsTenantConfigurationPersist getCssColors() {
        return cssColors;
    }

    public void setCssColors(CssColorsTenantConfigurationPersist cssColors) {
        this.cssColors = cssColors;
    }

    public DefaultUserLocaleTenantConfigurationPersist getDefaultUserLocale() {
        return defaultUserLocale;
    }

    public void setDefaultUserLocale(DefaultUserLocaleTenantConfigurationPersist defaultUserLocale) {
        this.defaultUserLocale = defaultUserLocale;
    }

    public DepositTenantConfigurationPersist getDepositPlugins() {
        return depositPlugins;
    }

    public void setDepositPlugins(DepositTenantConfigurationPersist depositPlugins) {
        this.depositPlugins = depositPlugins;
    }

    public EvaluatorTenantConfigurationPersist getEvaluatorPlugins() {
        return evaluatorPlugins;
    }

    public void setEvaluatorPlugins(EvaluatorTenantConfigurationPersist evaluatorPlugins) {
        this.evaluatorPlugins = evaluatorPlugins;
    }

    public FileTransformerTenantConfigurationPersist getFileTransformerPlugins() {
        return fileTransformerPlugins;
    }

    public void setFileTransformerPlugins(FileTransformerTenantConfigurationPersist fileTransformerPlugins) {
        this.fileTransformerPlugins = fileTransformerPlugins;
    }

    public LogoTenantConfigurationPersist getLogo() {
        return logo;
    }

    public void setLogo(LogoTenantConfigurationPersist logo) {
        this.logo = logo;
    }

    public PluginTenantConfigurationPersist getPluginConfiguration() {
        return pluginConfiguration;
    }

    public void setPluginConfiguration(PluginTenantConfigurationPersist pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }

    public FeaturedEntitiesPersist getFeaturedEntities() {
        return featuredEntities;
    }

    public void setFeaturedEntities(FeaturedEntitiesPersist featuredEntities) {
        this.featuredEntities = featuredEntities;
    }

    public DefaultPlanBlueprintConfigurationPersist getDefaultPlanBlueprint() {
        return defaultPlanBlueprint;
    }

    public void setDefaultPlanBlueprint(DefaultPlanBlueprintConfigurationPersist defaultPlanBlueprint) {
        this.defaultPlanBlueprint = defaultPlanBlueprint;
    }

    public ViewPreferencesConfigurationPersist getViewPreferences() {
        return viewPreferences;
    }

    public void setViewPreferences(ViewPreferencesConfigurationPersist viewPreferences) {
        this.viewPreferences = viewPreferences;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public TenantConfigurationType getType() {
        return type;
    }

    public void setType(TenantConfigurationType type) {
        this.type = type;
    }



    @Component(TenantConfigurationPersist.TenantConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class TenantConfigurationPersistValidator extends BaseValidator<TenantConfigurationPersist> {

        public static final String ValidatorName = "TenantConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected TenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<TenantConfigurationPersist> modelClass() {
            return TenantConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(TenantConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(TenantConfigurationPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(TenantConfigurationPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(TenantConfigurationPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getType()) && TenantConfigurationType.CssColors.equals(item.getType()))
                            .must(() -> !this.isNull(item.getCssColors()))
                            .failOn(TenantConfigurationPersist._cssColors).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._cssColors}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getType()) && TenantConfigurationType.DefaultUserLocale.equals(item.getType()))
                            .must(() -> !this.isNull(item.getDefaultUserLocale()))
                            .failOn(TenantConfigurationPersist._defaultUserLocale).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._defaultUserLocale}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getType()) && TenantConfigurationType.DepositPlugins.equals(item.getType()))
                            .must(() -> !this.isNull(item.getDepositPlugins()))
                            .failOn(TenantConfigurationPersist._depositPlugins).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._depositPlugins}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getType()) && TenantConfigurationType.FileTransformerPlugins.equals(item.getType()))
                            .must(() -> !this.isNull(item.getFileTransformerPlugins()))
                            .failOn(TenantConfigurationPersist._fileTransformerPlugins).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._fileTransformerPlugins}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getType()) && TenantConfigurationType.Logo.equals(item.getType()))
                            .must(() -> !this.isNull(item.getLogo()))
                            .failOn(TenantConfigurationPersist._logo).failWith(messageSource.getMessage("Validation_Required", new Object[]{TenantConfigurationPersist._logo}, LocaleContextHolder.getLocale())),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getCssColors()) && TenantConfigurationType.CssColors.equals(item.getType()))
                            .on(TenantConfigurationPersist._cssColors)
                            .over(item.getCssColors())
                            .using(() -> this.validatorFactory.validator(CssColorsTenantConfigurationPersist.CssColorsTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefaultUserLocale()) && TenantConfigurationType.DefaultUserLocale.equals(item.getType()))
                            .on(TenantConfigurationPersist._defaultUserLocale)
                            .over(item.getDefaultUserLocale())
                            .using(() -> this.validatorFactory.validator(DefaultUserLocaleTenantConfigurationPersist.DefaultUserLocaleTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDepositPlugins()) && TenantConfigurationType.DepositPlugins.equals(item.getType()))
                            .on(TenantConfigurationPersist._depositPlugins)
                            .over(item.getDepositPlugins())
                            .using(() -> this.validatorFactory.validator(DepositTenantConfigurationPersist.DepositTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getEvaluatorPlugins()) && TenantConfigurationType.EvaluatorPlugins.equals(item.getType()))
                            .on(TenantConfigurationPersist._evaluatorPlugins)
                            .over(item.getDepositPlugins())
                            .using(() -> this.validatorFactory.validator(EvaluatorTenantConfigurationPersist.EvaluatorTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getFileTransformerPlugins()) && TenantConfigurationType.FileTransformerPlugins.equals(item.getType()))
                            .on(TenantConfigurationPersist._fileTransformerPlugins)
                            .over(item.getFileTransformerPlugins())
                            .using(() -> this.validatorFactory.validator(FileTransformerTenantConfigurationPersist.FileTransformerTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getLogo()) && TenantConfigurationType.Logo.equals(item.getType()))
                            .on(TenantConfigurationPersist._logo)
                            .over(item.getLogo())
                            .using(() -> this.validatorFactory.validator(LogoTenantConfigurationPersist.LogoTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getPluginConfiguration()) && TenantConfigurationType.PluginConfiguration.equals(item.getType()))
                            .on(TenantConfigurationPersist._pluginConfiguration)
                            .over(item.getCssColors())
                            .using(() -> this.validatorFactory.validator(PluginTenantConfigurationPersist.PluginTenantConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getFeaturedEntities()) && TenantConfigurationType.FeaturedEntities.equals(item.getType()))
                            .on(TenantConfigurationPersist._featuredEntities)
                            .over(item.getFeaturedEntities())
                            .using(() -> this.validatorFactory.validator(FeaturedEntitiesPersist.FeaturedEntitiesPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefaultPlanBlueprint()) && TenantConfigurationType.DefaultPlanBlueprint.equals(item.getType()))
                            .on(TenantConfigurationPersist._defaultPlanBlueprint)
                            .over(item.getDefaultPlanBlueprint())
                            .using(() -> this.validatorFactory.validator(DefaultPlanBlueprintConfigurationPersist.DefaultPlanBlueprintConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getViewPreferences()) && TenantConfigurationType.ViewPreferences.equals(item.getType()))
                            .on(TenantConfigurationPersist._viewPreferences)
                            .over(item.getViewPreferences())
                            .using(() -> this.validatorFactory.validator(ViewPreferencesConfigurationPersist.ViewPreferencesConfigurationPersistValidator.class))
            );
        }
    }
}
