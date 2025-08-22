package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.pluginconfiguration.PluginConfigurationUserBuilder;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.user.UserAdditionalInfo;
import org.opencdmp.query.ReferenceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserAdditionalInfoBuilder extends BaseBuilder<UserAdditionalInfo, AdditionalInfoEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserAdditionalInfoBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserAdditionalInfoBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public UserAdditionalInfoBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserAdditionalInfo> build(FieldSet fields, List<AdditionalInfoEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceFields = fields.extractPrefixed(this.asPrefix(UserAdditionalInfo._organization));
        Map<UUID, Reference> referenceItemsMap = this.collectReferences(referenceFields, data);

        FieldSet pluginConfigurationFields = fields.extractPrefixed(this.asPrefix(UserAdditionalInfo._pluginConfigurations));
        List<UserAdditionalInfo> models = new ArrayList<>();

        for (AdditionalInfoEntity d : data) {
            UserAdditionalInfo m = new UserAdditionalInfo();
            if (fields.hasField(this.asIndexer(UserAdditionalInfo._language))) m.setLanguage(d.getLanguage());
            if (fields.hasField(this.asIndexer(UserAdditionalInfo._culture))) m.setCulture(d.getCulture());
            if (fields.hasField(this.asIndexer(UserAdditionalInfo._avatarUrl))) m.setAvatarUrl(d.getAvatarUrl());
            if (fields.hasField(this.asIndexer(UserAdditionalInfo._timezone))) m.setTimezone(d.getTimezone());
            if (!referenceFields.isEmpty() && referenceItemsMap != null && referenceItemsMap.containsKey(d.getOrganizationId())) m.setOrganization(referenceItemsMap.get(d.getOrganizationId()));
            if (fields.hasField(this.asIndexer(UserAdditionalInfo._roleOrganization))) m.setRoleOrganization(d.getRoleOrganization());
            if (!pluginConfigurationFields.isEmpty() && d.getPluginConfigurations() != null) m.setPluginConfigurations(this.builderFactory.builder(PluginConfigurationUserBuilder.class).authorize(this.authorize).build(pluginConfigurationFields, d.getPluginConfigurations()));

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }



    private Map<UUID, Reference> collectReferences(FieldSet fields, List<AdditionalInfoEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Reference.class.getSimpleName());

        Map<UUID, Reference> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Reference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(AdditionalInfoEntity::getOrganizationId).filter(Objects::nonNull).distinct().collect(Collectors.toList()),
                    x -> {
                        Reference item = new Reference();
                        item.setId(x);
                        return item;
                    },
                    Reference::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Reference._id);
            ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(AdditionalInfoEntity::getOrganizationId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Reference::getId);
        }
        if (!fields.hasField(Reference._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
