package org.opencdmp.query.lookup.accounting;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.accounting.AccountingAggregateType;
import org.opencdmp.commons.enums.accounting.AccountingDataRangeType;
import org.opencdmp.commons.enums.accounting.AccountingMeasureType;
import org.opencdmp.commons.enums.accounting.AccountingValueType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;


public class AccountingInfoLookup {

    private List<String> serviceCodes;
    public static final String _serviceCodes = "serviceCodes";

    private List<String> userCodes;
    public static final String _userCodes = "userCodes";

    private List<String> resourceCodes;
    public static final String _resourceCodes = "resourceCodes";

    private List<String> actionCodes;
    public static final String _actionCodes = "actionCodes";

    private Instant from;
    public static final String _from = "from";

    private Instant to;
    public static final String _to = "to";

    private List<AccountingValueType> types;
    public static final String _types = "types";

    private List<AccountingAggregateType> aggregateTypes;
    public static final String _aggregateTypes = "aggregateTypes";

    private AccountingDataRangeType dateRangeType;
    public static final String _dateRangeType = "dateRangeType";

    private AccountingMeasureType measure;
    public static final String _measure = "measure";

    private FieldSet groupingFields;
    public static final String _groupingFields = "groupingFields";

    private FieldSet project;
    public static final String _project = "project";

    public List<String> getServiceCodes() {
        return serviceCodes;
    }

    public void setServiceCodes(List<String> serviceCodes) {
        this.serviceCodes = serviceCodes;
    }

    public List<String> getUserCodes() {
        return userCodes;
    }

    public void setUserCodes(List<String> userCodes) {
        this.userCodes = userCodes;
    }

    public List<String> getResourceCodes() {
        return resourceCodes;
    }

    public void setResourceCodes(List<String> resourceCodes) {
        this.resourceCodes = resourceCodes;
    }

    public List<String> getActionCodes() {
        return actionCodes;
    }

    public void setActionCodes(List<String> actionCodes) {
        this.actionCodes = actionCodes;
    }

    public Instant getFrom() {
        return this.from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return this.to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public List<AccountingValueType> getTypes() {
        return this.types;
    }

    public void setTypes(List<AccountingValueType> types) {
        this.types = types;
    }

    public List<AccountingAggregateType> getAggregateTypes() {
        return this.aggregateTypes;
    }

    public void setAggregateTypes(List<AccountingAggregateType> aggregateTypes) {
        this.aggregateTypes = aggregateTypes;
    }

    public AccountingDataRangeType getDateRangeType() {
        return this.dateRangeType;
    }

    public void setDateRangeType(AccountingDataRangeType dateRangeType) {
        this.dateRangeType = dateRangeType;
    }

    public AccountingMeasureType getMeasure() {
        return this.measure;
    }

    public void setMeasure(AccountingMeasureType measure) {
        this.measure = measure;
    }

    public FieldSet getGroupingFields() {
        return this.groupingFields;
    }

    public void setGroupingFields(FieldSet groupingFields) {
        this.groupingFields = groupingFields;
    }

    public FieldSet getProject() {
        return project;
    }

    public void setProject(FieldSet project) {
        this.project = project;
    }

    @Component(AccountingInfoLookup.AccountingInfoLookupValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AccountingInfoLookupValidator extends BaseValidator<AccountingInfoLookup> {

        public static final String ValidatorName = "AccountingInfoLookupValidator";

        private final MessageSource messageSource;

        protected AccountingInfoLookupValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<AccountingInfoLookup> modelClass() {
            return AccountingInfoLookup.class;
        }

        @Override
        protected List<Specification> specifications(AccountingInfoLookup item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getMeasure()))
                            .failOn(AccountingInfoLookup._measure).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._measure}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDateRangeType()))
                            .failOn(AccountingInfoLookup._dateRangeType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._dateRangeType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> !this.isNull(item.getDateRangeType()) && item.getDateRangeType().equals(AccountingDataRangeType.Custom))
                            .must(() -> !this.isNull(item.getFrom()))
                            .failOn(AccountingInfoLookup._from).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._from}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> !this.isNull(item.getDateRangeType()) && item.getDateRangeType().equals(AccountingDataRangeType.Custom))
                            .must(() -> !this.isNull(item.getTo()))
                            .failOn(AccountingInfoLookup._to).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._to}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getAggregateTypes()))
                            .failOn(AccountingInfoLookup._aggregateTypes).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._aggregateTypes}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getGroupingFields()))
                            .failOn(AccountingInfoLookup._groupingFields).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccountingInfoLookup._groupingFields}, LocaleContextHolder.getLocale()))
                    );
        }
    }
}
