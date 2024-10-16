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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionPrefillingRequestData {

    private HashMap<String, String> data;
    public static final String _data = "data";

    private String id;
    public static final String _id = "id";

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Component(DescriptionPrefillingRequestDataValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionPrefillingRequestDataValidator extends BaseValidator<DescriptionPrefillingRequestData> {

        public static final String ValidatorName = "DescriptionPrefillingRequestDataValidator";

        private final MessageSource messageSource;


        protected DescriptionPrefillingRequestDataValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionPrefillingRequestData> modelClass() {
            return DescriptionPrefillingRequestData.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionPrefillingRequestData item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(DescriptionPrefillingRequestData._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionPrefillingRequestData._id}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
