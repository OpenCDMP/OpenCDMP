package org.opencdmp.data;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "\"Installation\"")
public class InstallationEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "share_usage", nullable = false)
    private Boolean shareUsage;
    public static final String _shareUsage = "shareUsage";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getShareUsage() {
        return shareUsage;
    }

    public void setShareUsage(Boolean shareUsage) {
        this.shareUsage = shareUsage;
    }
}
