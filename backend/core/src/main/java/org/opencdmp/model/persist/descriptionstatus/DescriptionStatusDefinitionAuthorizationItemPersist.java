package org.opencdmp.model.persist.descriptionstatus;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionAuthorizationItemPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DescriptionStatusDefinitionAuthorizationItemPersist {

    private List<String> roles = null;
    public final static String _roles = "roles";

    private List<PlanUserRole> planRoles = null;
    public final static String _planRoles = "planRoles";

    private Boolean allowAuthenticated;
    public final static String _allowAuthenticated = "allowAuthenticated";

    private Boolean allowAnonymous;
    public final static String _allowAnonymous = "allowAnonymous";

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<PlanUserRole> getPlanRoles() { return planRoles; }
    public void setPlanRoles(List<PlanUserRole> planRoles) { this.planRoles = planRoles; }

    public Boolean getAllowAuthenticated() { return allowAuthenticated; }
    public void setAllowAuthenticated(Boolean allowAuthenticated) { this.allowAuthenticated = allowAuthenticated; }

    public Boolean getAllowAnonymous() { return allowAnonymous; }
    public void setAllowAnonymous(Boolean allowAnonymous) { this.allowAnonymous = allowAnonymous; }

    @Component(DescriptionStatusDefinitionAuthorizationItemPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusDefinitionAuthorizationItemPersistValidator extends BaseValidator<DescriptionStatusDefinitionAuthorizationItemPersist> {

        public static final String ValidatorName = "DescriptionStatusPersistValidation.DescriptionStatusDefinitionPersistValidator.DescriptionStatusDefinitionAuthorizationPersistValidator.DescriptionStatusDefinitionAuthorizationItemPersistValidator";

        private final MessageSource messageSource;

        protected DescriptionStatusDefinitionAuthorizationItemPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionStatusDefinitionAuthorizationItemPersist> modelClass() {
            return DescriptionStatusDefinitionAuthorizationItemPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusDefinitionAuthorizationItemPersist item) {
            return Arrays.asList(
            );
        }
    }
}
