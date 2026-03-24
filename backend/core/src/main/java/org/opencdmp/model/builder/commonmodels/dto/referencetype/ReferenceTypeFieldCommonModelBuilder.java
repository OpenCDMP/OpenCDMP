package org.opencdmp.model.builder.commonmodels.dto.referencetype;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeFieldModel;
import org.opencdmp.commons.enums.ReferenceFieldDataType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.referencetype.ReferenceTypeField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.ReferenceTypeFieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeFieldCommonModelBuilder extends BaseCommonModelBuilder<ReferenceTypeField, ReferenceTypeFieldModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeFieldCommonModelBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeFieldCommonModelBuilder.class)));
    }

    public ReferenceTypeFieldCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<ReferenceTypeField, ReferenceTypeFieldModel>> buildInternal(List<ReferenceTypeFieldModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<ReferenceTypeField, ReferenceTypeFieldModel>> models = new ArrayList<>();
        for (ReferenceTypeFieldModel d : data) {
            ReferenceTypeField m = new ReferenceTypeField();
            m.setCode(d.getCode());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            switch (d.getDataType()){
                case Text -> m.setDataType(ReferenceFieldDataType.Text);
                case Date -> m.setDataType(ReferenceFieldDataType.Date);
                case null -> m.setDataType(null);
                default -> throw new MyApplicationException("unrecognized type " + d.getDataType());
            }
            m.setSemantics(d.getSemantics());
            m.setRequired(d.getRequired());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
