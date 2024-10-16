package org.opencdmp.model.builder.commonmodels.description;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetModel;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
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

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinitionFieldSetModel, PropertyDefinitionFieldSetEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private FieldSetEntity fieldSetEntity;
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


    public PropertyDefinitionFieldSetCommonModelBuilder withFieldSetEntity(FieldSetEntity fieldSetEntity) {
        this.fieldSetEntity = fieldSetEntity;
        return this;
    }


    private boolean useSharedStorage;
    public PropertyDefinitionFieldSetCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }
    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetModel, PropertyDefinitionFieldSetEntity>> buildInternal(List<PropertyDefinitionFieldSetEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetModel, PropertyDefinitionFieldSetEntity>> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetEntity d : data) {
            PropertyDefinitionFieldSetModel m = new PropertyDefinitionFieldSetModel();
            m.setComment(d.getComment());
            if (d.getItems() != null) m.setItems(this.builderFactory.builder(PropertyDefinitionFieldSetItemModelCommonModelBuilder.class).useSharedStorage(useSharedStorage).withFieldSetEntity(this.fieldSetEntity).authorize(this.authorize).build(d.getItems()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
