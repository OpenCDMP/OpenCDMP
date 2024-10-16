package org.opencdmp.model.builder.commonmodels.description;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionModel;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
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
public class PropertyDefinitionCommonModelBuilder extends BaseCommonModelBuilder<PropertyDefinitionModel, PropertyDefinitionEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionEntity definition;
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

    public PropertyDefinitionCommonModelBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }


    private boolean useSharedStorage;
    public PropertyDefinitionCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }
    @Override
    protected List<CommonModelBuilderItemResponse<PropertyDefinitionModel, PropertyDefinitionEntity>> buildInternal(List<PropertyDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PropertyDefinitionModel, PropertyDefinitionEntity>> models = new ArrayList<>();
        for (PropertyDefinitionEntity d : data) {
            PropertyDefinitionModel m = new PropertyDefinitionModel();
            m.setFieldSets(new HashMap<>());
            for (String key : d.getFieldSets().keySet()){
                FieldSetEntity fieldSetEntity = definition != null ? definition.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                m.getFieldSets().put(key, this.builderFactory.builder(PropertyDefinitionFieldSetCommonModelBuilder.class).useSharedStorage(useSharedStorage).authorize(this.authorize).withFieldSetEntity(fieldSetEntity).build(d.getFieldSets().get(key)));
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
