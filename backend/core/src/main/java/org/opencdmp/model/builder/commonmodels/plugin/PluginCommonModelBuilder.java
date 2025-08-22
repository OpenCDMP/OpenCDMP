package org.opencdmp.model.builder.commonmodels.plugin;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PluginType;
import org.opencdmp.commonmodels.models.plugin.PluginModel;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginCommonModelBuilder extends BaseCommonModelBuilder<PluginModel, PluginConfigurationEntity> {
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

    private boolean useSharedStorage;
    public PluginCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PluginModel, PluginConfigurationEntity>> buildInternal(List<PluginConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginModel, PluginConfigurationEntity>> models = new ArrayList<>();
        for (PluginConfigurationEntity d : data) {
            PluginModel m = new PluginModel();
            m.setCode(d.getPluginCode());

            switch (d.getPluginType()){
                case FileTransformer -> m.setType(PluginType.FileTransformer);
                case Deposit -> m.setType(PluginType.Deposit);
                case Evaluation -> m.setType(PluginType.Evaluation);
                default -> throw new MyApplicationException("unrecognized type " + d.getPluginType());
            }

            if ( d.getFields() != null) m.setFields(this.builderFactory.builder(PluginFieldCommonModelBuilder.class).useSharedStorage(useSharedStorage).authorize(this.authorize).build(d.getFields()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
