package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DefinitionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.description.PropertyDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PropertyDefinitionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinition, PropertyDefinitionModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionModel definitionModel;
    @Autowired
    public PropertyDefinitionCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PropertyDefinitionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PropertyDefinitionCommonModelBuilder withDefinitionModel(DefinitionModel definitionModel) {
        this.definitionModel = definitionModel;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinition, PropertyDefinitionModel>> buildInternal(List<PropertyDefinitionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinition, PropertyDefinitionModel>> models = new ArrayList<>();
        for (PropertyDefinitionModel d : data) {
            PropertyDefinition m = new PropertyDefinition();
            m.setFieldSets(new HashMap<>());
            if (d.getFieldSets() != null) {
                for (String key : d.getFieldSets().keySet()){
                    FieldSetModel fieldSetModel = definitionModel != null ? definitionModel.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                    m.getFieldSets().put(key, this.builderFactory.builder(PropertyDefinitionFieldSetCommonModelBuilder.class).authorize(this.authorize).withFieldSetModel(fieldSetModel).build(d.getFieldSets().get(key)));
                }
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
