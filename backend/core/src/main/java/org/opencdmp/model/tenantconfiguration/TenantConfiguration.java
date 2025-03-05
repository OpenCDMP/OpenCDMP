package org.opencdmp.model.tenantconfiguration;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;

import java.time.Instant;
import java.util.UUID;

public class TenantConfiguration {

    private UUID id;

    public static final String _id = "id";

    private TenantConfigurationType type;

    public static final String _type = "type";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private CssColorsTenantConfiguration cssColors;

    public static final String _cssColors = "cssColors";

    private DefaultUserLocaleTenantConfiguration defaultUserLocale;

    public static final String _defaultUserLocale = "defaultUserLocale";

    private DepositTenantConfiguration depositPlugins;

    public static final String _depositPlugins = "depositPlugins";

    private FileTransformerTenantConfiguration fileTransformerPlugins;

    public static final String _fileTransformerPlugins = "fileTransformerPlugins";

    private EvaluatorTenantConfiguration evaluatorPlugins;

    public static final String _evaluatorPlugins = "evaluatorPlugins";

    private LogoTenantConfiguration logo;

    public static final String _logo = "logo";

    private FeaturedEntities featuredEntities;

    public static final String _featuredEntities = "featuredEntities";

    private DefaultPlanBlueprintConfiguration defaultPlanBlueprint;

    public static final String _defaultPlanBlueprint = "defaultPlanBlueprint";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;

    public static final String _isActive = "isActive";

    private String hash;

    public static final String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public CssColorsTenantConfiguration getCssColors() {
        return cssColors;
    }

    public void setCssColors(CssColorsTenantConfiguration cssColors) {
        this.cssColors = cssColors;
    }

    public DefaultUserLocaleTenantConfiguration getDefaultUserLocale() {
        return defaultUserLocale;
    }

    public void setDefaultUserLocale(DefaultUserLocaleTenantConfiguration defaultUserLocale) {
        this.defaultUserLocale = defaultUserLocale;
    }

    public DepositTenantConfiguration getDepositPlugins() {
        return depositPlugins;
    }

    public void setDepositPlugins(DepositTenantConfiguration depositPlugins) {
        this.depositPlugins = depositPlugins;
    }

    public FileTransformerTenantConfiguration getFileTransformerPlugins() {
        return fileTransformerPlugins;
    }

    public void setFileTransformerPlugins(FileTransformerTenantConfiguration fileTransformerPlugins) {
        this.fileTransformerPlugins = fileTransformerPlugins;
    }

    public LogoTenantConfiguration getLogo() {
        return logo;
    }

    public void setLogo(LogoTenantConfiguration logo) {
        this.logo = logo;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }

    public TenantConfigurationType getType() {
        return type;
    }

    public void setType(TenantConfigurationType type) {
        this.type = type;
    }

    public EvaluatorTenantConfiguration getEvaluatorPlugins() {
        return evaluatorPlugins;
    }

    public void setEvaluatorPlugins(EvaluatorTenantConfiguration evaluatorPlugins) {
        this.evaluatorPlugins = evaluatorPlugins;
    }

    public FeaturedEntities getFeaturedEntities() {
        return featuredEntities;
    }

    public void setFeaturedEntities(FeaturedEntities featuredEntities) {
        this.featuredEntities = featuredEntities;
    }

    public DefaultPlanBlueprintConfiguration getDefaultPlanBlueprint() {
        return defaultPlanBlueprint;
    }

    public void setDefaultPlanBlueprint(DefaultPlanBlueprintConfiguration defaultPlanBlueprint) {
        this.defaultPlanBlueprint = defaultPlanBlueprint;
    }
}
