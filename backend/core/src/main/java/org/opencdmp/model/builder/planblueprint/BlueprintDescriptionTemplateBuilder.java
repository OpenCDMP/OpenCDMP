package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.types.planblueprint.BlueprintDescriptionTemplateEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.BlueprintDescriptionTemplate;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("planblueprintdefinitiondescriptiontemplatebuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BlueprintDescriptionTemplateBuilder extends BaseBuilder<BlueprintDescriptionTemplate, BlueprintDescriptionTemplateEntity> {

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public BlueprintDescriptionTemplateBuilder(
            ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(BlueprintDescriptionTemplateBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public BlueprintDescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<BlueprintDescriptionTemplate> build(FieldSet fields, List<BlueprintDescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet descriptionTemplateFields= fields.extractPrefixed(this.asPrefix(BlueprintDescriptionTemplate._descriptionTemplate));
        Map<UUID, org.opencdmp.model.descriptiontemplate.DescriptionTemplate> descriptionTemplateItemsMap = this.collectDescriptionTemplates(descriptionTemplateFields, data);

        List<BlueprintDescriptionTemplate> models = new ArrayList<>();
        for (BlueprintDescriptionTemplateEntity d : data) {
            BlueprintDescriptionTemplate m = new BlueprintDescriptionTemplate();
            if (!descriptionTemplateFields.isEmpty() && descriptionTemplateItemsMap != null && descriptionTemplateItemsMap.containsKey(d.getDescriptionTemplateGroupId())) m.setDescriptionTemplate(descriptionTemplateItemsMap.get(d.getDescriptionTemplateGroupId()));
            if (fields.hasField(this.asIndexer(BlueprintDescriptionTemplate._maxMultiplicity))) m.setMaxMultiplicity(d.getMaxMultiplicity());
            if (fields.hasField(this.asIndexer(BlueprintDescriptionTemplate._minMultiplicity))) m.setMinMultiplicity(d.getMinMultiplicity());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, org.opencdmp.model.descriptiontemplate.DescriptionTemplate> collectDescriptionTemplates(FieldSet fields, List<BlueprintDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", org.opencdmp.model.descriptiontemplate.DescriptionTemplate.class.getSimpleName());

        Map<UUID, org.opencdmp.model.descriptiontemplate.DescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(org.opencdmp.model.descriptiontemplate.DescriptionTemplate._groupId))) {
            itemMap = this.asEmpty(
                    data.stream().map(BlueprintDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()),
                    x -> {
                        org.opencdmp.model.descriptiontemplate.DescriptionTemplate item = new org.opencdmp.model.descriptiontemplate.DescriptionTemplate();
                        item.setGroupId(x);
                        return item;
                    },
                    org.opencdmp.model.descriptiontemplate.DescriptionTemplate::getGroupId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(org.opencdmp.model.descriptiontemplate.DescriptionTemplate._groupId);
            DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).versionStatuses(DescriptionTemplateVersionStatus.Current).statuses(DescriptionTemplateStatus.Finalized).groupIds(data.stream().map(BlueprintDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, org.opencdmp.model.descriptiontemplate.DescriptionTemplate::getGroupId);
        }
        if (!fields.hasField(org.opencdmp.model.descriptiontemplate.DescriptionTemplate._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
