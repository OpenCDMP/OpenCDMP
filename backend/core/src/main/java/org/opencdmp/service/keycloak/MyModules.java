package org.opencdmp.service.keycloak;

import org.keycloak.admin.client.resource.RealmResource;

public class MyModules {

    public static MyUsersModule getUsersModule(RealmResource realm) {
        return new MyUsersModule(realm);
    }

}
