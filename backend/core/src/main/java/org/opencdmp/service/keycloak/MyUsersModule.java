package org.opencdmp.service.keycloak;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public class MyUsersModule {

    private final RealmResource realm;

    MyUsersModule(RealmResource realm) {
        this.realm = realm;
    }

    public UserRepresentation findUserById(String id) {
        return this.realm.users().get(id).toRepresentation();
    }

    public List<UserRepresentation> findUsersByUsername(String username) {
        return this.realm.users().search(username);
    }

    public UserRepresentation findUserByUsername(String username) {
        return this.realm.users().search(username, true).stream().findFirst().orElse(null);
    }

    public UserRepresentation addUser(UserRepresentation user) {
        return this.realm.users().create(user).readEntity(UserRepresentation.class);
    }

    public void addUserToGroup(String userId, String groupId) {
        this.realm.users().get(userId).joinGroup(groupId);
    }

    public void removeUserFromGroup(String userId, String groupId) {
        this.realm.users().get(userId).leaveGroup(groupId);
    }

    public List<GroupRepresentation> getGroups(String userId) {
        return this.realm.users().get(userId).groups();
    }

    public double getUserSessionsCountByClientId(String clientId) {
        return this.realm.clients().get(clientId).getUserSessions(0, Integer.MAX_VALUE).size();
    }

    public double getAllUsersCount() {
        return this.realm.users().count();
    }

    public void updateUser(String userId, UserRepresentation user) {
        UserRepresentation existing = this.realm.users().get(userId).toRepresentation();
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEnabled(user.isEnabled());
        existing.setAttributes(user.getAttributes());
        existing.setClientRoles(user.getClientRoles());
        this.realm.users().get(userId).update(existing);
    }
}
