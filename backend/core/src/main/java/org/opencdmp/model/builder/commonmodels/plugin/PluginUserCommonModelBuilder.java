package org.opencdmp.model.builder.commonmodels.plugin;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PluginType;
import org.opencdmp.commonmodels.models.plugin.PluginUserModel;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginUserCommonModelBuilder extends BaseCommonModelBuilder<PluginUserModel, PluginConfigurationUserEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PluginUserCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PluginUserCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PluginUserCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private boolean useSharedStorage;
    public PluginUserCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PluginUserModel, PluginConfigurationUserEntity>> buildInternal(List<PluginConfigurationUserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginUserModel, PluginConfigurationUserEntity>> models = new ArrayList<>();
        for (PluginConfigurationUserEntity d : data) {
            PluginUserModel m = new PluginUserModel();
            m.setCode(d.getPluginCode());

            switch (d.getPluginType()){
                case FileTransformer -> m.setType(PluginType.FileTransformer);
                case Deposit -> m.setType(PluginType.Deposit);
                case Evaluation -> m.setType(PluginType.Evaluation);
                default -> throw new MyApplicationException("unrecognized type " + d.getPluginType());
            }

            if ( d.getUserFields() != null) m.setFields(this.builderFactory.builder(PluginUserFieldCommonModelBuilder.class).useSharedStorage(useSharedStorage).authorize(this.authorize).build(d.getUserFields()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
