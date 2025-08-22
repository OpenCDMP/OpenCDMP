package org.opencdmp.model.persist.planblueprintdefinition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "category",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SystemFieldPersist.class, name = "0"),
        @JsonSubTypes.Type(value = ExtraFieldPersist.class, name = "1"),
        @JsonSubTypes.Type(value = ReferenceTypeFieldPersist.class, name = "2"),
        @JsonSubTypes.Type(value = UploadFieldPersist.class, name = "3")
})
public abstract class FieldPersist {

    private UUID id;

    public static final String _id = "id";

    private PlanBlueprintFieldCategory category;

    public static final String _category = "category";

    private String label;

    public static final String _label = "label";

    private String placeholder;

    public static final String _placeholder = "placeholder";

    private String description;

    private List<String> semantics;
    public final static String _semantics  = "semantics";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";

    private Boolean required;

    public static final String _required = "required";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlanBlueprintFieldCategory getCategory() {
        return this.category;
    }

    public void setCategory(PlanBlueprintFieldCategory category) {
        this.category = category;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<String> getSemantics() {
        return this.semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Boolean getRequired() {
        return this.required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public abstract static class BaseFieldPersistValidator<T extends FieldPersist> extends BaseValidator<T> {

        protected final MessageSource messageSource;

        protected BaseFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        protected List<Specification> getBaseSpecifications(T item) {
            List<Specification> specifications = new ArrayList<>();
            specifications.addAll( Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(FieldPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getCategory()))
                            .failOn(FieldPersist._category).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._category}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(FieldPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRequired()))
                            .failOn(FieldPersist._required).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._required}, LocaleContextHolder.getLocale()))
            ));
            return specifications;
        }

    }

}
