package org.opencdmp.service.updatecheck;

public class UpdateCheckUsage {

    private long tenants;
    private long users;
    private long plans;
    private long descriptions;
    private String javaVersion;
    private String databaseVersion;

    public long getTenants() {
        return tenants;
    }

    public void setTenants(long tenants) {
        this.tenants = tenants;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getPlans() {
        return plans;
    }

    public void setPlans(long plans) {
        this.plans = plans;
    }

    public long getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(long descriptions) {
        this.descriptions = descriptions;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }
}
