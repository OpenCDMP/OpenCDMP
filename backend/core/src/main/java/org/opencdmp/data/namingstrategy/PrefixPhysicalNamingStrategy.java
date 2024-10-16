package org.opencdmp.data.namingstrategy;

import org.opencdmp.convention.ConventionService;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties({NamingStrategyProperties.class})
public class PrefixPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final NamingStrategyProperties namingStrategyProperties;
    private final ConventionService conventionService;

    public PrefixPhysicalNamingStrategy(NamingStrategyProperties namingStrategyProperties, ConventionService conventionService) {
        this.namingStrategyProperties = namingStrategyProperties;
	    this.conventionService = conventionService;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        if (conventionService.isNullOrEmpty(namingStrategyProperties.getPrefix()))
            return super.toPhysicalTableName(logicalName, context);
        Identifier identifier = new Identifier(namingStrategyProperties.getPrefix() + logicalName.getText(), logicalName.isQuoted());
        return super.toPhysicalTableName(identifier, context);
    }

}
