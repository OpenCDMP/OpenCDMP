package org.opencdmp.model.builder.commonmodels.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.DescriptionTemplateTypeModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.DescriptionTemplateTypeCommonModelBuilder;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateCommonModelBuilder extends BaseCommonModelBuilder<DescriptionTemplateModel, DescriptionTemplateEntity> {

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

    private boolean useSharedStorage;
    public DescriptionTemplateCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    private String repositoryId;
    public DescriptionTemplateCommonModelBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DescriptionTemplateModel, DescriptionTemplateEntity>> buildInternal(List<DescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Map<UUID, DescriptionTemplateTypeModel> typeMap = this.collectDescriptionTemplateTypes(data);
        List<CommonModelBuilderItemResponse<DescriptionTemplateModel, DescriptionTemplateEntity>> models = new ArrayList<>();
        for (DescriptionTemplateEntity d : data) {
            DescriptionTemplateModel m = new DescriptionTemplateModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setGroupId(d.getGroupId());
            m.setVersion(d.getVersion());
            m.setLanguage(d.getLanguage());
            if (d.getDefinition() != null){
                //TODO Update with the new logic of property definition 
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionCommonModelBuilder.class).useSharedStorage(useSharedStorage).setRepositoryId(repositoryId).authorize(this.authorize).build(definition));
            }
            if (typeMap != null && d.getTypeId() != null && typeMap.containsKey(d.getTypeId())) m.setType(typeMap.get(d.getTypeId()));
            
            
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }




    private Map<UUID, DescriptionTemplateTypeModel> collectDescriptionTemplateTypes(List<DescriptionTemplateEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplateTypeModel.class.getSimpleName());

        Map<UUID, DescriptionTemplateTypeModel> itemMap;
        DescriptionTemplateTypeQuery q = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionTemplateEntity::getTypeId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionTemplateTypeCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, DescriptionTemplateTypeEntity::getId);

        return itemMap;
    }

}
