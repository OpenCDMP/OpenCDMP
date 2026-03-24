package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.DescriptionTemplateTypeCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.DescriptionTemplateCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateCommonModelBuilder extends BaseCommonModelBuilder<DescriptionTemplate, DescriptionTemplateModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    @Autowired
    public DescriptionTemplateCommonModelBuilder(
		    ConventionService conventionService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTemplateCommonModelBuilder.class)));
	    this.xmlHandlingService = xmlHandlingService;
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public DescriptionTemplateCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DescriptionTemplate, DescriptionTemplateModel>> buildInternal(List<DescriptionTemplateModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DescriptionTemplate, DescriptionTemplateModel>> models = new ArrayList<>();
        for (DescriptionTemplateModel d : data) {
            DescriptionTemplate m = new DescriptionTemplate();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setGroupId(d.getGroupId());
            m.setVersion(d.getVersion());
            m.setLanguage(d.getLanguage());
            m.setIsActive(IsActive.Active);
            m.setDefinition(this.builderFactory.builder(DefinitionCommonModelBuilder.class).authorize(this.authorize).build(d.getDefinition()));
            m.setType(this.builderFactory.builder(DescriptionTemplateTypeCommonModelBuilder.class).authorize(this.authorize).build(d.getType()));
            
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
