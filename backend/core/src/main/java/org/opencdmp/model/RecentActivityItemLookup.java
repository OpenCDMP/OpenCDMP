package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.RecentActivityOrder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.lookup.*;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public class RecentActivityItemLookup{

    private String like;
    private Boolean onlyDraft;
    private Boolean onlyPlan;
    private Boolean onlyDescription;
    private List<UUID> userIds;
    private List<UUID> statusIds;
    /**
     * The My Plans filter panel's lookup. Only its filter parts are applied (blueprints, templates,
     * roles, references); paging, order, projection and the "my plans" restriction stay the dashboard's.
     */
    private PlanLookup planFilter;
    /** The My Assets filter panel's lookup, applied the same way as {@link #planFilter}. */
    private DescriptionLookup descriptionFilter;
    private Paging page;
    private BaseFieldSet project;
    private Order order;

    public static class Order {

        private RecentActivityOrder orderField;

        private Sort.Direction sortDirection;

        public RecentActivityOrder getOrderField() {
            return orderField;
        }

        public void setOrderField(RecentActivityOrder orderField) {
            this.orderField = orderField;
        }

        public Sort.Direction getSortDirection() {
            return sortDirection;
        }

        public void setSortDirection(Sort.Direction sortDirection) {
            this.sortDirection = sortDirection;
        }
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public Boolean getOnlyDraft() {
        return onlyDraft;
    }

    public void setOnlyDraft(Boolean onlyDraft) {
        this.onlyDraft = onlyDraft;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public List<UUID> getStatusIds() {
        return statusIds;
    }

    public void setStatusIds(List<UUID> statusIds) {
        this.statusIds = statusIds;
    }

    public Paging getPage() {
        return page;
    }

    public void setPage(Paging page) {
        this.page = page;
    }

    public PlanLookup getPlanFilter() {
        return planFilter;
    }

    public void setPlanFilter(PlanLookup planFilter) {
        this.planFilter = planFilter;
    }

    public DescriptionLookup getDescriptionFilter() {
        return descriptionFilter;
    }

    public void setDescriptionFilter(DescriptionLookup descriptionFilter) {
        this.descriptionFilter = descriptionFilter;
    }

    public BaseFieldSet getProject() {
        return project;
    }

    public void setProject(BaseFieldSet project) {
        this.project = project;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Boolean getOnlyPlan() {
        return onlyPlan;
    }

    public void setOnlyPlan(Boolean onlyPlan) {
        this.onlyPlan = onlyPlan;
    }

    public Boolean getOnlyDescription() {
        return onlyDescription;
    }

    public void setOnlyDescription(Boolean onlyDescription) {
        this.onlyDescription = onlyDescription;
    }

    public DescriptionLookup asDescriptionLookup() {
        if (this.onlyPlan != null) return null;

        DescriptionLookup lookup = new DescriptionLookup();
        lookup.setIsActive(List.of(IsActive.Active));
        if (this.like != null) lookup.setLike(this.like);
        if (this.onlyDraft != null) {
            DescriptionStatusLookup descriptionStatusLookup = new DescriptionStatusLookup();
            descriptionStatusLookup.setInternalStatuses(List.of(DescriptionStatus.Draft));
            descriptionStatusLookup.setIsActive(List.of(IsActive.Active));
            lookup.setDescriptionStatusSubQuery(descriptionStatusLookup);
        }
        if (this.userIds != null) {
            PlanLookup planLookup = new PlanLookup();
            PlanUserLookup planUserLookup = new PlanUserLookup();
            planUserLookup.setUserIds(this.userIds);
            planUserLookup.setIsActive(List.of(IsActive.Active));
            planLookup.setPlanUserSubQuery(planUserLookup);
            planLookup.setIsActive(List.of(IsActive.Active));
            lookup.setPlanSubQuery(planLookup);
        }
        if (this.statusIds != null) {
            lookup.setStatusIds(this.statusIds);
        }
        this.applyDescriptionFilter(lookup);
        if (this.page != null)  lookup.setPage(new Paging(this.page.getOffset(), this.page.getSize()));
        Ordering ordering = new Ordering();
        if (this.order != null && this.order.getOrderField() != null) {
            switch (this.order.getOrderField()){
                case Label -> this.applySortField(ordering, Description._label);
                case UpdatedAt -> this.applySortField(ordering, Description._updatedAt);
                case Status -> this.applySortField(ordering, Description._status);
                default -> throw  new IllegalArgumentException("Type not found" + this.order.getOrderField()) ;
            }
        } else {
            ordering.addDescending(Description._updatedAt);
        }
        lookup.setOrder(ordering);
        if (this.project !=null) lookup.setProject((BaseFieldSet) this.project.extractPrefixed(RecentActivityItem._description));
        return lookup;
    }

    public PlanLookup asPlanLookup() {
        if (this.onlyDescription != null) return null;

        PlanLookup lookup = new PlanLookup();
        lookup.setIsActive(List.of(IsActive.Active));
        if (this.like != null) lookup.setLike(this.like);
        if (this.onlyDraft != null) {
            PlanStatusLookup planStatusLookup = new PlanStatusLookup();
            planStatusLookup.setInternalStatuses(List.of(PlanStatus.Draft));
            planStatusLookup.setIsActive(List.of(IsActive.Active));
            lookup.setPlanStatusSubQuery(planStatusLookup);
        }
        if (this.userIds != null) {
            PlanUserLookup planUserLookup = new PlanUserLookup();
            planUserLookup.setUserIds(this.userIds);
            planUserLookup.setIsActive(List.of(IsActive.Active));
            lookup.setPlanUserSubQuery(planUserLookup);
        }
        if (this.statusIds != null) {
            PlanStatusLookup planStatusLookup = new PlanStatusLookup();
            planStatusLookup.setIds(this.statusIds);
            planStatusLookup.setIsActive(List.of(IsActive.Active));
            lookup.setPlanStatusSubQuery(planStatusLookup);
        }
        this.applyPlanFilter(lookup);
        if (this.page != null)  lookup.setPage(new Paging(this.page.getOffset(), this.page.getSize()));
        Ordering ordering = new Ordering();
        if (this.order != null && this.order.getOrderField() != null) {
            switch (this.order.getOrderField()){
                case Label -> this.applySortField(ordering, Plan._label);
                case UpdatedAt -> this.applySortField(ordering, Plan._updatedAt);
                case Status -> this.applySortField(ordering, Plan._status);
                default -> throw  new IllegalArgumentException("Type not found" + this.order.getOrderField()) ;
            }
        } else {
            ordering.addDescending(Plan._updatedAt);
        }
        lookup.setOrder(ordering);
        if (this.project !=null)lookup.setProject((BaseFieldSet) this.project.extractPrefixed(RecentActivityItem._plan));
        return lookup;
    }

    /** Copies the panel's narrowing sub-queries onto the dashboard's plan lookup; nothing that widens it. */
    private void applyPlanFilter(PlanLookup lookup) {
        if (this.planFilter == null) return;
        if (this.planFilter.getPlanBlueprintSubQuery() != null) lookup.setPlanBlueprintLookup(this.planFilter.getPlanBlueprintSubQuery());
        if (this.planFilter.getPlanDescriptionTemplateSubQuery() != null) lookup.setPlanDescriptionTemplateSubQuery(this.planFilter.getPlanDescriptionTemplateSubQuery());
        if (this.planFilter.getPlanReferenceSubQuery() != null) lookup.setPlanReferenceLookup(this.planFilter.getPlanReferenceSubQuery());
        // Roles narrow the dashboard's own "plans I am a member of" sub-query instead of replacing it.
        List<PlanUserRole> roles = this.planFilter.getPlanUserSubQuery() != null ? this.planFilter.getPlanUserSubQuery().getUserRoles() : null;
        if (roles != null && !roles.isEmpty() && lookup.getPlanUserSubQuery() != null) lookup.getPlanUserSubQuery().setUserRoles(roles);
    }

    /** Copies the panel's narrowing sub-queries onto the dashboard's description lookup; nothing that widens it. */
    private void applyDescriptionFilter(DescriptionLookup lookup) {
        if (this.descriptionFilter == null) return;
        if (this.descriptionFilter.getDescriptionTemplateSubQuery() != null) lookup.setDescriptionTemplateSubQuery(this.descriptionFilter.getDescriptionTemplateSubQuery());
        if (this.descriptionFilter.getDescriptionTagSubQuery() != null) lookup.setDescriptionTagSubQuery(this.descriptionFilter.getDescriptionTagSubQuery());
        if (this.descriptionFilter.getDescriptionReferenceSubQuery() != null) lookup.setDescriptionReferenceSubQuery(this.descriptionFilter.getDescriptionReferenceSubQuery());

        PlanLookup filterPlan = this.descriptionFilter.getPlanSubQuery();
        if (filterPlan == null) return;
        PlanLookup plan = lookup.getPlanSubQuery();
        if (plan == null) {
            plan = new PlanLookup();
            plan.setIsActive(List.of(IsActive.Active));
            lookup.setPlanSubQuery(plan);
        }
        if (filterPlan.getIds() != null && !filterPlan.getIds().isEmpty()) plan.setIds(filterPlan.getIds());
        List<PlanUserRole> roles = filterPlan.getPlanUserSubQuery() != null ? filterPlan.getPlanUserSubQuery().getUserRoles() : null;
        if (roles != null && !roles.isEmpty() && plan.getPlanUserSubQuery() != null) plan.getPlanUserSubQuery().setUserRoles(roles);
    }

    private void applySortField(Ordering ordering, String field) {
        if (this.getOrder().getSortDirection() == null) ordering.addDescending(field);
        else switch (this.getOrder().getSortDirection()){
            case ASC -> ordering.addAscending(field);
            case DESC -> ordering.addDescending(field);
            default -> throw  new IllegalArgumentException("Type not found" + this.getOrder().getSortDirection()) ;
        }
    }

}
