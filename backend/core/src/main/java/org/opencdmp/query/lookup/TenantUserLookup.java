package org.opencdmp.query.lookup;


import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.TenantUserQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class TenantUserLookup extends Lookup {

	@Schema(description = SwaggerHelpers.TenantUser.isActive_description)
	private List<IsActive> isActive;

	@Schema(description = SwaggerHelpers.TenantUser.ids_description)
	private List<UUID> ids;

	@Schema(description = SwaggerHelpers.TenantUser.userIds_description)
	private List<UUID> userIds;

	@Schema(description = SwaggerHelpers.TenantUser.tenantIds_description)
	private List<UUID> tenantIds;

	private UserLookup userSubQuery;

	public List<IsActive> getIsActive() {
		return isActive;
	}

	public void setIsActive(List<IsActive> isActive) {
		this.isActive = isActive;
	}

	public List<UUID> getIds() {
		return ids;
	}

	public void setIds(List<UUID> ids) {
		this.ids = ids;
	}

	public List<UUID> getUserIds() { return userIds; }

	public void setUserIds(List<UUID> userIds) { this.userIds = userIds; }

	public List<UUID> getTenantIds() {
		return tenantIds;
	}

	public void setTenantIds(List<UUID> tenantIds) {
		this.tenantIds = tenantIds;
	}

	public UserLookup getUserSubQuery() {
		return userSubQuery;
	}

	public void setUserSubQuery(UserLookup userSubQuery) {
		this.userSubQuery = userSubQuery;
	}

	public TenantUserQuery enrich(QueryFactory queryFactory) {
		TenantUserQuery query = queryFactory.query(TenantUserQuery.class);
		if (this.isActive != null) query.isActive(this.isActive);
		if (this.ids != null) query.ids(this.ids);
		if (this.userIds != null) query.userIds(this.userIds);
		if (this.tenantIds != null) query.tenantIds(this.tenantIds);
		if (this.userSubQuery != null) query.userSubQuery(this.userSubQuery.enrich(queryFactory));

		this.enrichCommon(query);

		return query;
	}
}
