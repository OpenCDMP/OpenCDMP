package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.description.FieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.reference.ReferenceCommonModelBuilder;
import org.opencdmp.model.description.Field;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.FieldCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldCommonModelBuilder extends BaseCommonModelBuilder<Field, FieldModel> {
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel fieldModel;
    @Autowired
    public FieldCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public FieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    public FieldCommonModelBuilder withFieldModel(org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel fieldModel) {
        this.fieldModel = fieldModel;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Field, FieldModel>> buildInternal(List<FieldModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        FieldType fieldType = this.fieldModel != null && this.fieldModel.getData() != null ? this.fieldModel.getData().getFieldType() :  FieldType.FREE_TEXT;

        List<CommonModelBuilderItemResponse<Field, FieldModel>> models = new ArrayList<>();
        for (FieldModel d : data) {
            Field m = new Field();
            if (this.isTextType(fieldType)) m.setTextValue(d.getTextValue());
            if (this.isTextListType(fieldType)) m.setTextListValue(d.getTextListValue());
            if (this.isDateType(fieldType)) m.setDateValue(d.getDateValue());
            if (this.isBooleanType(fieldType)) m.setBooleanValue(d.getBooleanValue());
            if (this.isReferenceType(fieldType)) m.setReferences(this.builderFactory.builder(ReferenceCommonModelBuilder.class).authorize(this.authorize).build(d.getReferences()));
            if (this.isExternalIdentifierType(fieldType)) m.setExternalIdentifier(this.builderFactory.builder(ExternalIdentifierCommonModelBuilder.class).authorize(this.authorize).build(d.getExternalIdentifier()));
            if (this.isTagType(fieldType)) m.setTextListValue(d.getTextListValue());
            if (this.isTagType(fieldType)) m.setTextValue(d.getTextValue());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private boolean isReferenceType(FieldType fieldType){
        return fieldType.equals(FieldType.REFERENCE_TYPES);
    }

    private boolean isTextType(FieldType fieldType){
        return  fieldType.equals(FieldType.FREE_TEXT)  || fieldType.equals(FieldType.TEXT_AREA) ||
                fieldType.equals(FieldType.RICH_TEXT_AREA) || fieldType.equals(FieldType.UPLOAD) ||
                fieldType.equals(FieldType.RADIO_BOX);
    }

    private boolean isTextListType(FieldType fieldType){
        return  fieldType.equals(FieldType.SELECT)  || fieldType.equals(FieldType.INTERNAL_ENTRIES_PlANS) ||
                fieldType.equals(FieldType.INTERNAL_ENTRIES_DESCRIPTIONS);
    }

    private boolean isTagType(FieldType fieldType){
        return fieldType.equals(FieldType.TAGS);
    }

    private boolean isDateType(FieldType fieldType){
        return  fieldType.equals(FieldType.DATE_PICKER);
    }
    private boolean isBooleanType(FieldType fieldType){
        return  fieldType.equals(FieldType.BOOLEAN_DECISION) || fieldType.equals(FieldType.CHECK_BOX);
    }

    private boolean isExternalIdentifierType(FieldType fieldType){
        return  fieldType.equals(FieldType.VALIDATION) ||  fieldType.equals(FieldType.DATASET_IDENTIFIER) ;
    }


}
