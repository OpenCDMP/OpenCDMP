package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.BaseFieldDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class BaseFieldDataCommonModelBuilder<Model extends BaseFieldDataModel, Entity extends BaseFieldDataEntity> extends BaseCommonModelBuilder<Model, Entity> {
    protected EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public BaseFieldDataCommonModelBuilder(
            ConventionService conventionService,
            LoggerService logger) {
        super(conventionService, logger);
    }

    public BaseFieldDataCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    protected abstract Model getInstance();

    protected abstract void buildChild(Entity d, Model m);


    @Override
    protected List<CommonModelBuilderItemResponse<Model, Entity>> buildInternal(List<Entity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Model, Entity>> models = new ArrayList<>();
        for (Entity d : data) {
            Model m = this.getInstance();
            m.setLabel(d.getLabel());
            switch (d.getFieldType()){
                case SELECT -> m.setFieldType(FieldType.SELECT);
                case BOOLEAN_DECISION -> m.setFieldType(FieldType.BOOLEAN_DECISION);
                case RADIO_BOX -> m.setFieldType(FieldType.RADIO_BOX);
                case INTERNAL_ENTRIES_PLANS -> m.setFieldType(FieldType.INTERNAL_ENTRIES_PlANS);
                case INTERNAL_ENTRIES_DESCRIPTIONS -> m.setFieldType(FieldType.INTERNAL_ENTRIES_DESCRIPTIONS);
                case CHECK_BOX -> m.setFieldType(FieldType.CHECK_BOX);
                case FREE_TEXT -> m.setFieldType(FieldType.FREE_TEXT);
                case TEXT_AREA -> m.setFieldType(FieldType.TEXT_AREA);
                case RICH_TEXT_AREA -> m.setFieldType(FieldType.RICH_TEXT_AREA);
                case UPLOAD -> m.setFieldType(FieldType.UPLOAD);
                case DATE_PICKER -> m.setFieldType(FieldType.DATE_PICKER);
                case TAGS -> m.setFieldType(FieldType.TAGS);
                case REFERENCE_TYPES -> m.setFieldType(FieldType.REFERENCE_TYPES);
                case DATASET_IDENTIFIER -> m.setFieldType(FieldType.DATASET_IDENTIFIER);
                case VALIDATION -> m.setFieldType(FieldType.VALIDATION);
                default -> throw new MyApplicationException("unrecognized type " + d.getFieldType());
            }
            this.buildChild(d, m);
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
