package org.opencdmp.authorization;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.service.deposit.DepositServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PermissionNameProvider {
	private static final Logger logger = LoggerFactory.getLogger(PermissionNameProvider.class);
	private final List<String> permissions;

	public PermissionNameProvider(ConventionService conventionService) {
		this.permissions = new ArrayList<>();
		Class<Permission> clazz = Permission.class;
		for (Field f : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) {
				try {
					Object value = f.get(null);
					if (value != null && !conventionService.isNullOrEmpty((String)value)) this.permissions.add((String)value);
				} catch (Exception e) {
					logger.error("Can not load permission " + f.getName() + " " + e.getMessage());
				}
			}
		}
	}

	public List<String> getPermissions() {
		return permissions;
	}
}
