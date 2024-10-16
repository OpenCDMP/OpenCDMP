package org.opencdmp.controllers.publicapi.request.plan;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.criteria.plan.DataManagementPlanPublicCriteria;
import org.opencdmp.controllers.publicapi.query.PaginationService;
import org.opencdmp.controllers.publicapi.query.definition.TableQuery;
import org.opencdmp.controllers.publicapi.types.FieldSelectionType;
import org.opencdmp.controllers.publicapi.types.SelectionField;
import org.opencdmp.data.PlanEntity;

import jakarta.persistence.criteria.Predicate;
import java.util.*;

public class DataManagmentPlanPublicTableRequest extends TableQuery<DataManagementPlanPublicCriteria, PlanEntity, UUID> {

    public QueryableList<PlanEntity> applyCriteria() {
        QueryableList<PlanEntity> query = this.getQuery();
        query.where((builder, root) -> builder.equal(root.get("isPublic"), true));
        if (this.getCriteria().getLike() != null && !this.getCriteria().getLike().isEmpty())
            query.where((builder, root) -> builder.or(
                    builder.like(builder.upper(root.get("label")), "%" + this.getCriteria().getLike().toUpperCase() + "%"),
                    builder.like(builder.upper(root.get("description")), "%" + this.getCriteria().getLike().toUpperCase() + "%")));
        if (this.getCriteria().getPeriodStart() != null)
            query.where((builder, root) -> builder.greaterThan(root.get("created"), this.getCriteria().getPeriodStart()));
        if (this.getCriteria().getPeriodEnd() != null)
            query.where((builder, root) -> builder.lessThan(root.get("created"), this.getCriteria().getPeriodEnd()));
        if (this.getCriteria().getGrants() != null && !this.getCriteria().getGrants().isEmpty())
            query.where(((builder, root) -> root.get("grant").get("id").in(this.getCriteria().getGrants())));
        if (this.getCriteria().getGrantsLike() != null && !this.getCriteria().getGrantsLike().isEmpty()) {
            query.where(((builder, root) -> {
                List<Predicate> predicates = new ArrayList<>();
                for(String grantLike: this.getCriteria().getGrantsLike()){
                    String pattern = "%" + grantLike.toUpperCase() + "%";
                    predicates.add(builder.like(builder.upper(root.get("grant").get("label")), pattern));
                    predicates.add(builder.like(builder.upper(root.get("grant").get("abbreviation")), pattern));
                    predicates.add(builder.like(builder.upper(root.get("grant").get("reference")), pattern));
                    predicates.add(builder.like(builder.upper(root.get("grant").get("definition")), pattern));
                    predicates.add(builder.like(builder.upper(root.get("grant").get("description")), pattern));
                }
                return builder.or(predicates.toArray(new Predicate[0]));
            }
            ));
        }
        if (this.getCriteria().getFunders() != null && !this.getCriteria().getFunders().isEmpty())
            query.where(((builder, root) -> root.get("grant").get("funder").get("id").in(this.getCriteria().getFunders())));
        if (this.getCriteria().getFundersLike() != null && !this.getCriteria().getFundersLike().isEmpty()) {
            query.where(((builder, root) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    for(String funderLike: this.getCriteria().getFundersLike()){
                        String pattern = "%" + funderLike.toUpperCase() + "%";
                        predicates.add(builder.like(builder.upper(root.get("grant").get("funder").get("label")), pattern));
                        predicates.add(builder.like(builder.upper(root.get("grant").get("funder").get("reference")), pattern));
                        predicates.add(builder.like(builder.upper(root.get("grant").get("funder").get("definition")), pattern));
                    }
                    return builder.or(predicates.toArray(new Predicate[0]));
                }
            ));
        }

        //query.where((builder, root) -> builder.lessThan(root.get("grant").get("enddate"), new Date())); // GrantStateType.FINISHED
        query.where((builder, root) ->
                builder.or(builder.greaterThan(root.get("grant").get("enddate"), new Date())
                        , builder.isNull(root.get("grant").get("enddate")))); // GrantStateType.ONGOING

        if (this.getCriteria().getDatasetTemplates() != null && !this.getCriteria().getDatasetTemplates().isEmpty())
            query.where((builder, root) -> root.join("dataset").get("id").in(this.getCriteria().getDatasetTemplates()));
        if (this.getCriteria().getDmpOrganisations() != null && !this.getCriteria().getDmpOrganisations().isEmpty())
            query.where(((builder, root) -> root.join("organisations").get("reference").in(this.getCriteria().getDmpOrganisations())));
        if (this.getCriteria().getCollaborators() != null && !this.getCriteria().getCollaborators().isEmpty())
            query.where(((builder, root) -> root.join("researchers").get("id").in(this.getCriteria().getCollaborators())));
        if (this.getCriteria().getCollaboratorsLike() != null && !this.getCriteria().getCollaboratorsLike().isEmpty()) {
            query.where(((builder, root) -> {
                List<Predicate> predicates = new ArrayList<>();
                for(String collaboratorLike: this.getCriteria().getCollaboratorsLike()){
                    String pattern = "%" + collaboratorLike.toUpperCase() + "%";
                    predicates.add(builder.like(builder.upper(root.join("researchers").get("label")), pattern));
                    predicates.add(builder.like(builder.upper(root.join("researchers").get("uri")), pattern));
                    predicates.add(builder.like(builder.upper(root.join("researchers").get("primaryEmail")), pattern));
                }
                return builder.or(predicates.toArray(new Predicate[0]));
            }
            ));
        }
        if (!this.getCriteria().getAllVersions()) {
            query.initSubQuery(String.class).where((builder, root) -> builder.equal(root.get("version"),
                    query.<String>subQueryMax((builder1, externalRoot, nestedRoot) -> builder1.and(builder1.equal(externalRoot.get("groupId"),
                            nestedRoot.get("groupId")), builder1.equal(nestedRoot.get("isPublic"), true)), Arrays.asList(new SelectionField(FieldSelectionType.FIELD, "version")), String.class)));
        }
        if (this.getCriteria().getGroupIds() != null && !this.getCriteria().getGroupIds().isEmpty()) {
            query.where((builder, root) -> root.get("groupId").in(this.getCriteria().getGroupIds()));
        }
        query.where((builder, root) -> builder.notEqual(root.get("isActive"), IsActive.Inactive));
        return query;
    }

    @Override
    public QueryableList<PlanEntity> applyPaging(QueryableList<PlanEntity> items) {
        return PaginationService.applyPaging(items, this);
    }
}
