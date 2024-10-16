package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.UserRole;
import org.opencdmp.model.builder.usercredential.UserCredentialBuilder;
import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.query.TenantUserQuery;
import org.opencdmp.query.UserContactInfoQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.UserRoleQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserBuilder extends BaseBuilder<User, UserEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private final JsonHandlingService jsonHandlingService;
    private final AuthorizationConfiguration authorizationConfiguration;
    private final TenantScope tenantScope;


    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserBuilder(ConventionService conventionService,
                       QueryFactory queryFactory,
                       BuilderFactory builderFactory, JsonHandlingService jsonHandlingService, AuthorizationConfiguration authorizationConfiguration, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.jsonHandlingService = jsonHandlingService;
	    this.authorizationConfiguration = authorizationConfiguration;
	    this.tenantScope = tenantScope;
    }

    public UserBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<User> build(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<User> models = new ArrayList<>();

        FieldSet contactsFields = fields.extractPrefixed(this.asPrefix(User._contacts));
        Map<UUID, List<UserContactInfo>> contactsMap = this.collectUserContactInfos(contactsFields, data);

        FieldSet globalRolesFields = fields.extractPrefixed(this.asPrefix(User._globalRoles));
        Map<UUID, List<UserRole>> globalRolesMap = this.collectUserGlobalRoles(globalRolesFields, data);

        FieldSet tenantRolesFields = fields.extractPrefixed(this.asPrefix(User._tenantRoles));
        Map<UUID, List<UserRole>> tenantRolesMap = this.collectUserTenantRoles(tenantRolesFields, data);

        FieldSet credentialsFields = fields.extractPrefixed(this.asPrefix(User._credentials));
        Map<UUID, List<UserCredential>> credentialsMap = this.collectUserCredentials(credentialsFields, data);

        FieldSet additionalInfoFields = fields.extractPrefixed(this.asPrefix(User._additionalInfo));

        FieldSet tenantUsersFields = fields.extractPrefixed(this.asPrefix(User._tenantUsers));
        Map<UUID, List<TenantUser>> tenantUsersMap = this.collectTenantUsers(tenantUsersFields, data);
        for (UserEntity d : data) {
            User m = new User();
            if (fields.hasField(this.asIndexer(User._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(User._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(User._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(User._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(User._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(User._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (contactsMap != null && !contactsFields.isEmpty() && contactsMap.containsKey(d.getId())) m.setContacts(contactsMap.get(d.getId()));
            if (globalRolesMap != null && !globalRolesFields.isEmpty() && globalRolesMap.containsKey(d.getId())) m.setGlobalRoles(globalRolesMap.get(d.getId()));
            if (tenantRolesMap != null && !tenantRolesFields.isEmpty() && tenantRolesMap.containsKey(d.getId())) m.setTenantRoles(tenantRolesMap.get(d.getId()));
            if (credentialsMap != null && !credentialsFields.isEmpty() && credentialsMap.containsKey(d.getId())) m.setCredentials(credentialsMap.get(d.getId()));
            if (!additionalInfoFields.isEmpty() && d.getAdditionalInfo() != null){
                AdditionalInfoEntity definition = this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, d.getAdditionalInfo());
                m.setAdditionalInfo(this.builderFactory.builder(UserAdditionalInfoBuilder.class).authorize(this.authorize).build(additionalInfoFields, definition));
            }
            if (!tenantUsersFields.isEmpty() && tenantUsersMap != null && tenantUsersMap.containsKey(d.getId())) m.setTenantUsers(tenantUsersMap.get(d.getId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, List<UserContactInfo>> collectUserContactInfos(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", UserContactInfo.class.getSimpleName());

        Map<UUID, List<UserContactInfo>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(UserContactInfo._user, User._id));
        UserContactInfoQuery query = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().authorize(this.authorize).userIds(data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserContactInfoBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getUser().getId());

        if (!fields.hasField(this.asIndexer(UserContactInfo._user, User._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getUser() != null).forEach(x -> {
                x.getUser().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<UserRole>> collectUserGlobalRoles(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", UserRole.class.getSimpleName());

        Map<UUID, List<UserRole>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(UserRole._user, User._id));
        UserRoleQuery query = this.queryFactory.query(UserRoleQuery.class).disableTracking().authorize(this.authorize).tenantIsSet(false).roles(this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()).userIds(data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserRoleBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getUser().getId());

        if (!fields.hasField(this.asIndexer(UserRole._user, User._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getUser() != null).forEach(x -> {
                x.getUser().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<UserRole>> collectUserTenantRoles(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", UserRole.class.getSimpleName());
        Map<UUID, List<UserRole>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(UserRole._user, User._id));

        if (!this.tenantScope.isSet())  throw new MyForbiddenException("tenant scope required");
            
        UserRoleQuery query = this.queryFactory.query(UserRoleQuery.class).disableTracking().authorize(this.authorize).roles(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()).userIds(data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList()));
        if (this.tenantScope.isDefaultTenant()) query.tenantIsSet(false);
        else {
            try {
                query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            }
        }

        itemMap = this.builderFactory.builder(UserRoleBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getUser().getId());

        if (!fields.hasField(this.asIndexer(UserRole._user, User._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getUser() != null).forEach(x -> {
                x.getUser().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<UserCredential>> collectUserCredentials(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", UserCredential.class.getSimpleName());

        Map<UUID, List<UserCredential>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(UserCredential._user, User._id));
        UserCredentialQuery query = this.queryFactory.query(UserCredentialQuery.class).disableTracking().authorize(this.authorize).userIds(data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserCredentialBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getUser().getId());

        if (!fields.hasField(this.asIndexer(UserCredential._user, User._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getUser() != null).forEach(x -> {
                x.getUser().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<TenantUser>> collectTenantUsers(FieldSet fields, List<UserEntity> datas) throws MyApplicationException {
        if (fields.isEmpty() || datas.isEmpty()) return null;
        this.logger.debug("checking related - {}", TenantUser.class.getSimpleName());

        Map<UUID, List<TenantUser>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(TenantUser._user, User._id));
        TenantUserQuery query = this.queryFactory.query(TenantUserQuery.class).disableTracking().authorize(this.authorize).userIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(TenantUserBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getUser().getId());

        if (!fields.hasField(this.asIndexer(TenantUser._user, User._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getUser() != null).forEach(x -> {
                x.getUser().setId(null);
            });
        }
        return itemMap;
    }

}
