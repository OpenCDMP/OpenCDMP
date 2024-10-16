package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PlanBlueprintStatus;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
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
public class PlanBlueprintCommonModelBuilder extends BaseCommonModelBuilder<PlanBlueprintModel, PlanBlueprintEntity> {

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
    protected List<CommonModelBuilderItemResponse<PlanBlueprintModel, PlanBlueprintEntity>> buildInternal(List<PlanBlueprintEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();
        
        List<CommonModelBuilderItemResponse<PlanBlueprintModel, PlanBlueprintEntity>> models = new ArrayList<>();
        

        for (PlanBlueprintEntity d : data) {
            PlanBlueprintModel m = new PlanBlueprintModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setGroupId(d.getGroupId());
            switch (d.getStatus()){
                case Finalized -> m.setStatus(PlanBlueprintStatus.Finalized);
                case Draft -> m.setStatus(PlanBlueprintStatus.Draft);
                default -> throw new MyApplicationException("unrecognized type " + d.getStatus());
            }
            if (d.getDefinition() != null){
                //TODO Update with the new logic of property definition 
                DefinitionEntity propertyDefinition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionCommonModelBuilder.class).authorize(this.authorize).build(propertyDefinition));
            }
            

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
