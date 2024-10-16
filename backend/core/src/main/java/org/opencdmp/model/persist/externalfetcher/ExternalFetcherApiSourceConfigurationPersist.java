package org.opencdmp.model.persist.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ExternalFetcherApiSourceConfigurationPersist extends ExternalFetcherBaseSourceConfigurationPersist {

    private String url;

    public static final String _url = "url";

    private ResultsConfigurationPersist results;

    public static final String _results = "results";

    private String paginationPath;

    public static final String _paginationPath = "paginationPath";

    private String contentType;

    public static final String _contentType = "contentType";

    private String firstPage;

    public static final String _firstPage = "firstPage";

    private ExternalFetcherApiHTTPMethodType httpMethod;

    public static final String _httpMethod = "httpMethod";

    private String requestBody;

    private String filterType;

    private AuthenticationConfigurationPersist auth;

    public static final String _auth = "auth";

    private List<QueryConfigPersist> queries;

    public static final String _queries = "queries";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResultsConfigurationPersist getResults() {
        return results;
    }

    public void setResults(ResultsConfigurationPersist results) {
        this.results = results;
    }

    public String getPaginationPath() {
        return paginationPath;
    }

    public void setPaginationPath(String paginationPath) {
        this.paginationPath = paginationPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public ExternalFetcherApiHTTPMethodType getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(ExternalFetcherApiHTTPMethodType httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public AuthenticationConfigurationPersist getAuth() {
        return auth;
    }

    public void setAuth(AuthenticationConfigurationPersist auth) {
        this.auth = auth;
    }

    public List<QueryConfigPersist> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryConfigPersist> queries) {
        this.queries = queries;
    }

    @Component(ExternalFetcherApiSourceConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ExternalFetcherApiSourceConfigurationPersistValidator extends ExternalFetcherBaseSourceConfigurationPersistValidator<ExternalFetcherApiSourceConfigurationPersist> {

        public static final String ValidatorName = "ExternalFetcherApiSourceConfigurationPersistValidator";

        protected ExternalFetcherApiSourceConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource, validatorFactory);
        }

        @Override
        protected Class<ExternalFetcherApiSourceConfigurationPersist> modelClass() {
            return ExternalFetcherApiSourceConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExternalFetcherApiSourceConfigurationPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getUrl()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._url}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPaginationPath()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._paginationPath).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._paginationPath}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getContentType()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._contentType).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._contentType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getHttpMethod()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._httpMethod).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._httpMethod}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isNull(item.getResults()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._results).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._results}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getResults()))
                            .on(ExternalFetcherApiSourceConfigurationPersist._results)
                            .over(item.getResults())
                            .using(() -> this.validatorFactory.validator(ResultsConfigurationPersist.ResultsConfigurationPersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isNull(item.getAuth()))
                            .failOn(ExternalFetcherApiSourceConfigurationPersist._auth).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiSourceConfigurationPersist._auth}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAuth()))
                            .on(ExternalFetcherApiSourceConfigurationPersist._auth)
                            .over(item.getAuth())
                            .using(() -> this.validatorFactory.validator(AuthenticationConfigurationPersist.AuthenticationConfigurationPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getQueries()))
                            .on(ExternalFetcherApiSourceConfigurationPersist._queries)
                            .over(item.getQueries())
                            .using((itm) -> this.validatorFactory.validator(QueryConfigPersist.QueryConfigPersistValidator.class))
            ));
            return specifications;
        }
    }

}
