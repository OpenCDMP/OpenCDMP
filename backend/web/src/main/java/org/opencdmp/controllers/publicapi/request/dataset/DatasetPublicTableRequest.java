package org.opencdmp.controllers.publicapi.request.dataset;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.criteria.dataset.DatasetPublicCriteria;
import org.opencdmp.controllers.publicapi.query.definition.TableQuery;
import org.opencdmp.controllers.publicapi.types.FieldSelectionType;
import org.opencdmp.controllers.publicapi.types.SelectionField;
import org.opencdmp.data.DescriptionEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;


public class DatasetPublicTableRequest extends TableQuery<DatasetPublicCriteria, DescriptionEntity, UUID> {
    @Override
    public QueryableList<DescriptionEntity> applyCriteria() {
        QueryableList<DescriptionEntity> query = this.getQuery();
        query.where((builder, root) -> builder.equal(root.get("dmp").get("isPublic"), true));
        query.where((builder, root) -> builder.equal(root.get("status"), DescriptionStatus.Finalized));
//        query.initSubQuery(String.class).where((builder, root) -> builder.equal(root.get("dmp").get("version"),
//                query.<String>subQueryMax((builder1, externalRoot, nestedRoot) -> builder1.equal(externalRoot.get("dmp").get("groupId"), nestedRoot.get("dmp").get("groupId")),
//                        Arrays.asList(new SelectionField(FieldSelectionType.COMPOSITE_FIELD, "dmp:version")), String.class)));
        if (this.getCriteria().getLike() != null && !this.getCriteria().getLike().isEmpty())
            query.where((builder, root) -> builder.or(
                    builder.like(builder.upper(root.get("label")), "%" + this.getCriteria().getLike().toUpperCase() + "%"),
                    builder.like(builder.upper(root.get("description")), "%" + this.getCriteria().getLike().toUpperCase() + "%")));
        if (this.getCriteria().getPeriodStart() != null)
            query.where((builder, root) -> builder.greaterThan(root.get("created"), this.getCriteria().getPeriodStart()));
        if (this.getCriteria().getPeriodEnd() != null)
            query.where((builder, root) -> builder.lessThan(root.get("created"), this.getCriteria().getPeriodEnd()));
        if (this.getCriteria().getGrants() != null && !this.getCriteria().getGrants().isEmpty())
            query.where(((builder, root) -> root.get("dmp").get("grant").get("id").in(this.getCriteria().getGrants())));
        if (this.getCriteria().getCollaborators() != null && !this.getCriteria().getCollaborators().isEmpty())
            query.where(((builder, root) -> root.join("dmp").join("researchers").get("id").in(this.getCriteria().getCollaborators())));
        if (this.getCriteria().getDatasetTemplates() != null && !this.getCriteria().getDatasetTemplates().isEmpty())
            query.where((builder, root) -> root.get("id").in(this.getCriteria().getDatasetTemplates()));
        if (this.getCriteria().getDmpOrganisations() != null && !this.getCriteria().getDmpOrganisations().isEmpty())
            query.where(((builder, root) -> root.join("dmp").join("organisations").get("reference").in(this.getCriteria().getDmpOrganisations())));
        if (this.getCriteria().getDmpIds() != null && !this.getCriteria().getDmpIds().isEmpty()) {
            query.where(((builder, root) -> root.get("dmp").get("id").in(this.getCriteria().getDmpIds())));
        }
        if (this.getCriteria().getGroupIds() != null && !this.getCriteria().getGroupIds().isEmpty()) {
            query.where((builder, root) -> root.get("dmp").get("groupId").in(this.getCriteria().getGroupIds()));
        }

        //query.where((builder, root) -> builder.lessThan(root.get("dmp").get("grant").get("enddate"), new Date())); // GrantStateType.FINISHED
        query.where((builder, root) ->
                builder.or(builder.greaterThan(root.get("dmp").get("grant").get("enddate"), new Date())
                        , builder.isNull(root.get("dmp").get("grant").get("enddate")))); // GrantStateType.ONGOING

        if (!this.getCriteria().getAllVersions()) {
            query.initSubQuery(String.class).where((builder, root) -> builder.equal(root.get("profile").get("version"),
                    query.<String>subQueryMax((builder1, externalRoot, nestedRoot) -> builder1.and(builder1.equal(externalRoot.get("profile").get("groupId"),
                            nestedRoot.get("profile").get("groupId")), builder1.equal(nestedRoot.get("dmp").get("isPublic"), true)), Arrays.asList(new SelectionField(FieldSelectionType.COMPOSITE_FIELD, "profile:version")), String.class)));
        }
        query.where((builder, root) -> builder.notEqual(root.get("isActive"), IsActive.Inactive));
        return query;
    }

    @Override
    public QueryableList<DescriptionEntity> applyPaging(QueryableList<DescriptionEntity> items) {
        return null;
    }
}
