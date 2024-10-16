package org.opencdmp.model.builder.commonmodels.descriptiontemplate;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.MultiplicityModel;
import org.opencdmp.commons.types.descriptiontemplate.MultiplicityEntity;
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MultiplicityCommonModelBuilder extends BaseCommonModelBuilder<MultiplicityModel, MultiplicityEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public MultiplicityCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(MultiplicityCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public MultiplicityCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<MultiplicityModel, MultiplicityEntity>> buildInternal(List<MultiplicityEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<MultiplicityModel, MultiplicityEntity>> models = new ArrayList<>();
        for (MultiplicityEntity d : data) {
            MultiplicityModel m = new MultiplicityModel();
            m.setMin(d.getMin());
            m.setMax(d.getMax());
            m.setPlaceholder(d.getPlaceholder());
            m.setTableView(d.getTableView());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
