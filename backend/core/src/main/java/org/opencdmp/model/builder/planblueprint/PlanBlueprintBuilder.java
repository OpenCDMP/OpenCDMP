package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.PlanBlueprintTypeBuilder;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.query.PlanBlueprintTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintBuilder extends BaseBuilder<PlanBlueprint, PlanBlueprintEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;

    private final XmlHandlingService xmlHandlingService;
    private final TenantScopeFactory tenantScopeFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private Map<UUID, Integer> featuredOrdinalMap;
    private boolean isPublic;

    @Autowired
    public PlanBlueprintBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService, TenantScopeFactory tenantScopeFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
	    this.tenantScopeFactory = tenantScopeFactory;
    }

    public PlanBlueprintBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintBuilder featuredOrdinalMap(Map<UUID, Integer> featuredOrdinalMap) {
        this.featuredOrdinalMap = featuredOrdinalMap;
        return this;
    }

    public PlanBlueprintBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    public List<PlanBlueprint> build(FieldSet fields, List<PlanBlueprintEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planBlueprintTypeFields = fields.extractPrefixed(this.asPrefix(PlanBlueprint._type));
        Map<UUID, PlanBlueprintType> planBlueprintTypeMap = this.collectPlanBlueprintTypes(planBlueprintTypeFields, data);


        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PlanBlueprint._definition));
        List<PlanBlueprint> models = new ArrayList<>();
        for (PlanBlueprintEntity d : data) {
            PlanBlueprint m = new PlanBlueprint();
            if (fields.hasField(this.asIndexer(PlanBlueprint._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanBlueprint._label)))
                m.setLabel(d.getLabel());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._code)))
                m.setCode(d.getCode());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._status)))
                m.setStatus(d.getStatus());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._groupId)))
                m.setGroupId(d.getGroupId());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._version)))
                m.setVersion(d.getVersion());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._versionStatus)))
                m.setVersionStatus(d.getVersionStatus());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanBlueprint._isActive)))
                m.setIsActive(d.getIsActive());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._description)))
                m.setDescription(d.getDescription());
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).isPublic(this.isPublic).build(definitionFields, definition));
            }
            if (!this.isPublic && !planBlueprintTypeFields.isEmpty() && planBlueprintTypeMap != null && planBlueprintTypeMap.containsKey(d.getTypeId()))
                m.setType(planBlueprintTypeMap.get(d.getTypeId()));
            if (!this.isPublic && fields.hasField(this.asIndexer(PlanBlueprint._ordinal)) && this.featuredOrdinalMap != null && this.featuredOrdinalMap.containsKey(d.getGroupId())) {
                m.setOrdinal(this.featuredOrdinalMap.get(d.getGroupId()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, PlanBlueprintType> collectPlanBlueprintTypes(FieldSet fields, List<PlanBlueprintEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty() || this.isPublic)
            return null;
        this.logger.debug("checking related - {}", PlanBlueprintType.class.getSimpleName());

        Map<UUID, PlanBlueprintType> itemMap = null;
        if (!fields.hasOtherField(this.asIndexer(PlanBlueprintType._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanBlueprintEntity::getTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        PlanBlueprintType item = new PlanBlueprintType();
                        item.setId(x);
                        return item;
                    },
                    PlanBlueprintType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PlanBlueprintType._id);
            PlanBlueprintTypeQuery q = this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().ids(data.stream().map(PlanBlueprintEntity::getTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBlueprintTypeBuilder.class).asForeignKey(q, clone, PlanBlueprintType::getId);
        }
        if (!fields.hasField(PlanBlueprintType._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> {
                x.setId(null);
            });
        }

        return itemMap;
    }
}
