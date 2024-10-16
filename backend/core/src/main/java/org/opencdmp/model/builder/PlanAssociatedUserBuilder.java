package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.PlanAssociatedUser;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.user.User;
import org.opencdmp.query.UserContactInfoQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanAssociatedUserBuilder extends BaseBuilder<PlanAssociatedUser, UserEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanAssociatedUserBuilder(ConventionService conventionService,
                                     QueryFactory queryFactory,
                                     BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanAssociatedUserBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public PlanAssociatedUserBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanAssociatedUser> build(FieldSet fields, List<UserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanAssociatedUser> models = new ArrayList<>();

        Map<UUID, List<UserContactInfo>> contactsMap = this.collectUserContactInfos(new BaseFieldSet().ensure(UserContactInfo._value).ensure(UserContactInfo._type).ensure(UserContactInfo._ordinal), data);

        for (UserEntity d : data) {
            PlanAssociatedUser m = new PlanAssociatedUser();
            if (fields.hasField(this.asIndexer(User._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(User._name))) m.setName(d.getName());
            if (contactsMap != null && contactsMap.containsKey(d.getId())){
                List<UserContactInfo> contactInfos = contactsMap.get(d.getId());
                if (contactInfos != null) {
                    contactInfos.sort(Comparator.comparing(UserContactInfo::getOrdinal));
                    m.setEmail(contactInfos.stream().filter(x -> ContactInfoType.Email.equals(x.getType())).map(UserContactInfo::getValue).findFirst().orElse(null));
                }
            }
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

}
