package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionSectionPermissionResolver {

    private UUID planId;
    public static final String _planId = "planId";

    private List<UUID> sectionIds;

    public static final String _sectionIds = "sectionIds";

    private List<String> permissions;

    public static final String _permissions = "permissions";

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public List<UUID> getSectionIds() {
        return sectionIds;
    }

    public void setSectionIds(List<UUID> sectionIds) {
        this.sectionIds = sectionIds;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    @Component(DescriptionSectionPermissionResolverPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionSectionPermissionResolverPersistValidator extends BaseValidator<DescriptionSectionPermissionResolver> {

        public static final String ValidatorName = "DescriptionSectionPermissionResolverPersistValidator";

        private final MessageSource messageSource;

        protected DescriptionSectionPermissionResolverPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionSectionPermissionResolver> modelClass() {
            return DescriptionSectionPermissionResolver.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionSectionPermissionResolver item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(DescriptionSectionPermissionResolver._planId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionSectionPermissionResolver._planId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> item.getPermissions() != null && !item.getPermissions().isEmpty())
                            .failOn(DescriptionSectionPermissionResolver._permissions).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionSectionPermissionResolver._permissions}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> item.getSectionIds() != null && !item.getSectionIds().isEmpty())
                            .failOn(DescriptionSectionPermissionResolver._sectionIds).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionSectionPermissionResolver._sectionIds}, LocaleContextHolder.getLocale()))

                    );
        }
    }

}
