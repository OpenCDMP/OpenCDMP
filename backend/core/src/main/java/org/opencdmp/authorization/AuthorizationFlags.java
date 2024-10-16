package org.opencdmp.authorization;

import java.util.EnumSet;

public enum AuthorizationFlags {
	None, Permission, PlanAssociated, Public, Owner, DescriptionTemplateAssociated;
	public static final EnumSet<AuthorizationFlags> AllExceptPublic = EnumSet.of(PlanAssociated, Permission, Owner, DescriptionTemplateAssociated);
	public static final EnumSet<AuthorizationFlags> All = EnumSet.of(PlanAssociated, Permission, Owner, Public);
}
