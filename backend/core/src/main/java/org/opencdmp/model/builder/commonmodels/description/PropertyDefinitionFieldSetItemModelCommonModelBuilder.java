package org.opencdmp.model.builder.commonmodels.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetItemModelCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinitionFieldSetItemModel, PropertyDefinitionFieldSetItemEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private FieldSetEntity fieldSetEntity;
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


    public PropertyDefinitionFieldSetItemModelCommonModelBuilder withFieldSetEntity(FieldSetEntity fieldSetEntity) {
        this.fieldSetEntity = fieldSetEntity;
        return this;
    }


    private boolean useSharedStorage;
    public PropertyDefinitionFieldSetItemModelCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

   
    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetItemModel, PropertyDefinitionFieldSetItemEntity>> buildInternal(List<PropertyDefinitionFieldSetItemEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinitionFieldSetItemModel, PropertyDefinitionFieldSetItemEntity>> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetItemEntity d : data) {
            PropertyDefinitionFieldSetItemModel m = new PropertyDefinitionFieldSetItemModel();

            m.setOrdinal(d.getOrdinal());
            if (d.getFields() != null && !d.getFields().isEmpty()) {
                m.setFields(new HashMap<>());
                for (String key : d.getFields().keySet()){
                    FieldEntity fieldEntity = this.fieldSetEntity != null ? this.fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                    m.getFields().put(key, this.builderFactory.builder(FieldCommonModelBuilder.class).useSharedStorage(this.useSharedStorage).authorize(this.authorize).withFieldEntity(fieldEntity).build(d.getFields().get(key)));
                }
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
