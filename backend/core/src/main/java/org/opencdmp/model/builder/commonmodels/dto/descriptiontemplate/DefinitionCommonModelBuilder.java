package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.DefinitionModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.plugin.PluginCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.Definition;
import org.opencdmp.service.tenantconfiguration.TenantConfigurationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.DefinitionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionCommonModelBuilder extends BaseCommonModelBuilder<Definition, DefinitionModel> {
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


    @Override
    protected List<CommonModelBuilderItemResponse<Definition, DefinitionModel>> buildInternal(List<DefinitionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Definition, DefinitionModel>> models = new ArrayList<>();
        for (DefinitionModel d : data) {
            Definition m = new Definition();
            if (d.getPages() != null) {
                m.setPages(this.builderFactory.builder(PageCommonModelBuilder.class).authorize(this.authorize).build(d.getPages()));
            }
            m.setPluginConfigurations(this.builderFactory.builder(PluginCommonModelBuilder.class).authorize(this.authorize).build(d.getPlugins()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
