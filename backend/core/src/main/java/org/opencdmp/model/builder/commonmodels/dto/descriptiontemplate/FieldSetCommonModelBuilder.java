package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptiontemplate.FieldSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.FieldSetCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldSetCommonModelBuilder extends BaseCommonModelBuilder<FieldSet, FieldSetModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public FieldSetCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldSetCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public FieldSetCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<FieldSet, FieldSetModel>> buildInternal(List<FieldSetModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<FieldSet, FieldSetModel>> models = new ArrayList<>();
        for (FieldSetModel d : data) {
            FieldSet m = new FieldSet();
            m.setId(d.getId());
            m.setOrdinal(d.getOrdinal());
            m.setTitle(d.getTitle());
            m.setDescription(d.getDescription());
            m.setExtendedDescription(d.getExtendedDescription());
            m.setAdditionalInformation(d.getAdditionalInformation());
            m.setHasMultiplicity(d.isHasMultiplicity());
            if (d.getMultiplicity() != null) m.setMultiplicity(this.builderFactory.builder(MultiplicityCommonModelBuilder.class).authorize(this.authorize).build(d.getMultiplicity()));
            if (d.getFields() != null) m.setFields(this.builderFactory.builder(FieldCommonModelBuilder.class).authorize(this.authorize).build(d.getFields()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
