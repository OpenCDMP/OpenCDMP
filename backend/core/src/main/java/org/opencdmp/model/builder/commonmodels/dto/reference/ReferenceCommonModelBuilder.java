package org.opencdmp.model.builder.commonmodels.dto.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.referencetype.ReferenceTypeCommonModelBuilder;
import org.opencdmp.model.reference.Reference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.ReferenceCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceCommonModelBuilder extends BaseCommonModelBuilder<Reference, ReferenceModel> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceCommonModelBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
	    this.xmlHandlingService = xmlHandlingService;
    }

    public ReferenceCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Reference, ReferenceModel>> buildInternal(List<ReferenceModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Reference, ReferenceModel>> models = new ArrayList<>();
        for (ReferenceModel d : data) {
            Reference m = new Reference();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDefinition(this.builderFactory.builder(ReferenceDefinitionCommonModelBuilder.class).authorize(this.authorize).build(d.getDefinition()));
            m.setType(this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).authorize(this.authorize).build(d.getType()));
            m.setReference(d.getReference());
            m.setAbbreviation(d.getAbbreviation());
            m.setDescription(d.getDescription());
            m.setSource(d.getSource());
            m.setIsActive(IsActive.Active);
            if (d.getSourceType() != null) {
                switch (d.getSourceType()){
                    case Internal -> m.setSourceType(ReferenceSourceType.Internal);
                    case External -> m.setSourceType(ReferenceSourceType.External);
                    default -> throw new MyApplicationException("unrecognized type " + d.getSourceType().getValue());
                }
            }
            
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
