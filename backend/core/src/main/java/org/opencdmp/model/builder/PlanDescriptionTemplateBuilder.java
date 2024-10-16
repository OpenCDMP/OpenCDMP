package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanDescriptionTemplateEntity;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanDescriptionTemplateBuilder extends BaseBuilder<PlanDescriptionTemplate, PlanDescriptionTemplateEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private final TenantScope tenantScope;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanDescriptionTemplateBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanDescriptionTemplateBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.tenantScope = tenantScope;
    }

    public PlanDescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanDescriptionTemplate> build(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet templateFields = fields.extractPrefixed(this.asPrefix(PlanDescriptionTemplate._descriptionTemplates));
        Map<UUID, List<DescriptionTemplate>> templateItemsMap = this.collectDescriptionTemplates(templateFields, data);

        FieldSet currentDescriptionTemplateFields = fields.extractPrefixed(this.asPrefix(PlanDescriptionTemplate._currentDescriptionTemplate));
        Map<UUID, DescriptionTemplate> currentDescriptionTemplateItemsMap = this.collectCurrentDescriptionTemplates(currentDescriptionTemplateFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PlanDescriptionTemplate._plan));
        Map<UUID, Plan> planItemsMap = this.collectPlans(planFields, data);

        List<PlanDescriptionTemplate> models = new ArrayList<>();
        for (PlanDescriptionTemplateEntity d : data) {
            PlanDescriptionTemplate m = new PlanDescriptionTemplate();
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._sectionId))) m.setSectionId(d.getSectionId());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._descriptionTemplateGroupId))) m.setDescriptionTemplateGroupId(d.getDescriptionTemplateGroupId());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._createdAt)))  m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanDescriptionTemplate._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!templateFields.isEmpty() && templateItemsMap != null && templateItemsMap.containsKey(d.getDescriptionTemplateGroupId())) m.setDescriptionTemplates(templateItemsMap.get(d.getDescriptionTemplateGroupId()));
            if (!currentDescriptionTemplateFields.isEmpty() && currentDescriptionTemplateItemsMap != null && currentDescriptionTemplateItemsMap.containsKey(d.getDescriptionTemplateGroupId())) m.setCurrentDescriptionTemplate(currentDescriptionTemplateItemsMap.get(d.getDescriptionTemplateGroupId()));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, Plan> collectPlans(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, Plan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Plan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanDescriptionTemplateEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        Plan item = new Plan();
                        item.setId(x);
                        return item;
                    },
                    Plan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanDescriptionTemplateEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Plan::getId);
        }
        if (!fields.hasField(Plan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

    private Map<UUID, List<DescriptionTemplate>> collectDescriptionTemplates(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplate.class.getSimpleName());

        Map<UUID, List<DescriptionTemplate>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionTemplate._groupId);
        DescriptionTemplateQuery query = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).groupIds(data.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(this.authorize).asMasterKey(query, clone, DescriptionTemplate::getGroupId);

        if (!fields.hasField(DescriptionTemplate._groupId)) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getGroupId() != null).forEach(x -> {
                x.setGroupId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, DescriptionTemplate> collectCurrentDescriptionTemplates(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplate.class.getSimpleName());

        Map<UUID, DescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(DescriptionTemplate._groupId))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()),
                    x -> {
                        DescriptionTemplate item = new DescriptionTemplate();
                        item.setGroupId(x);
                        return item;
                    },
                    DescriptionTemplate::getGroupId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionTemplate._id, DescriptionTemplate._groupId);
            DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).groupIds(data.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, DescriptionTemplate::getGroupId);
        }
        if (!fields.hasField(DescriptionTemplate._groupId)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setGroupId(null));
        }

        return itemMap;
    }

}
