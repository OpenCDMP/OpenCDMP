package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.description.PropertyDefinitionFieldSetItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PropertyDefinitionFieldSetItemModelCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetItemModelCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinitionFieldSetItem, PropertyDefinitionFieldSetItemModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel fieldSetModel;
    @Autowired
    public PropertyDefinitionFieldSetItemModelCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionFieldSetItemModelCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PropertyDefinitionFieldSetItemModelCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    public PropertyDefinitionFieldSetItemModelCommonModelBuilder withFieldSetModel(org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel fieldSetModel) {
        this.fieldSetModel = fieldSetModel;
        return this;
    }

   
    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetItem, PropertyDefinitionFieldSetItemModel>> buildInternal(List<PropertyDefinitionFieldSetItemModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetItem, PropertyDefinitionFieldSetItemModel>> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetItemModel d : data) {
            PropertyDefinitionFieldSetItem m = new PropertyDefinitionFieldSetItem();

            m.setOrdinal(d.getOrdinal());
            if (d.getFields() != null && !d.getFields().isEmpty()) {
                m.setFields(new HashMap<>());
                for (String key : d.getFields().keySet()){
                    FieldModel fieldModel = this.fieldSetModel != null ? this.fieldSetModel.getFieldById(key).stream().findFirst().orElse(null) : null;
                    m.getFields().put(key, this.builderFactory.builder(FieldCommonModelBuilder.class).authorize(this.authorize).withFieldModel(fieldModel).build(d.getFields().get(key)));
                }
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
