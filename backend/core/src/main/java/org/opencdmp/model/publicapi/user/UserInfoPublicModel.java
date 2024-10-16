package org.opencdmp.model.publicapi.user;

import org.opencdmp.model.PlanUser;
import org.opencdmp.model.user.User;

import java.util.UUID;

public class UserInfoPublicModel {
    private UUID id;
    private String name;
    private int role;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getRole() {
        return role;
    }
    public void setRole(int role) {
        this.role = role;
    }

    public static UserInfoPublicModel fromDmpUser(PlanUser planUser) {
        UserInfoPublicModel model = new UserInfoPublicModel();
        model.setId(planUser.getUser().getId());
        model.setName(planUser.getUser().getName());
        model.setRole(planUser.getRole().getValue());
        return model;
    }

    public static UserInfoPublicModel fromDescriptionCreator(User user) {
        UserInfoPublicModel model = new UserInfoPublicModel();
        model.setId(user.getId());
        model.setName(user.getName());
        model.setRole(0);
        return model;
    }

    public String getHint() {
        return "UserInfoListingModel";
    }
}
