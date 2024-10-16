package org.opencdmp.model.externalfetcher;


import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import java.util.List;

public class ExternalFetcherApiSourceConfiguration extends ExternalFetcherBaseSourceConfiguration {

    public final static String _url = "url";

    private String url;

    public final static String _results = "results";
    private ResultsConfiguration results;

    public final static String _paginationPath = "paginationPath";
    private String paginationPath;

    public final static String _contentType = "contentType";
    private String contentType;

    public final static String _firstPage = "firstPage";
    private String firstPage;

    public final static String _httpMethod = "httpMethod";
    private ExternalFetcherApiHTTPMethodType httpMethod;

    public final static String _requestBody = "requestBody";
    private String requestBody = "";

    public final static String _filterType = "filterType";
    private String filterType = "remote";

    public final static String _auth = "auth";
    private AuthenticationConfiguration auth;

    public final static String _queries = "queries";
    private List<QueryConfig> queries;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResultsConfiguration getResults() {
        return results;
    }

    public void setResults(ResultsConfiguration results) {
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

    public AuthenticationConfiguration getAuth() {
        return auth;
    }

    public void setAuth(AuthenticationConfiguration auth) {
        this.auth = auth;
    }

    public List<QueryConfig> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryConfig> queries) {
        this.queries = queries;
    }
}
