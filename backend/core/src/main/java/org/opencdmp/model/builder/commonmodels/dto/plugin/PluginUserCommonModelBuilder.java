package org.opencdmp.model.builder.commonmodels.dto.plugin;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plugin.PluginUserModel;
import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationUser;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PluginUserCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginUserCommonModelBuilder extends BaseCommonModelBuilder<PluginConfigurationUser, PluginUserModel> {
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


    @Override
    protected List<CommonModelBuilderItemResponse<PluginConfigurationUser, PluginUserModel>> buildInternal(List<PluginUserModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PluginConfigurationUser, PluginUserModel>> models = new ArrayList<>();
        for (PluginUserModel d : data) {
            PluginConfigurationUser m = new PluginConfigurationUser();
            m.setPluginCode(d.getCode());

            switch (d.getType()){
                case FileTransformer -> m.setPluginType(org.opencdmp.commons.enums.PluginType.FileTransformer);
                case Deposit -> m.setPluginType(org.opencdmp.commons.enums.PluginType.Deposit);
                case Evaluation -> m.setPluginType(PluginType.Evaluation);
                default -> throw new MyApplicationException("unrecognized type " + d.getType());
            }

            if (d.getFields() != null) m.setUserFields(this.builderFactory.builder(PluginUserFieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
