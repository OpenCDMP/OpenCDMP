package org.opencdmp.model.builder.tenantconfiguration;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.tenantconfiguration.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantConfigurationBuilder extends BaseBuilder<TenantConfiguration, TenantConfigurationEntity> {
    private final TenantScope tenantScope;
    private final JsonHandlingService jsonHandlingService;

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public TenantConfigurationBuilder(
		    ConventionService conventionService,
		    TenantScope tenantScope, JsonHandlingService jsonHandlingService, BuilderFactory builderFactory1) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TenantConfigurationBuilder.class)));
	    this.tenantScope = tenantScope;
	    this.jsonHandlingService = jsonHandlingService;
	    this.builderFactory = builderFactory1;
    }

    public TenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<TenantConfiguration> build(FieldSet fields, List<TenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet cssColorsFields = fields.extractPrefixed(this.asPrefix(TenantConfiguration._cssColors));
        FieldSet defaultUserLocaleFields = fields.extractPrefixed(this.asPrefix(TenantConfiguration._defaultUserLocale));
        FieldSet depositPluginsFields = fields.extractPrefixed(this.asPrefix(TenantConfiguration._depositPlugins));
        FieldSet fileTransformerPluginsFields = fields.extractPrefixed(this.asPrefix(TenantConfiguration._fileTransformerPlugins));
        FieldSet logoFields = fields.extractPrefixed(this.asPrefix(TenantConfiguration._logo));
        
        
        List<TenantConfiguration> models = new ArrayList<>();
        for (TenantConfigurationEntity d : data) {
            TenantConfiguration m = new TenantConfiguration();
            if (fields.hasField(this.asIndexer(TenantConfiguration._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(TenantConfiguration._type))) m.setType(d.getType());
            if (!cssColorsFields.isEmpty() && !this.conventionService.isNullOrEmpty(d.getValue()) && TenantConfigurationType.CssColors.equals(d.getType())){
                CssColorsTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(CssColorsTenantConfigurationEntity.class, d.getValue());
                m.setCssColors(this.builderFactory.builder(CssColorsTenantConfigurationBuilder.class).authorize(this.authorize).build(cssColorsFields, valueTyped));
            }
            if (!defaultUserLocaleFields.isEmpty() && !this.conventionService.isNullOrEmpty(d.getValue()) && TenantConfigurationType.DefaultUserLocale.equals(d.getType())){
                DefaultUserLocaleTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(DefaultUserLocaleTenantConfigurationEntity.class, d.getValue());
                m.setDefaultUserLocale(this.builderFactory.builder(DefaultUserLocaleTenantConfigurationBuilder.class).authorize(this.authorize).build(defaultUserLocaleFields, valueTyped));
            }
            if (!depositPluginsFields.isEmpty() && !this.conventionService.isNullOrEmpty(d.getValue()) && TenantConfigurationType.DepositPlugins.equals(d.getType())){
                DepositTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(DepositTenantConfigurationEntity.class, d.getValue());
                m.setDepositPlugins(this.builderFactory.builder(DepositTenantConfigurationBuilder.class).authorize(this.authorize).build(depositPluginsFields, valueTyped));
            }
            if (!fileTransformerPluginsFields.isEmpty() && !this.conventionService.isNullOrEmpty(d.getValue()) && TenantConfigurationType.FileTransformerPlugins.equals(d.getType())){
                FileTransformerTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(FileTransformerTenantConfigurationEntity.class, d.getValue());
                m.setFileTransformerPlugins(this.builderFactory.builder(FileTransformerTenantConfigurationBuilder.class).authorize(this.authorize).build(fileTransformerPluginsFields, valueTyped));
            }
            if (!logoFields.isEmpty() && !this.conventionService.isNullOrEmpty(d.getValue()) && TenantConfigurationType.Logo.equals(d.getType())){
                LogoTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(LogoTenantConfigurationEntity.class, d.getValue());
                m.setLogo(this.builderFactory.builder(LogoTenantConfigurationBuilder.class).authorize(this.authorize).build(logoFields, valueTyped));
            }
            if (fields.hasField(this.asIndexer(TenantConfiguration._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(TenantConfiguration._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(TenantConfiguration._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(TenantConfiguration._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(TenantConfiguration._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
