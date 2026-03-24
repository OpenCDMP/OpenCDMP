package org.opencdmp.model.builder.commonmodels.dto.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.referencetype.ReferenceType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.ReferenceTypeCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeCommonModelBuilder extends BaseCommonModelBuilder<ReferenceType, ReferenceTypeModel> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
    }

    public ReferenceTypeCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<ReferenceType, ReferenceTypeModel>> buildInternal(List<ReferenceTypeModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<ReferenceType, ReferenceTypeModel>> models = new ArrayList<>();
        for (ReferenceTypeModel d : data) {
            ReferenceType m = new ReferenceType();
            m.setId(d.getId());
            m.setCode(d.getCode());
            m.setName(d.getName());
            m.setIsActive(IsActive.Active);
            m.setDefinition(this.builderFactory.builder(ReferenceTypeDefinitionCommonModelBuilder.class).authorize(this.authorize).build(d.getDefinition()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
