package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.RecentActivityOrder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.lookup.*;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.fieldset.BaseFieldSet;

import java.util.List;
import java.util.UUID;

public class RecentActivityItemLookup{

    private String like;
    private Boolean onlyDraft;
    private Boolean onlyPlan;
    private Boolean onlyDescription;
    private List<UUID> userIds;
    private Paging page;
    private BaseFieldSet project;
    private RecentActivityOrder orderField;

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

    public Paging getPage() {
        return page;
    }

    public void setPage(Paging page) {
        this.page = page;
    }

    public BaseFieldSet getProject() {
        return project;
    }

    public void setProject(BaseFieldSet project) {
        this.project = project;
    }

    public RecentActivityOrder getOrderField() {
        return orderField;
    }

    public void setOrderField(RecentActivityOrder orderField) {
        this.orderField = orderField;
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
        if (this.page != null)  lookup.setPage(new Paging(this.page.getOffset(), this.page.getSize()));
        Ordering ordering = new Ordering();
        if (this.orderField != null) {
            switch (this.orderField){
                case Label -> ordering.addDescending(Description._label).addDescending(Description._updatedAt);
                case UpdatedAt -> ordering.addDescending(Description._updatedAt);
                case Status -> ordering.addDescending(Description._status).addDescending(Description._updatedAt);
                default -> throw  new IllegalArgumentException("Type not found" + this.orderField) ;
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
        if (this.page != null)  lookup.setPage(new Paging(this.page.getOffset(), this.page.getSize()));
        Ordering ordering = new Ordering();
        if (this.orderField != null) {
            switch (this.orderField){
                case Label -> ordering.addDescending(Plan._label);
                case UpdatedAt -> ordering.addDescending(Plan._updatedAt);
                case Status -> ordering.addDescending(Plan._status).addDescending(Plan._updatedAt);
                default -> throw  new IllegalArgumentException("Type not found" + this.orderField) ;
            }
        } else {
            ordering.addDescending(Plan._updatedAt);
        }
        lookup.setOrder(ordering);
        if (this.project !=null)lookup.setProject((BaseFieldSet) this.project.extractPrefixed(RecentActivityItem._plan));
        return lookup;
    }

}
