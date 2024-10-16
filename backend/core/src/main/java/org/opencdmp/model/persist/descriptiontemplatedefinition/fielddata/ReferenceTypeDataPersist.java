package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

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

public class ReferenceTypeDataPersist extends BaseFieldDataPersist {

    private Boolean multipleSelect;

    public static final String _multipleSelect = "multipleSelect";

    private UUID referenceTypeId;
    public final static String _referenceTypeId = "referenceTypeId";

    public Boolean getMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    @Component(ReferenceTypeDataPersist.ReferenceTypeDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceTypeDataPersistValidator extends BaseFieldDataPersistValidator<ReferenceTypeDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.ReferenceTypeDataPersistValidator";

        protected ReferenceTypeDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<ReferenceTypeDataPersist> modelClass() {
            return ReferenceTypeDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferenceTypeDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getReferenceTypeId()))
                            .failOn(ReferenceTypeDataPersist._referenceTypeId).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeDataPersist._referenceTypeId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMultipleSelect()))
                            .failOn(ReferenceTypeDataPersist._multipleSelect).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeDataPersist._multipleSelect}, LocaleContextHolder.getLocale()))
            ));
            return specifications;
        }
    }

}
