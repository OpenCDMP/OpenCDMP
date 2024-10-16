package org.opencdmp.model.censorship;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.TenantUser;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantUserCensor extends BaseCensor {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantUserCensor.class));

	protected final AuthorizationService authService;
	protected final CensorFactory censorFactory;

	@Autowired
	public TenantUserCensor(
			ConventionService conventionService,
			AuthorizationService authService,
			CensorFactory censorFactory
	) {
		super(conventionService);
		this.authService = authService;
		this.censorFactory = censorFactory;
	}

	public void censor(FieldSet fields, UUID userId) throws MyForbiddenException {
		logger.debug(new DataLogEntry("censoring fields", fields));
		if (this.isEmpty(fields)) return;
		this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null, Permission.BrowseTenantUser);
		FieldSet tenantFields = fields.extractPrefixed(this.asIndexerPrefix(TenantUser._tenant));
		this.censorFactory.censor(TenantCensor.class).censor(tenantFields, null);
		FieldSet userFields = fields.extractPrefixed(this.asIndexerPrefix(TenantUser._user));
		this.censorFactory.censor(UserCensor.class).censor(userFields, userId);
	}
}
