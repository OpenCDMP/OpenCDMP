package org.opencdmp.authorization;

import org.opencdmp.commons.scope.user.UserScope;
import gr.cite.commons.web.authz.handler.AuthorizationHandler;
import gr.cite.commons.web.authz.handler.AuthorizationHandlerContext;
import gr.cite.commons.web.authz.policy.AuthorizationRequirement;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ownedAuthorizationHandler")
public class OwnedAuthorizationHandler extends AuthorizationHandler<OwnedAuthorizationRequirement> {

	private final UserScope userScope;

	@Autowired
	public OwnedAuthorizationHandler(UserScope userScope) {
		this.userScope = userScope;
	}

	@Override
	public int handleRequirement(AuthorizationHandlerContext context, Object resource, AuthorizationRequirement requirement) {
		OwnedAuthorizationRequirement req = (OwnedAuthorizationRequirement) requirement;

		OwnedResource rs = (OwnedResource) resource;

		boolean isAuthenticated = ((MyPrincipal) context.getPrincipal()).isAuthenticated();
		if (!isAuthenticated) return ACCESS_NOT_DETERMINED;

		if (this.userScope.getUserIdSafe() == null) return ACCESS_NOT_DETERMINED;

		if (rs != null && rs.getUserIds() != null && rs.getUserIds().contains(this.userScope.getUserIdSafe())) return ACCESS_GRANTED;

		return ACCESS_NOT_DETERMINED;
	}

	@Override
	public Class<? extends AuthorizationRequirement> supporting() {
		return OwnedAuthorizationRequirement.class;
	}

}

