package org.opencdmp.model.persist.descriptiontemplatedefinition;

import org.opencdmp.commons.validation.BaseValidator;
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

public class MultiplicityPersist {

    private Integer min = null;
    public static final String _min = "min";

    private Integer max = null;
    public static final String _max = "max";


    private String placeholder = null;

    private Boolean tableView = null;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Boolean getTableView() {
        return tableView;
    }

    public void setTableView(Boolean tableView) {
        this.tableView = tableView;
    }

    @Component(MultiplicityValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class MultiplicityValidator extends BaseValidator<MultiplicityPersist> {

        public static final String ValidatorName = "DescriptionTemplate.MultiplicityValidator";

        private final MessageSource messageSource;

        protected MultiplicityValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<MultiplicityPersist> modelClass() {
            return MultiplicityPersist.class;
        }

        @Override
        protected List<Specification> specifications(MultiplicityPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> !this.isNull(item.getMin()))
                            .must(() -> item.getMin() >= 0)
                            .failOn(MultiplicityPersist._min).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{MultiplicityPersist._min}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMax()))
                            .must(() -> item.getMax() > 0)
                            .failOn(MultiplicityPersist._max).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{MultiplicityPersist._max}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMax()))
                            .must(() -> !this.isNull(item.getMin()) && (item.getMax() >= item.getMin()))
                            .failOn(MultiplicityPersist._max).failWith(messageSource.getMessage("Validation.LowerThanMin", new Object[]{MultiplicityPersist._min}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
