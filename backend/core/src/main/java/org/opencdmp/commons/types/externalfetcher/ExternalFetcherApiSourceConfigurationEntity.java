package org.opencdmp.commons.types.externalfetcher;


import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import org.opencdmp.service.externalfetcher.config.entities.ExternalFetcherApiHeaderConfiguration;
import org.opencdmp.service.externalfetcher.config.entities.SourceExternalApiConfiguration;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;
public class ExternalFetcherApiSourceConfigurationEntity extends ExternalFetcherBaseSourceConfigurationEntity implements SourceExternalApiConfiguration<ResultsConfigurationEntity, AuthenticationConfigurationEntity, QueryConfigEntity, ExternalFetcherApiHeaderConfigurationEntity> {

    private String url;
    private ResultsConfigurationEntity results;
    private String paginationPath;
    private String contentType;
    private String firstPage;
    private ExternalFetcherApiHTTPMethodType httpMethod;
    private String requestBody = "";
    private String filterType = "remote";
    private AuthenticationConfigurationEntity auth;

    private List<ExternalFetcherApiHeaderConfigurationEntity> headers;
    private List<QueryConfigEntity> queries;

    public String getUrl() {
        return url;
    }
    @XmlElement(name = "url")
    public void setUrl(String url) {
        this.url = url;
    }

    public ResultsConfigurationEntity getResults() {
        return results;
    }
    @XmlElement(name = "results")
    public void setResults(ResultsConfigurationEntity results) {
        this.results = results;
    }

    public String getPaginationPath() {
        return paginationPath;
    }
    @XmlElement(name = "paginationPath")
    public void setPaginationPath(String paginationPath) {
        this.paginationPath = paginationPath;
    }


    public String getContentType() {
        return contentType;
    }
    @XmlElement(name = "contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFirstPage() {
        return firstPage;
    }
    @XmlElement(name = "firstPage")
    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }
    
    public ExternalFetcherApiHTTPMethodType getHttpMethod() {
        return httpMethod;
    }
    @XmlElement(name = "requestHttpMethod")
    public void setHttpMethod(ExternalFetcherApiHTTPMethodType httpMethod) {
        this.httpMethod = httpMethod != null ? httpMethod : ExternalFetcherApiHTTPMethodType.GET;
    }
	public String getRequestBody() {
		return requestBody;
	}
	@XmlElement(name = "requestBody")
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody != null ? requestBody : "";
	}
    public String getFilterType() {
        return filterType;
    }
    @XmlElement(name = "filterType")
    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public List<QueryConfigEntity> getQueries() {
        return queries;
    }

    @XmlElementWrapper
    @XmlElement(name = "query")
    public void setQueries(List<QueryConfigEntity> queries) {
        this.queries = queries;
    }

    public AuthenticationConfigurationEntity getAuth() {
        return auth;
    }

    @XmlElement(name="authentication")
    public void setAuth(AuthenticationConfigurationEntity auth) {
        this.auth = auth;
    }

    public List<ExternalFetcherApiHeaderConfigurationEntity> getHeaders() {
        return headers;
    }

    @XmlElementWrapper
    @XmlElement(name = "headers")
    public void setHeaders(List<ExternalFetcherApiHeaderConfigurationEntity> headers) {
        this.headers = headers;
    }
}
