package org.opencdmp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserTermsAcceptance\"")
public class UserTermsAcceptanceEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "\"user\"", columnDefinition = "uuid", nullable = false)
    private UUID userId;
    public static final String _userId = "userId";

    @Column(name = "tenant", columnDefinition = "uuid", nullable = true)
    private UUID tenantId;
    public static final String _tenantId = "tenantId";

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;
    public static final String _acceptedAt = "acceptedAt";

    @Column(name = "client_ip", length = 100, nullable = true)
    private String clientIp;
    public static final String _clientIp = "clientIp";

    @Column(name = "terms_version", nullable = true)
    private Short termsVersion;
    public static final String _termsVersion = "termsVersion";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Short getTermsVersion() {
        return termsVersion;
    }

    public void setTermsVersion(Short termsVersion) {
        this.termsVersion = termsVersion;
    }
}
