package org.opencdmp.integrationevent.inbox;

import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorProperties;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxPrincipal implements MyPrincipal, ClaimAccessor {
	private final Map<String, Object> claims;

	private final boolean isAuthenticated;

	public InboxPrincipal(Boolean isAuthenticated, String name) {
		this.claims = new HashMap<>();
		this.put(JwtClaimNames.SUB, name);
		this.isAuthenticated = isAuthenticated;
	}

	public static InboxPrincipal build(IntegrationEventProperties properties, ClaimExtractorProperties claimExtractorProperties) {
		InboxPrincipal inboxPrincipal = new InboxPrincipal(true, "IntegrationEventQueueAppId");
		List<ClaimExtractorProperties.KeyPath> clientKey = claimExtractorProperties.getMapping().getOrDefault("Client",  null);
		inboxPrincipal.put(clientKey != null && clientKey.getFirst() != null ? clientKey.getFirst().getType() : "client_id", properties.getAppId());
		inboxPrincipal.put("active", "true");
		List<ClaimExtractorProperties.KeyPath> notBeforeKey = claimExtractorProperties.getMapping().getOrDefault("NotBefore",  null);
		inboxPrincipal.put(notBeforeKey != null && notBeforeKey.getFirst() != null ? notBeforeKey.getFirst().getType() :"nbf", Instant.now().minus(30, ChronoUnit.SECONDS).toString());
		List<ClaimExtractorProperties.KeyPath> expiresAt = claimExtractorProperties.getMapping().getOrDefault("ExpiresAt",  null);
		inboxPrincipal.put(expiresAt != null && expiresAt.getFirst() != null ? expiresAt.getFirst().getType() :"exp", Instant.now().plus(10, ChronoUnit.MINUTES).toString());
		return inboxPrincipal;
	}

	@Override
	public Boolean isAuthenticated() {
		return this.isAuthenticated;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.claims;
	}

	@Override
	public List<String> getClaimAsStringList(String claim) {
		if (claims == null)
			return null;
		if (this.claims.containsKey(claim)){
			return List.of(this.claims.get(claim).toString());
		}
		return null;
	}

	@Override
	public String getName() {
		return this.getClaimAsString(JwtClaimNames.SUB);
	}

	public void put(String key, Object value) {
		this.claims.put(key, value);
	}
}
