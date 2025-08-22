package org.opencdmp.integrationevent.outbox.indicator;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.types.indicator.IndicatorFieldBaseType;
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
import java.util.UUID;

public class IndicatorField {

    private UUID id;

    private String code;

    private String name;

    private String label;

    private String description;

    @JsonProperty(value="basetype")
    private IndicatorFieldBaseType basetype;
    public static final String _baseType = "basetype";

    @JsonProperty(value="typesemantics")
    private String typeSemantics;

    @JsonProperty("typeid")
    private String typeId;

    @JsonProperty("subfieldof")
    private String subfieldOf;

    @JsonProperty("valuefield")
    private String valueField;

    @JsonProperty("useas")
    private String useAs;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IndicatorFieldBaseType getBasetype() {
        return basetype;
    }

    public void setBasetype(IndicatorFieldBaseType basetype) {
        this.basetype = basetype;
    }

    public String getTypeSemantics() {
        return typeSemantics;
    }

    public void setTypeSemantics(String typeSemantics) {
        this.typeSemantics = typeSemantics;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getSubfieldOf() {
        return subfieldOf;
    }

    public void setSubfieldOf(String subfieldOf) {
        this.subfieldOf = subfieldOf;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getUseAs() {
        return useAs;
    }

    public void setUseAs(String useAs) {
        this.useAs = useAs;
    }

    @Component(IndicatorFieldValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class IndicatorFieldValidator extends BaseValidator<IndicatorField> {

        public static final String ValidatorName = "IndicatorFieldValidator";

        private final MessageSource messageSource;


        protected IndicatorFieldValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<IndicatorField> modelClass() {
            return IndicatorField.class;
        }

        @Override
        protected List<Specification> specifications(IndicatorField item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getBasetype()))
                            .failOn(IndicatorField._baseType).failWith(messageSource.getMessage("Validation_Required", new Object[]{IndicatorField._baseType}, LocaleContextHolder.getLocale()))
                    );
        }
    }
}
