package org.opencdmp.model.builder.commonmodels.planreference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.reference.ReferenceCommonModelBuilder;
import org.opencdmp.query.ReferenceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanReferenceCommonModelBuilder extends BaseCommonModelBuilder<PlanReferenceModel, PlanReferenceEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanReferenceCommonModelBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanReferenceCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
	    this.jsonHandlingService = jsonHandlingService;
    }

    public PlanReferenceCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanReferenceModel, PlanReferenceEntity>> buildInternal(List<PlanReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Map<UUID, ReferenceModel> referenceModelMap = this.collectReferences(data);
        List<CommonModelBuilderItemResponse<PlanReferenceModel, PlanReferenceEntity>> models = new ArrayList<>();
        for (PlanReferenceEntity d : data) {
            PlanReferenceModel m = new PlanReferenceModel();
            m.setId(d.getId());
            if (d.getData() != null){
                PlanReferenceDataEntity definition = this.jsonHandlingService.fromJsonSafe(PlanReferenceDataEntity.class, d.getData());
                m.setData(this.builderFactory.builder(PlanReferenceDataCommonModelBuilder.class).authorize(this.authorize).build(definition));
            }
            if (referenceModelMap != null && d.getReferenceId() != null && referenceModelMap.containsKey(d.getReferenceId())) m.setReference(referenceModelMap.get(d.getReferenceId()));
            
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, ReferenceModel> collectReferences(List<PlanReferenceEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceModel.class.getSimpleName());

        Map<UUID, ReferenceModel> itemMap;
        ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(ReferenceCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, ReferenceEntity::getId);

        return itemMap;
    }
}
