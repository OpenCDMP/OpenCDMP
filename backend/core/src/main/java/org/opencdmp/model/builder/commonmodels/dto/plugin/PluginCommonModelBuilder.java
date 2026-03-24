package org.opencdmp.model.builder.commonmodels.dto.plugin;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plugin.PluginModel;
import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PluginCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginCommonModelBuilder extends BaseCommonModelBuilder<PluginConfiguration, PluginModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PluginCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PluginCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PluginConfiguration, PluginModel>> buildInternal(List<PluginModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginConfiguration, PluginModel>> models = new ArrayList<>();
        for (PluginModel d : data) {
            PluginConfiguration m = new PluginConfiguration();
            m.setPluginCode(d.getCode());

            switch (d.getType()){
                case FileTransformer -> m.setPluginType(PluginType.FileTransformer);
                case Deposit -> m.setPluginType(PluginType.Deposit);
                case Evaluation -> m.setPluginType(PluginType.Evaluation);
                default -> throw new MyApplicationException("unrecognized type " + d.getType());
            }

            if ( d.getFields() != null) m.setFields(this.builderFactory.builder(PluginFieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
