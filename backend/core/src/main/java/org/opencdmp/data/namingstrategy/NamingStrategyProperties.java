package org.opencdmp.data.namingstrategy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "naming-strategy")
public class NamingStrategyProperties {

    private final String prefix;

    @ConstructorBinding
    public NamingStrategyProperties(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

}
