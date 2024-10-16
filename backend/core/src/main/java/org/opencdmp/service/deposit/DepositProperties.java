package org.opencdmp.service.deposit;

import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "deposit")
public class DepositProperties {

    private List<DepositSourceEntity> sources;

    public List<DepositSourceEntity> getSources() {
        return sources;
    }

    public void setSources(List<DepositSourceEntity> sources) {
        this.sources = sources;
    }
}
