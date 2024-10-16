package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReferenceTypeFieldPersist extends FieldPersist {

    private UUID referenceTypeId;

    public static final String _referenceTypeId = "referenceTypeId";

    private Boolean multipleSelect;

    public static final String _multipleSelect = "multipleSelect";

    
    public UUID getReferenceTypeId() {
        return this.referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public Boolean getMultipleSelect() {
        return this.multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    @Component(ReferenceFieldPersistPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceFieldPersistPersistValidator extends BaseFieldPersistValidator<ReferenceTypeFieldPersist> {

        public static final String ValidatorName = "PlanBlueprint.ReferenceFieldPersistPersistValidator";

        protected ReferenceFieldPersistPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<ReferenceTypeFieldPersist> modelClass() {
            return ReferenceTypeFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferenceTypeFieldPersist item) {
            List<Specification> specifications = this.getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getReferenceTypeId()))
                            .failOn(ReferenceTypeFieldPersist._referenceTypeId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeFieldPersist._referenceTypeId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMultipleSelect()))
                            .failOn(ReferenceTypeFieldPersist._multipleSelect).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeFieldPersist._multipleSelect}, LocaleContextHolder.getLocale())))
                    
            );
            return specifications;
        }
    }

}
