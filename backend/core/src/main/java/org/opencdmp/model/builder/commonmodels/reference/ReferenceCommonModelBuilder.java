package org.opencdmp.model.builder.commonmodels.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.ReferenceSourceType;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceTypeModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.ReferenceTypeCommonModelBuilder;
import org.opencdmp.query.ReferenceTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceCommonModelBuilder extends BaseCommonModelBuilder<ReferenceModel, ReferenceEntity> {

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
    protected List<CommonModelBuilderItemResponse<ReferenceModel, ReferenceEntity>> buildInternal(List<ReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Map<UUID, ReferenceTypeModel> typeMap = this.collectReferenceTypes(data);
        List<CommonModelBuilderItemResponse<ReferenceModel, ReferenceEntity>> models = new ArrayList<>();
        for (ReferenceEntity d : data) {
            ReferenceModel m = new ReferenceModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            if (d.getDefinition() != null){
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(ReferenceDefinitionCommonModelBuilder.class).authorize(this.authorize).build(definition));
            }
            m.setReference(d.getReference());
            m.setAbbreviation(d.getAbbreviation());
            m.setDescription(d.getDescription());
            m.setSource(d.getSource());
            switch (d.getSourceType()){
                case Internal -> m.setSourceType(ReferenceSourceType.Internal);
                case External -> m.setSourceType(ReferenceSourceType.External);
                default -> throw new MyApplicationException("unrecognized type " + d.getSourceType().getValue());
            }
            if (typeMap != null && d.getTypeId() != null && typeMap.containsKey(d.getTypeId())) m.setType(typeMap.get(d.getTypeId()));
            
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, ReferenceTypeModel> collectReferenceTypes(List<ReferenceEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceTypeModel.class.getSimpleName());

        Map<UUID, ReferenceTypeModel> itemMap;
        ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, ReferenceTypeEntity::getId);

        return itemMap;
    }
}
