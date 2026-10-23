package org.opencdmp.commons.types.entitydoi;

import org.opencdmp.commons.enums.DepositAuthMethod;

import java.util.UUID;

public class EntityDoiMetadataEntity {

    private DepositAuthMethod depositMethod;

    private UUID userId;

    private String accountId;

    private String accountName;

    public DepositAuthMethod getDepositMethod() {
        return depositMethod;
    }

    public void setDepositMethod(DepositAuthMethod depositMethod) {
        this.depositMethod = depositMethod;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
