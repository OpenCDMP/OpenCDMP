package org.opencdmp.model.persist.planstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class PlanStatusDefinitionAuthorizationItemPersist {

    private List<String> roles = null;
    public final static String _roles = "roles";

    private List<PlanUserRole> planRoles = null;
    public final static String _planRoles = "planRoles";

    private Boolean allowAuthenticated;
    public final static String _allowAuthenticated = "allowAuthenticated";

    private Boolean allowAnonymous;
    public final static String _allowAnonymous = "allowAuthenticated";


    public List<String> getRoles() { return this.roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<PlanUserRole> getPlanRoles() { return this.planRoles; }
    public void setPlanRoles(List<PlanUserRole> planRoles) { this.planRoles = planRoles; }

    public Boolean getAllowAuthenticated() { return this.allowAuthenticated; }
    public void setAllowAuthenticated(Boolean allowAuthenticated) { this.allowAuthenticated = allowAuthenticated; }

    public Boolean getAllowAnonymous() { return this.allowAnonymous; }
    public void setAllowAnonymous(Boolean allowAnonymous) { this.allowAnonymous = allowAnonymous; }

    @Component(PlanStatusDefinitionAuthorizationItemPersist.PlanStatusDefinitionAuthorizationItemPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanStatusDefinitionAuthorizationItemPersistValidator extends BaseValidator<PlanStatusDefinitionAuthorizationItemPersist> {

        public static final String ValidatorName = "PlanStatus.PlanStatusDefinitionAuthorizationItemPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanStatusDefinitionAuthorizationItemPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanStatusDefinitionAuthorizationItemPersist> modelClass() {
            return PlanStatusDefinitionAuthorizationItemPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanStatusDefinitionAuthorizationItemPersist item) {
            return Arrays.asList(
            );
        }
    }
}