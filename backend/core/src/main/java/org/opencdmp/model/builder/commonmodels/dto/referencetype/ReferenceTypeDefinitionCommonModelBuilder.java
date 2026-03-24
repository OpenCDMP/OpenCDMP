package org.opencdmp.model.builder.commonmodels.dto.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeDefinitionModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.referencetype.ReferenceTypeDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.ReferenceTypeDefinitionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeDefinitionCommonModelBuilder extends BaseCommonModelBuilder<ReferenceTypeDefinition, ReferenceTypeDefinitionModel> {

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
    protected List<CommonModelBuilderItemResponse<ReferenceTypeDefinition, ReferenceTypeDefinitionModel>> buildInternal(List<ReferenceTypeDefinitionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<ReferenceTypeDefinition, ReferenceTypeDefinitionModel>> models = new ArrayList<>();
        for (ReferenceTypeDefinitionModel d : data) {
            ReferenceTypeDefinition m = new ReferenceTypeDefinition();
            if (d.getFields() != null) m.setFields(this.builderFactory.builder(ReferenceTypeFieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
