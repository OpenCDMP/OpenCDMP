package org.opencdmp.commons.users;

public class EmailExpirationTimeSeconds {

    private int mergeAccountExpiration;

    private int removeCredentialExpiration;

    private int tenantSpecificInvitationExpiration;

    private int planInvitationExternalUserExpiration;

    public int getMergeAccountExpiration() {
        return mergeAccountExpiration;
    }

    public void setMergeAccountExpiration(int mergeAccountExpiration) {
        this.mergeAccountExpiration = mergeAccountExpiration;
    }

    public int getRemoveCredentialExpiration() {
        return removeCredentialExpiration;
    }

    public void setRemoveCredentialExpiration(int removeCredentialExpiration) {
        this.removeCredentialExpiration = removeCredentialExpiration;
    }

    public int getTenantSpecificInvitationExpiration() {
        return tenantSpecificInvitationExpiration;
    }

    public void setTenantSpecificInvitationExpiration(int tenantSpecificInvitationExpiration) {
        this.tenantSpecificInvitationExpiration = tenantSpecificInvitationExpiration;
    }

    public int getPlanInvitationExternalUserExpiration() {
        return planInvitationExternalUserExpiration;
    }

    public void setPlanInvitationExternalUserExpiration(int planInvitationExternalUserExpiration) {
        this.planInvitationExternalUserExpiration = planInvitationExternalUserExpiration;
    }
}
