package org.opencdmp.model.builder.commonmodels.dto.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.reference.ReferenceDefinitionModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.reference.Definition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.ReferenceDefinitionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceDefinitionCommonModelBuilder extends BaseCommonModelBuilder<Definition, ReferenceDefinitionModel> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceDefinitionCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceDefinitionCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ReferenceDefinitionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Definition, ReferenceDefinitionModel>> buildInternal(List<ReferenceDefinitionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Definition, ReferenceDefinitionModel>> models = new ArrayList<>();
        for (ReferenceDefinitionModel d : data) {
            Definition m = new Definition();
            if (d.getFields() != null) m.setFields(this.builderFactory.builder(ReferenceFieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
