package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.UserDescriptionTemplate;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.DescriptionTemplateTypeBuilder;
import org.opencdmp.model.builder.UserDescriptionTemplateBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import org.opencdmp.query.UserDescriptionTemplateQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateBuilder extends BaseBuilder<DescriptionTemplate, DescriptionTemplateEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private final AuthorizationService authorizationService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private Map<UUID, Integer> featuredOrdinalMap;

    @Autowired
    public DescriptionTemplateBuilder(
		    ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope, AuthorizationService authorizationService, AuthorizationContentResolver authorizationContentResolver) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTemplateBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
	    this.tenantScope = tenantScope;
	    this.authorizationService = authorizationService;
	    this.authorizationContentResolver = authorizationContentResolver;
    }

    public DescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionTemplateBuilder featuredOrdinalMap(Map<UUID, Integer> featuredOrdinalMap) {
        this.featuredOrdinalMap = featuredOrdinalMap;
        return this;
    }

    @Override
    public List<DescriptionTemplate> build(FieldSet fields, List<DescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet descriptionTemplateTypeFields = fields.extractPrefixed(this.asPrefix(DescriptionTemplate._type));
        Map<UUID, DescriptionTemplateType> descriptionTemplateTypeMap = this.collectDescriptionTemplateTypes(descriptionTemplateTypeFields, data);

        FieldSet usersFields = fields.extractPrefixed(this.asPrefix(DescriptionTemplate._users));
        Map<UUID, List<UserDescriptionTemplate>> usersMap = this.collectUserDescriptionTemplates(usersFields, data);

        Set<String> authorizationFlags = this.extractAuthorizationFlags(fields, Plan._authorizationFlags, this.authorizationContentResolver.getPermissionNames());
        Map<UUID, AffiliatedResource>  affiliatedResourceMap = authorizationFlags == null || authorizationFlags.isEmpty() ? null : this.authorizationContentResolver.descriptionTemplateAffiliation(data.stream().map(DescriptionTemplateEntity::getId).collect(Collectors.toList()));

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(DescriptionTemplate._definition));
        List<DescriptionTemplate> models = new ArrayList<>();
        for (DescriptionTemplateEntity d : data) {
            DescriptionTemplate m = new DescriptionTemplate();
            if (fields.hasField(this.asIndexer(DescriptionTemplate._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._label)))
                m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._code)))
                m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._description)))
                m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._groupId)))
                m.setGroupId(d.getGroupId());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._version)))
                m.setVersion(d.getVersion());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._language)))
                m.setLanguage(d.getLanguage());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._versionStatus)))
                m.setVersionStatus(d.getVersionStatus());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionTemplate._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            if (!usersFields.isEmpty() && usersMap != null && usersMap.containsKey(d.getId()))
                m.setUsers(usersMap.get(d.getId()));
            if (!descriptionTemplateTypeFields.isEmpty() && descriptionTemplateTypeMap != null && descriptionTemplateTypeMap.containsKey(d.getTypeId()))
                m.setType(descriptionTemplateTypeMap.get(d.getTypeId()));
            if (affiliatedResourceMap != null && !authorizationFlags.isEmpty()) m.setAuthorizationFlags(this.evaluateAuthorizationFlags(this.authorizationService, authorizationFlags, affiliatedResourceMap.getOrDefault(d.getId(), null)));
            if (fields.hasField(this.asIndexer(DescriptionTemplate._ordinal)) && this.featuredOrdinalMap != null && this.featuredOrdinalMap.containsKey(d.getGroupId())) {
                m.setOrdinal(this.featuredOrdinalMap.get(d.getGroupId()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, DescriptionTemplateType> collectDescriptionTemplateTypes(FieldSet fields, List<DescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplateType.class.getSimpleName());

        Map<UUID, DescriptionTemplateType> itemMap = null;
        if (!fields.hasOtherField(this.asIndexer(DescriptionTemplateType._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionTemplateEntity::getTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        DescriptionTemplateType item = new DescriptionTemplateType();
                        item.setId(x);
                        return item;
                    },
		            DescriptionTemplateType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionTemplateType._id);
            DescriptionTemplateTypeQuery q = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().ids(data.stream().map(DescriptionTemplateEntity::getTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionTemplateTypeBuilder.class).asForeignKey(q, clone, DescriptionTemplateType::getId);
        }
        if (!fields.hasField(DescriptionTemplateType._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> {
                x.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<UserDescriptionTemplate>> collectUserDescriptionTemplates(FieldSet fields, List<DescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", UserDescriptionTemplate.class.getSimpleName());

        Map<UUID, List<UserDescriptionTemplate>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(UserDescriptionTemplate._descriptionTemplate, DescriptionTemplate._id));
        UserDescriptionTemplateQuery query = this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).descriptionTemplateIds(data.stream().map(DescriptionTemplateEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserDescriptionTemplateBuilder.class).authorize(this.authorize).authorize(this.authorize).asMasterKey(query, clone, x -> x.getDescriptionTemplate().getId());

        if (!fields.hasField(this.asIndexer(UserDescriptionTemplate._descriptionTemplate, DescriptionTemplate._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getDescriptionTemplate() != null).forEach(x -> {
                x.getDescriptionTemplate().setId(null);
            });
        }
        return itemMap;
    }
}
