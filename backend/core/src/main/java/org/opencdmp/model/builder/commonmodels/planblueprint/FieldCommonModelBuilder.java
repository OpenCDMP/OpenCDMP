package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commonmodels.models.planblueprint.FieldModel;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
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

@Component("planblueprint.FieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class FieldCommonModelBuilder<Model extends FieldModel, Entity extends FieldEntity> extends BaseCommonModelBuilder<Model, Entity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public FieldCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldCommonModelBuilder.class)));
    }

    public FieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    protected abstract Model getInstance();

    protected abstract Model buildChild(Entity data, Model model);

    @Override
    protected List<CommonModelBuilderItemResponse<Model, Entity>> buildInternal(List<Entity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Model, Entity>> models = new ArrayList<>();
        for (Entity d : data) {
            Model m =  this.getInstance();
            m.setId(d.getId());
            m.setDescription(d.getDescription());
            switch (d.getCategory()){
                case System -> m.setCategory(PlanBlueprintFieldCategory.System);
                case Extra -> m.setCategory(PlanBlueprintFieldCategory.Extra);
                case ReferenceType -> m.setCategory(PlanBlueprintFieldCategory.ReferenceType);
                case Upload -> m.setCategory(PlanBlueprintFieldCategory.Upload);
                default -> throw new MyApplicationException("unrecognized type " + d.getCategory());
            }
            m.setPlaceholder(d.getPlaceholder());
            m.setDescription(d.getDescription());
            m.setSemantics(d.getSemantics());
            m.setRequired(d.isRequired());
            m.setLabel(d.getLabel());
            m.setOrdinal(d.getOrdinal());
            this.buildChild(d, m);
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
