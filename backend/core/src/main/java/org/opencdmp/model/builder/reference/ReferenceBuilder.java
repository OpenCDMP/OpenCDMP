package org.opencdmp.model.builder.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.builder.planreference.PlanReferenceBuilder;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.PlanReferenceQuery;
import org.opencdmp.query.ReferenceTypeQuery;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceBuilder extends BaseBuilder<Reference, ReferenceEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
	    this.tenantScope = tenantScope;
    }

    public ReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Reference> build(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //ToDo FieldSet userInfoFields = fields.extractPrefixed(this.asPrefix(Reference._createdBy));
        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(Reference._definition));

        FieldSet planReferencesFields = fields.extractPrefixed(this.asPrefix(Reference._planReferences));
        Map<UUID, List<PlanReference>> planReferenceMap = this.collectPlanReferences(planReferencesFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Reference._createdBy));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        FieldSet typeFields = fields.extractPrefixed(this.asPrefix(Reference._type));
        Map<UUID, ReferenceType> typeItemsMap = this.collectReferenceTypes(typeFields, data);

        List<Reference> models = new ArrayList<>();
        for (ReferenceEntity d : data) {
            Reference m = new Reference();
            if (fields.hasField(this.asIndexer(Reference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Reference._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Reference._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Reference._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Reference._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(Reference._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (!definitionFields.isEmpty() && d.getDefinition() != null){
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            if (fields.hasField(this.asIndexer(Reference._reference))) m.setReference(d.getReference());
            if (fields.hasField(this.asIndexer(Reference._abbreviation))) m.setAbbreviation(d.getAbbreviation());
            if (fields.hasField(this.asIndexer(Reference._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Reference._source))) m.setSource(d.getSource());
            if (fields.hasField(this.asIndexer(Reference._sourceType))) m.setSourceType(d.getSourceType());
            if (!typeFields.isEmpty() && typeItemsMap != null && typeItemsMap.containsKey(d.getTypeId())) m.setType(typeItemsMap.get(d.getTypeId()));
            if (planReferenceMap != null && !planReferenceMap.isEmpty() && planReferenceMap.containsKey(d.getId())) m.setPlanReferences(planReferenceMap.get(d.getId()));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getCreatedById())) m.setCreatedBy(userItemsMap.get(d.getCreatedById()));
            if (fields.hasField(this.asIndexer(Reference._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, ReferenceType> collectReferenceTypes(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceType.class.getSimpleName());

        Map<UUID, ReferenceType> itemMap;
        if (!fields.hasOtherField(this.asIndexer(ReferenceType._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        ReferenceType item = new ReferenceType();
                        item.setId(x);
                        return item;
                    },
                    ReferenceType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(ReferenceType._id);
            ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, ReferenceType::getId);
        }
        if (!fields.hasField(ReferenceType._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(ReferenceEntity::getCreatedById).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(ReferenceEntity::getCreatedById).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
        }
        if (!fields.hasField(User._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PlanReference>> collectPlanReferences(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanReference.class.getSimpleName());

        Map<UUID, List<PlanReference>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanReference._reference, Reference._id));
        PlanReferenceQuery query = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().authorize(this.authorize).referenceIds(data.stream().map(ReferenceEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanReferenceBuilder.class).authorize(this.authorize).authorize(this.authorize).asMasterKey(query, clone, x -> x.getReference().getId());

        if (!fields.hasField(this.asIndexer(PlanReference._reference, Reference._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getReference() != null).map(x -> {
                x.getReference().setId(null);
                return x;
            }).collect(Collectors.toList());
        }
        return itemMap;
    }
}
