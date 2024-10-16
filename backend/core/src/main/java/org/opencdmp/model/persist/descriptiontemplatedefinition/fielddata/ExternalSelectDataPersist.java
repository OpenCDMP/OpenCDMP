package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
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

public class ExternalSelectDataPersist extends BaseFieldDataPersist {

    private Boolean multipleSelect;

    public static final String _multipleSelect = "multipleSelect";

    private List<ExternalSelectSourcePersist> sources;

    public final static String _sources = "sources";

    public List<ExternalSelectSourcePersist> getSources() {
        return sources;
    }

    public void setSources(List<ExternalSelectSourcePersist> sources) {
        this.sources = sources;
    }

    public Boolean getMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    @Component(AutoCompleteDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AutoCompleteDataPersistValidator extends BaseFieldDataPersistValidator<ExternalSelectDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.AutoCompleteDataPersistValidator";

        private final ValidatorFactory validatorFactory;

        protected AutoCompleteDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ExternalSelectDataPersist> modelClass() {
            return ExternalSelectDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExternalSelectDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getMultipleSelect()))
                            .failOn(ExternalSelectDataPersist._multipleSelect).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectDataPersist._multipleSelect}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isNull(item.getSources()))
                            .on(ExternalSelectDataPersist._sources)
                            .over(item.getSources())
                            .using((itm) -> this.validatorFactory.validator(ExternalSelectSourcePersist.ExternalSelectSourcePersistValidator.class))
            ));
            return specifications;
        }
    }

    public static class ExternalSelectSourcePersist {
        private String url;
        public final static String _url = "url";
        private String method;
        public final static String _method = "method";
        private String optionsRoot;
        public final static String _optionsRoot = "optionsRoot";
        private ExternalSelectSourceBindingPersist sourceBinding;
        public final static String _sourceBinding = "sourceBinding";
        private Boolean hasAuth;
        public final static String _hasAuth = "hasAuth";
        private ExternalSelectAuthDataPersist auth;
        public final static String _auth = "auth";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getOptionsRoot() {
            return optionsRoot;
        }

        public void setOptionsRoot(String optionsRoot) {
            this.optionsRoot = optionsRoot;
        }

        public ExternalSelectSourceBindingPersist getSourceBinding() {
            return sourceBinding;
        }

        public void setSourceBinding(ExternalSelectSourceBindingPersist sourceBinding) {
            this.sourceBinding = sourceBinding;
        }

        public Boolean getHasAuth() {
            return hasAuth;
        }

        public void setHasAuth(Boolean hasAuth) {
            this.hasAuth = hasAuth;
        }

        public ExternalSelectAuthDataPersist getAuth() {
            return auth;
        }

        public void setAuth(ExternalSelectAuthDataPersist auth) {
            this.auth = auth;
        }

        @Component(ExternalSelectSourcePersist.ExternalSelectSourcePersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class ExternalSelectSourcePersistValidator extends BaseValidator<ExternalSelectSourcePersist> {

            public static final String ValidatorName = "DescriptionTemplate.ExternalSelectSourcePersistValidator";

            private final ValidatorFactory validatorFactory;
            private final MessageSource messageSource;

            protected ExternalSelectSourcePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
                super(conventionService, errors);
                this.validatorFactory = validatorFactory;
                this.messageSource = messageSource;
            }

            @Override
            protected Class<ExternalSelectSourcePersist> modelClass() {
                return ExternalSelectSourcePersist.class;
            }

            @Override
            protected List<Specification> specifications(ExternalSelectSourcePersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isEmpty(item.getMethod()))
                                .failOn(ExternalSelectSourcePersist._method).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectSourcePersist._method}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getUrl()))
                                .failOn(ExternalSelectSourcePersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectSourcePersist._url}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .iff(() -> !this.isNull(item.getHasAuth()) && item.getHasAuth())
                                .must(() -> !this.isNull(item.getAuth()))
                                .failOn(ExternalSelectSourcePersist._auth).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectSourcePersist._auth}, LocaleContextHolder.getLocale())),
                        this.refSpec()
                                .iff(() -> !this.isNull(item.getSourceBinding()))
                                .on(ExternalSelectSourcePersist._sourceBinding)
                                .over(item.getSourceBinding())
                                .using(() -> this.validatorFactory.validator(ExternalSelectSourceBindingPersist.ExternalSelectSourceBindingPersistValidator.class)),
                        this.refSpec()
                                .iff(() -> !this.isNull(item.getHasAuth()) && item.getHasAuth() && !this.isNull(item.getAuth()))
                                .on(ExternalSelectSourcePersist._auth)
                                .over(item.getAuth())
                                .using(() -> this.validatorFactory.validator(ExternalSelectAuthDataPersist.ExternalSelectAuthDataPersistValidator.class))
                );
            }
        }
    }

    public static class ExternalSelectSourceBindingPersist {
        private String label;
        public final static String _label = "label";
        private String value;
        public final static String _value = "value";
        private String source;
        public final static String _source = "source";

        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }

        public String getSource() {
            return source;
        }
        public void setSource(String source) {
            this.source = source;
        }

        @Component(ExternalSelectSourceBindingPersist.ExternalSelectSourceBindingPersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class ExternalSelectSourceBindingPersistValidator extends BaseValidator<ExternalSelectSourceBindingPersist> {

            public static final String ValidatorName = "DescriptionTemplate.ExternalSelectSourceBindingPersistValidator";

            private final MessageSource messageSource;

            protected ExternalSelectSourceBindingPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
                super(conventionService, errors);
                this.messageSource = messageSource;
            }

            @Override
            protected Class<ExternalSelectSourceBindingPersist> modelClass() {
                return ExternalSelectSourceBindingPersist.class;
            }

            @Override
            protected List<Specification> specifications(ExternalSelectSourceBindingPersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isEmpty(item.getLabel()))
                                .failOn(ExternalSelectSourceBindingPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectSourceBindingPersist._label}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getValue()))
                                .failOn(ExternalSelectSourceBindingPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectSourceBindingPersist._value}, LocaleContextHolder.getLocale()))
                );
            }
        }
    }

    public static class ExternalSelectAuthDataPersist {
        private String url;
        public final static String _url = "url";
        private String method;
        public final static String _method = "method";
        private String body;
        public final static String _body = "body";
        private String path;
        public final static String _path = "path";
        private String type;
        public final static String _type = "type";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Component(ExternalSelectAuthDataPersist.ExternalSelectAuthDataPersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class ExternalSelectAuthDataPersistValidator extends BaseValidator<ExternalSelectAuthDataPersist> {

            public static final String ValidatorName = "DescriptionTemplate.ExternalSelectAuthDataPersistValidator";

            private final MessageSource messageSource;

            protected ExternalSelectAuthDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
                super(conventionService, errors);
                this.messageSource = messageSource;
            }

            @Override
            protected Class<ExternalSelectAuthDataPersist> modelClass() {
                return ExternalSelectAuthDataPersist.class;
            }

            @Override
            protected List<Specification> specifications(ExternalSelectAuthDataPersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isEmpty(item.getUrl()))
                                .failOn(ExternalSelectAuthDataPersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectAuthDataPersist._url}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getMethod()))
                                .failOn(ExternalSelectAuthDataPersist._method).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectAuthDataPersist._method}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getType()))
                                .failOn(ExternalSelectAuthDataPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalSelectAuthDataPersist._type}, LocaleContextHolder.getLocale()))
                );
            }
        }
    }
   
}

