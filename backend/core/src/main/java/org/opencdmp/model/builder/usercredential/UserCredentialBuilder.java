package org.opencdmp.model.builder.usercredential;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.types.usercredential.UserCredentialDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredential;
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
public class UserCredentialBuilder extends BaseBuilder<UserCredential, UserCredentialEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserCredentialBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserCredentialBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.jsonHandlingService = jsonHandlingService;
    }

    public UserCredentialBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserCredential> build(FieldSet fields, List<UserCredentialEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(UserCredential._user));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);
        
        FieldSet definitionPropertiesFields = fields.extractPrefixed(this.asPrefix(UserCredential._data));

        List<UserCredential> models = new ArrayList<>();

        for (UserCredentialEntity d : data) {
            UserCredential m = new UserCredential();
            if (fields.hasField(this.asIndexer(UserCredential._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(UserCredential._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(UserCredential._externalId))) m.setExternalId(d.getExternalId());
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
            if (!definitionPropertiesFields.isEmpty() && d.getData() != null){
                UserCredentialDataEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(UserCredentialDataEntity.class, d.getData());
                m.setData(this.builderFactory.builder(UserCredentialDataBuilder.class).authorize(this.authorize).build(definitionPropertiesFields, propertyDefinition));
            }
            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<UserCredentialEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(UserCredentialEntity::getUserId).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(UserCredentialEntity::getUserId).distinct().collect(Collectors.toList()));
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

}
