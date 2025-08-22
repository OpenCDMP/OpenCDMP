package org.opencdmp.model.builder.commonmodels.descriptiontemplate;

import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.DefinitionModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationFieldEntity;
import org.opencdmp.commons.types.tenantconfiguration.PluginTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.builder.commonmodels.plugin.PluginCommonModelBuilder;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.service.tenantconfiguration.TenantConfigurationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionCommonModelBuilder extends BaseCommonModelBuilder<DefinitionModel, DefinitionEntity> {
    private final BuilderFactory builderFactory;
    private final TenantConfigurationService tenantConfigurationService;
    private final JsonHandlingService jsonHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public DefinitionCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, TenantConfigurationService tenantConfigurationService, JsonHandlingService jsonHandlingService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefinitionCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
        this.tenantConfigurationService = tenantConfigurationService;
        this.jsonHandlingService = jsonHandlingService;
    }

    public DefinitionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private boolean useSharedStorage;
    public DefinitionCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    private String repositoryId;
    public DefinitionCommonModelBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DefinitionModel, DefinitionEntity>> buildInternal(List<DefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DefinitionModel, DefinitionEntity>> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            DefinitionModel m = new DefinitionModel();
            if (d.getPages() != null) m.setPages(this.builderFactory.builder(PageCommonModelBuilder.class).authorize(this.authorize).build(d.getPages()));

            TenantConfigurationEntity tenantConfigurationEntity = null;
            try {
                tenantConfigurationEntity = this.tenantConfigurationService.getActiveType(TenantConfigurationType.PluginConfiguration, new BaseFieldSet().ensure(TenantConfiguration._id).ensure(TenantConfiguration._pluginConfiguration));
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            }

            if (d.getPluginConfigurations() == null){
                if (tenantConfigurationEntity != null && tenantConfigurationEntity.getValue() != null) {
                    PluginTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(PluginTenantConfigurationEntity.class, tenantConfigurationEntity.getValue());
                    if (!this.conventionService.isListNullOrEmpty(valueTyped.getPluginConfigurations())) {
                        m.setPlugins(this.builderFactory.builder(PluginCommonModelBuilder.class).useSharedStorage(useSharedStorage).authorize(this.authorize).build(valueTyped.getPluginConfigurations().stream().filter(x -> x.getPluginCode().equals(this.repositoryId)).toList()));
                    }
                }
            }
            else {
                for (PluginConfigurationEntity pluginConfiguration: d.getPluginConfigurations()) {
                    for (PluginConfigurationFieldEntity field: pluginConfiguration.getFields()) {
                        // if field don't have values override from tenant configuration
                        if (field.getFileValue() == null && this.conventionService.isNullOrEmpty(field.getTextValue()) && tenantConfigurationEntity != null && tenantConfigurationEntity.getValue() != null) {

                            PluginTenantConfigurationEntity valueTyped = this.jsonHandlingService.fromJsonSafe(PluginTenantConfigurationEntity.class, tenantConfigurationEntity.getValue());
                            if (valueTyped != null && !this.conventionService.isListNullOrEmpty(valueTyped.getPluginConfigurations())) {
                                PluginConfigurationEntity tenantPluginConfigurationEntity = valueTyped.getPluginConfigurations().stream().filter(x -> x.getPluginCode().equals(pluginConfiguration.getPluginCode()) && x.getPluginType().equals(pluginConfiguration.getPluginType())).findFirst().orElse(null);
                                if (tenantPluginConfigurationEntity != null) {
                                    PluginConfigurationFieldEntity tenantFieldEntity = tenantPluginConfigurationEntity.getFields().stream().filter(x -> x.getCode().equals(field.getCode())).findFirst().orElse(null);
                                    if (tenantFieldEntity != null) {
                                        field.setTextValue(tenantFieldEntity.getTextValue());
                                        field.setFileValue(tenantFieldEntity.getFileValue());
                                    }
                                }
                            }
                        }

                    }
                }
                m.setPlugins(this.builderFactory.builder(PluginCommonModelBuilder.class).useSharedStorage(useSharedStorage).authorize(this.authorize).build(d.getPluginConfigurations().stream().filter(x -> x.getPluginCode().equals(this.repositoryId)).toList()));

            }

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
