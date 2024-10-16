package org.opencdmp.model.persist;


import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TagEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TagPersist {

    private UUID id;

    public final static String _id = "id";

    private String label;

    public final static String _label = "label";

    private String hash;

    public final static String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(TagPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class TagPersistValidator extends BaseValidator<TagPersist> {
        public static final String ValidatorName = "TagPersistValidator";
        
        private final MessageSource messageSource;

	    public TagPersistValidator(MessageSource messageSource, ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
		    this.messageSource = messageSource;
	    }

        @Override
        protected Class<TagPersist> modelClass() {
            return TagPersist.class;
        }

        @Override
        protected List<Specification> specifications(TagPersist item) {
            return Arrays.asList(
                    this.spec()
                        .iff(() -> this.isValidGuid(item.getId()))
                        .must(() -> this.isValidHash(item.getHash()))
                        .failOn(TagPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{TagPersist._hash}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isValidGuid(item.getId()))
                        .must(() -> !this.isValidHash(item.getHash()))
                        .failOn(TagPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .must(() -> !this.isEmpty(item.getLabel()))
                        .failOn(TagPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{TagPersist._label}, LocaleContextHolder.getLocale())),
		            this.spec()
                        .iff(() -> !this.isEmpty(item.getLabel()))
                        .must(() -> this.lessEqualLength(item.getLabel(), TagEntity._labelLength))
                        .failOn(TagPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{TagPersist._label}, LocaleContextHolder.getLocale()))
            );
        }
    }
    
}

