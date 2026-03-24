package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PlanBlueprintCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintCommonModelBuilder extends BaseCommonModelBuilder<PlanBlueprint, PlanBlueprintModel> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanBlueprintCommonModelBuilder(ConventionService conventionService,
                                           BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
	    this.xmlHandlingService = xmlHandlingService;
    }

    public PlanBlueprintCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanBlueprint, PlanBlueprintModel>> buildInternal(List<PlanBlueprintModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();
        
        List<CommonModelBuilderItemResponse<PlanBlueprint, PlanBlueprintModel>> models = new ArrayList<>();
        

        for (PlanBlueprintModel d : data) {
            PlanBlueprint m = new PlanBlueprint();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setGroupId(d.getGroupId());
            m.setIsActive(IsActive.Active);
            switch (d.getStatus()){
                case Finalized -> m.setStatus(PlanBlueprintStatus.Finalized);
                case Draft -> m.setStatus(PlanBlueprintStatus.Draft);
                default -> throw new MyApplicationException("unrecognized type " + d.getStatus());
            }
            m.setDefinition(this.builderFactory.builder(DefinitionCommonModelBuilder.class).authorize(this.authorize).build(d.getDefinition()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
