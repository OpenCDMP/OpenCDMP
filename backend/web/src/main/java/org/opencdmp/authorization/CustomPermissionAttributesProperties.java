package org.opencdmp.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.HashMap;

@ConfigurationProperties(prefix = "permissions")
@ConditionalOnProperty(prefix = "permissions", name = "enabled", havingValue = "true")
public class CustomPermissionAttributesProperties {

	private final HashMap<String, MyPermission> policies;

	@ConstructorBinding
	public CustomPermissionAttributesProperties(HashMap<String, MyPermission> policies) {
		this.policies = policies;
	}

	public HashMap<String, MyPermission> getPolicies() {
		return this.policies;
	}

	public static class MyPermission {

		private final PlanRole plan;
		private final DescriptionTemplateRole descriptionTemplate;

		@ConstructorBinding
		public MyPermission(PlanRole plan, DescriptionTemplateRole descriptionTemplate) {
			this.plan = plan;
			this.descriptionTemplate = descriptionTemplate;
		}


		public PlanRole getPlan() {
			return this.plan;
		}

		public DescriptionTemplateRole getDescriptionTemplate() {
			return this.descriptionTemplate;
		}
	}

}
