package org.opencdmp.model.builder.commonmodels.dto.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.DefinitionModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.description.PropertyDefinitionFieldSetCommonModelBuilder;
import org.opencdmp.model.plan.PlanProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanPropertiesCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanPropertiesCommonModelBuilder extends BaseCommonModelBuilder<PlanProperties, PlanPropertiesModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionModel definitionModel;

    @Autowired
    public PlanPropertiesCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanPropertiesCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PlanPropertiesCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanPropertiesCommonModelBuilder withDefinitionModel(DefinitionModel definitionModel) {
        this.definitionModel = definitionModel;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanProperties, PlanPropertiesModel>> buildInternal(List<PlanPropertiesModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanProperties, PlanPropertiesModel>> models = new ArrayList<>();
        for (PlanPropertiesModel d : data) {
            PlanProperties m = new PlanProperties();
            m.setFieldSets(new HashMap<>());
            if (d.getFieldSets() != null) {
                for (String key : d.getFieldSets().keySet()){
                    FieldSetModel fieldSetModel = definitionModel != null ? definitionModel.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                    m.getFieldSets().put(key, this.builderFactory.builder(PropertyDefinitionFieldSetCommonModelBuilder.class).authorize(this.authorize).withFieldSetModel(fieldSetModel).build(d.getFieldSets().get(key)));
                }
            }
            if (d.getContacts() != null) m.setContacts(this.builderFactory.builder(PlanContactCommonModelBuilder.class).authorize(this.authorize).build(d.getContacts()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
