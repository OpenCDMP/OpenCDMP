package org.opencdmp.model.entitydoi;

import org.opencdmp.commons.enums.DepositAuthMethod;

import java.util.UUID;

public class EntityDoiMetadata {

    private DepositAuthMethod depositMethod;
    public static final String _depositMethod = "depositMethod";

    private UUID userId;
    public static final String _userId = "userId";

    private String accountId;
    public static final String _accountId = "accountId";

    private String accountName;
    public static final String _accountName = "accountName";

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
