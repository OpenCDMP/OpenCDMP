package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.description.PropertyDefinitionFieldSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PropertyDefinitionFieldSetCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinitionFieldSet, PropertyDefinitionFieldSetModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel fieldSetModel;
    @Autowired
    public PropertyDefinitionFieldSetCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionFieldSetCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PropertyDefinitionFieldSetCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    public PropertyDefinitionFieldSetCommonModelBuilder withFieldSetModel(org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel fieldSetModel) {
        this.fieldSetModel = fieldSetModel;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSet, PropertyDefinitionFieldSetModel>> buildInternal(List<PropertyDefinitionFieldSetModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSet, PropertyDefinitionFieldSetModel>> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetModel d : data) {
            PropertyDefinitionFieldSet m = new PropertyDefinitionFieldSet();
            m.setComment(d.getComment());
            if (d.getItems() != null) m.setItems(this.builderFactory.builder(PropertyDefinitionFieldSetItemModelCommonModelBuilder.class).withFieldSetModel(this.fieldSetModel).authorize(this.authorize).build(d.getItems()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
