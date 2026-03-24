package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper.FieldDataHelperService;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper.FieldDataHelperServiceProvider;
import org.opencdmp.model.descriptiontemplate.Field;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.commonmodels.descriptiontemplate")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldCommonModelBuilder extends BaseCommonModelBuilder<Field, FieldModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;
    @Autowired
    public FieldCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, FieldDataHelperServiceProvider fieldDataHelperServiceProvider
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
    }

    public FieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Field, FieldModel>> buildInternal(List<FieldModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Field, FieldModel>> models = new ArrayList<>();
        for (FieldModel d : data) {
            Field m = new Field();
            m.setId(d.getId());
            m.setOrdinal(d.getOrdinal());
            m.setSemantics(d.getSemantics());
            if (d.getDefaultValue() != null) m.setDefaultValue(this.builderFactory.builder(DefaultValueCommonModelBuilder.class).build(d.getDefaultValue()));
            m.setIncludeInExport(d.getIncludeInExport());
            if (!this.conventionService.isListNullOrEmpty(d.getValidations())){
                m.setValidations(new ArrayList<>());
                for (org.opencdmp.commonmodels.enums.FieldValidationType fieldValidationType : d.getValidations()) {
                    switch (fieldValidationType){
                        case Url -> m.getValidations().add(FieldValidationType.Url);
                        case None -> m.getValidations().add(FieldValidationType.None);
                        case Required -> m.getValidations().add(FieldValidationType.Required);
                        default -> throw new MyApplicationException("unrecognized type " + fieldValidationType);
                    }
                    
                }
            }
            if (d.getData() != null){
                FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(d.getData().getFieldType());
                m.setData(fieldDataHelperService.buildOne(d.getData(), this.authorize));
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
