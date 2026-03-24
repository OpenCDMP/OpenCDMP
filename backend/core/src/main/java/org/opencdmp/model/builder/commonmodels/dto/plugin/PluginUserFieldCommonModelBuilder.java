package org.opencdmp.model.builder.commonmodels.dto.plugin;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plugin.PluginUserFieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationUserField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PluginUserFieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginUserFieldCommonModelBuilder extends BaseCommonModelBuilder<PluginConfigurationUserField, PluginUserFieldModel> {

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PluginUserFieldCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginUserFieldCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PluginUserFieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PluginConfigurationUserField, PluginUserFieldModel>> buildInternal(List<PluginUserFieldModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginConfigurationUserField, PluginUserFieldModel>> models = new ArrayList<>();
        for (PluginUserFieldModel d : data) {
            PluginConfigurationUserField m = new PluginConfigurationUserField();
            m.setCode(d.getCode());
            m.setTextValue(d.getTextValue());
            m.setFileValue(this.builderFactory.builder(FileEnvelopeCommonModelBuilder.class).authorize(this.authorize).build(d.getFile()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
