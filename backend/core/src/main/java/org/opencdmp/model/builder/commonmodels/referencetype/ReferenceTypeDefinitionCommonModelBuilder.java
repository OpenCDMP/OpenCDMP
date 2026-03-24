package org.opencdmp.model.builder.commonmodels.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeDefinitionModel;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
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
public class ReferenceTypeDefinitionCommonModelBuilder extends BaseCommonModelBuilder<ReferenceTypeDefinitionModel, ReferenceTypeDefinitionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeDefinitionCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeDefinitionCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ReferenceTypeDefinitionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<ReferenceTypeDefinitionModel, ReferenceTypeDefinitionEntity>> buildInternal(List<ReferenceTypeDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<ReferenceTypeDefinitionModel, ReferenceTypeDefinitionEntity>> models = new ArrayList<>();
        for (ReferenceTypeDefinitionEntity d : data) {
            ReferenceTypeDefinitionModel m = new ReferenceTypeDefinitionModel();
            if (d.getFields() != null) m.setFields(this.builderFactory.builder(ReferenceTypeFieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
