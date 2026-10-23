package org.opencdmp.service.externalfetcher;

import org.springframework.http.codec.json.JacksonJsonDecoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.minidev.json.JSONArray;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;
import org.opencdmp.commons.enums.ExternalFetcherApiResponseFormatType;
import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.types.externalfetcher.AuthenticationConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiSourceConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherBaseSourceConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.StaticOptionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.persist.externalfetcher.AuthenticationConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherApiSourceConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.StaticOptionPersist;
import org.opencdmp.model.reference.Field;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.externalfetcher.config.entities.*;
import org.opencdmp.service.tenant.TenantProperties;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.opencdmp.service.externalfetcher.models.ExternalFetcherTestResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.json.JsonMapper;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExternalFetcherServiceImpl implements ExternalFetcherService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ExternalFetcherServiceImpl.class));

    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
    private static final Pattern AT_PLACEHOLDER_PATTERN = Pattern.compile("@\\{\\{(.*?)}}");
    private static final String FIRST_SUFFIX = ".first()";
    // Matches "name", "./name", "name/@attr", "./name/@attr", optionally followed by "/text()"
    private static final Pattern SIMPLE_CHILD_PATTERN = Pattern.compile("^(?:\\./)?([A-Za-z_][\\w.-]*)(?:/@([\\w-]+)|/text\\(\\))?$");
    // Matches "@attr", "./@attr"
    private static final Pattern SIMPLE_SELF_ATTR_PATTERN = Pattern.compile("^(?:\\./)?@([\\w-]+)$");
    // Matches absolute "/list/repository" (no predicates, no functions)
    private static final Pattern SIMPLE_ABSOLUTE_PATTERN = Pattern.compile("^/([A-Za-z_][\\w.-]*)(?:/([A-Za-z_][\\w.-]*))*$");
    // Matches "//name" — descendant-or-self of root, single element name
    private static final Pattern SIMPLE_DESCENDANT_PATTERN = Pattern.compile("^//([A-Za-z_][\\w.-]*)$");

    private WebClient webClient;
	private final ConventionService conventionService;
    private final JsonHandlingService jsonHandlingService;
    private final ExternalFetcherProperties externalFetcherProperties;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;

    @Autowired
    public ExternalFetcherServiceImpl(ConventionService conventionService, JsonHandlingService jsonHandlingService, ExternalFetcherProperties externalFetcherProperties, EncryptionService encryptionService, TenantProperties tenantProperties) {
	    this.conventionService = conventionService;
        this.jsonHandlingService = jsonHandlingService;
        this.externalFetcherProperties = externalFetcherProperties;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
    }

    private WebClient getWebClient()  {
        if (this.webClient == null) {
            HttpClient httpClient = HttpClient.create().followRedirect(true);
            if (this.externalFetcherProperties.isDisableSSLCertificateValidation()) {
                httpClient = httpClient.secure(sslContextSpec -> {
                    try {
                        sslContextSpec.sslContext(
                                SslContextBuilder.forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Error setting up insecure SSL context for external api fetcher web client", e);
                    }
                });
            }

            this.webClient = WebClient.builder().filters(exchangeFilterFunctions -> {
                exchangeFilterFunctions.add(logRequest());
                exchangeFilterFunctions.add(logResponse());
            }).codecs(clientCodecConfigurer -> {
                clientCodecConfigurer.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(JsonMapper.builder().build()));
                clientCodecConfigurer.defaultCodecs().maxInMemorySize(Math.toIntExact((long) this.externalFetcherProperties.getMaxInMemoryMb() * 1024 * 1024));
            }
            ).clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        }
        return this.webClient;
    }



    @Override
    public ExternalDataResult getExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key, boolean isTestMode)  {
        List<SourceBaseConfiguration> apiSourcesToUse =  sources;
        if (!this.conventionService.isNullOrEmpty(key)){
            apiSourcesToUse = sources.stream().filter(x-> x.getKey().equals(key)).collect(Collectors.toList());
        }
        if (this.conventionService.isListNullOrEmpty(apiSourcesToUse)) return new ExternalDataResult();

        Comparator<SourceBaseConfiguration> comparator = Comparator.comparing(SourceBaseConfiguration::getOrdinal, Comparator.nullsLast(Comparator.naturalOrder()));
        apiSourcesToUse.sort(comparator);

        return this.queryExternalData(apiSourcesToUse, externalReferenceCriteria,isTestMode);
    }

    @Override
    public Integer countExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key, boolean  isTestMode) {
        ExternalDataResult fetched = this.getExternalData(sources, externalReferenceCriteria, key, isTestMode);
        if (fetched.getTotal() != null) return fetched.getTotal();
        return fetched.getResults().size();
    }

    private static final int TEST_BODY_MAX_LENGTH = 200_000;
    private static final int TEST_ITEMS_MAX = 10;

    @Override
    public ExternalFetcherTestResult testSource(SourceBaseConfiguration source, ExternalReferenceCriteria externalReferenceCriteria) {
        ExternalFetcherTestResult testResult = new ExternalFetcherTestResult();
        if (source.getType() != null && source.getType().equals(ExternalFetcherSourceType.STATIC)) {
            ExternalDataResult staticData = this.queryStaticData((SourceStaticOptionConfiguration) source, externalReferenceCriteria, true);
            testResult.setResults(staticData.getResults());
            testResult.setTotal(staticData.getResults().size());
            return testResult;
        }

        SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration> apiSource = (SourceExternalApiConfiguration) source;
        long started = System.currentTimeMillis();
        try {
            String auth = null;
            if (apiSource.getAuth() != null && apiSource.getAuth().getEnabled() != null && apiSource.getAuth().getEnabled()) {
                try {
                    auth = this.buildAuthentication(apiSource.getAuth());
                } catch (Exception e) {
                    testResult.setError("Authentication: " + e.getMessage());
                    return testResult;
                }
            }

            String replacedPath = this.replaceLookupFields(apiSource.getUrl(), apiSource, externalReferenceCriteria);
            String replacedBody = this.replaceLookupFields(apiSource.getRequestBody(), apiSource, externalReferenceCriteria);
            testResult.setUrl(replacedPath);

            ResponseEntity<String> response;
            try {
                response = this.exchange(replacedPath, apiSource, replacedBody, auth);
            } catch (WebClientResponseException e) {
                testResult.setStatus(e.getStatusCode().value());
                this.setTestBody(testResult, e.getResponseBodyAsString());
                return testResult;
            }
            if (response == null) return testResult;
            testResult.setStatus(response.getStatusCode().value());
            this.setTestBody(testResult, response.getBody());
            if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK) || response.getBody() == null) return testResult;

            // the extracted items are read on their own, so they show even before any field is mapped
            if (apiSource.getResponseFormat() != ExternalFetcherApiResponseFormatType.XML && !this.conventionService.isNullOrEmpty(apiSource.getResults().getResultsArrayPath())) {
                Object jsonData = JsonPath.parse(response.getBody()).read(apiSource.getResults().getResultsArrayPath());
                List<Map<String, Object>> items = jsonData instanceof List ? (List<Map<String, Object>>) jsonData : Collections.singletonList((Map<String, Object>) jsonData);
                testResult.setItems(items.stream().limit(TEST_ITEMS_MAX).toList());
            }

            ExternalDataResult externalDataResult = this.parseResponseBody(response.getBody(), apiSource);
            this.applyLocalFilter(externalDataResult, apiSource, externalReferenceCriteria);
            testResult.setResults(externalDataResult.getResults());
            testResult.setTotal(externalDataResult.getTotal());
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            testResult.setError(e.getMessage());
        } finally {
            testResult.setDurationMs(System.currentTimeMillis() - started);
        }
        return testResult;
    }

    private void setTestBody(ExternalFetcherTestResult testResult, String body) {
        if (body == null) return;
        testResult.setBodyTruncated(body.length() > TEST_BODY_MAX_LENGTH);
        testResult.setBody(body.length() > TEST_BODY_MAX_LENGTH ? body.substring(0, TEST_BODY_MAX_LENGTH) : body);
    }

    @Override
    public String authRequestBodyToStore(AuthenticationConfigurationPersist persist, AuthenticationConfigurationEntity old) {
        // the editor never receives the body, so an empty one keeps the stored value
        if (this.conventionService.isNullOrEmpty(persist.getAuthRequestBody())) return old != null ? old.getAuthRequestBody() : null;
        try {
            return this.encryptionService.encryptAES(persist.getAuthRequestBody(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv());
        } catch (Exception e) {
            throw new MyApplicationException(e.getMessage());
        }
    }

    @Override
    public void useStoredAuthRequestBodies(List<ExternalFetcherBaseSourceConfigurationPersist> sources, List<ExternalFetcherBaseSourceConfigurationEntity> stored) {
        if (this.conventionService.isListNullOrEmpty(sources) || this.conventionService.isListNullOrEmpty(stored)) return;
        for (ExternalFetcherBaseSourceConfigurationPersist source : sources) {
            if (!(source instanceof ExternalFetcherApiSourceConfigurationPersist)) continue;
            AuthenticationConfigurationPersist auth = ((ExternalFetcherApiSourceConfigurationPersist) source).getAuth();
            if (auth == null || !this.conventionService.isNullOrEmpty(auth.getAuthRequestBody())) continue;
            ExternalFetcherBaseSourceConfigurationEntity match = stored.stream().filter(x -> x != null && Objects.equals(x.getKey(), source.getKey())).findFirst().orElse(null);
            if (match instanceof ExternalFetcherApiSourceConfigurationEntity && ((ExternalFetcherApiSourceConfigurationEntity) match).getAuth() != null) {
                auth.setAuthRequestBody(this.decrypt(((ExternalFetcherApiSourceConfigurationEntity) match).getAuth().getAuthRequestBody()));
            }
        }
    }

    private String decrypt(String value) {
        if (this.conventionService.isNullOrEmpty(value)) return value;
        try {
            return this.encryptionService.decryptAES(value, this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv());
        } catch (Exception e) {
            throw new MyApplicationException(e.getMessage());
        }
    }

    private ExternalDataResult queryExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, boolean isTestMode) {

        ExternalDataResult results = new ExternalDataResult();

	    if (this.conventionService.isListNullOrEmpty(sources)) return new ExternalDataResult();

	    for (SourceBaseConfiguration source : sources) {
		    if (source.getType() == null || source.getType().equals(ExternalFetcherSourceType.API)) {
			    try {
                    SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>,ExternalFetcherApiHeaderConfiguration > apiSource = (SourceExternalApiConfiguration)source;
//                    this.applyFunderToQuery(apiSource, externalReferenceCriteria);

                    String auth = null;
				    if (apiSource.getAuth() != null && apiSource.getAuth().getEnabled() != null && apiSource.getAuth().getEnabled()) {
					    auth = this.buildAuthentication(apiSource.getAuth());
				    }
				    results.addAll(this.queryExternalData(apiSource, externalReferenceCriteria, auth));
			    } catch (Exception e) {
				    logger.error(e.getLocalizedMessage(), e);
			    }
		    } else if (source.getType() != null &&  source.getType().equals(ExternalFetcherSourceType.STATIC)) {
                SourceStaticOptionConfiguration<Static> staticSource = (SourceStaticOptionConfiguration)source;
			    results.addAll(this.queryStaticData(staticSource, externalReferenceCriteria,isTestMode));
		    }
	    }
        return results;
    }
    private ExternalDataResult queryStaticData(SourceStaticOptionConfiguration<Static> staticSource, ExternalReferenceCriteria externalReferenceCriteria, boolean isTestMode){
        ExternalDataResult externalDataResult = new ExternalDataResult();
        externalDataResult.setRawData(new ArrayList<>());
        externalDataResult.setResults(new ArrayList<>());

        for (Static item : staticSource.getItems()){
            if (this.conventionService.isListNullOrEmpty(item.getOptions())) continue;
            Map<String, String> result = new HashMap<>();
            Map<String, Object> rawData = new HashMap<>();

            for (Object object: item.getOptions()) {
                if (isTestMode)  {
                    StaticOptionPersist staticOption = (StaticOptionPersist) object;
                    rawData.put(staticOption.getCode(), staticOption.getValue());
                    result.put(staticOption.getCode(), staticOption.getValue());
                } else {
                    StaticOptionEntity staticOption = (StaticOptionEntity) object;
                    rawData.put(staticOption.getCode(), staticOption.getValue());
                    result.put(staticOption.getCode(), staticOption.getValue());
                }
                result.put(ReferenceEntity.KnownFields.SourceLabel, staticSource.getLabel());
                result.put(ReferenceEntity.KnownFields.Key, staticSource.getKey());

            }
            if (!this.conventionService.isNullOrEmpty(externalReferenceCriteria.getLike())) {
                if ( (result.get(ReferenceEntity.KnownFields.ReferenceId) != null && result.get(ReferenceEntity.KnownFields.ReferenceId).toUpperCase().contains(externalReferenceCriteria.getLike().toUpperCase()))
                        || (result.get(ReferenceEntity.KnownFields.Label) != null && result.get(ReferenceEntity.KnownFields.Label).toUpperCase().contains(externalReferenceCriteria.getLike().toUpperCase()))
                        || (result.get(ReferenceEntity.KnownFields.Description) != null && result.get(ReferenceEntity.KnownFields.Description).toUpperCase().contains(externalReferenceCriteria.getLike().toUpperCase()))) {
                    if (!rawData.isEmpty()) externalDataResult.getRawData().add(rawData);
                    if (!result.isEmpty()) externalDataResult.getResults().add(result);
                }
            } else{
                if (!rawData.isEmpty()) externalDataResult.getRawData().add(rawData);
                if (!result.isEmpty()) externalDataResult.getResults().add(result);
            }
        }

        return externalDataResult;
    }

    private String buildAuthentication(AuthenticationConfiguration authenticationConfiguration) {
        HttpMethod method;
        switch (authenticationConfiguration.getAuthMethod()) {
            case GET -> method = HttpMethod.GET;
            case POST -> method =HttpMethod.POST;
            default -> throw new MyApplicationException("unrecognized type " + authenticationConfiguration.getAuthMethod());
        }

        // a stored source holds the body encrypted; a test sends it as typed
        String body = authenticationConfiguration instanceof AuthenticationConfigurationEntity ? this.decrypt(authenticationConfiguration.getAuthRequestBody()) : authenticationConfiguration.getAuthRequestBody();

        logger.debug(new MapLogEntry("Authentication").And("url", authenticationConfiguration.getAuthUrl()));
        Map<String, Object> response = this.getWebClient().method(method).uri(authenticationConfiguration.getAuthUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(this.parseBodyString(body))
                .exchangeToMono(mono -> mono.statusCode().isError() ?
                        mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}))
                .block();
        if (response == null) throw new MyApplicationException("Authentication " + authenticationConfiguration.getAuthUrl() + " failed");

        return authenticationConfiguration.getType() + " " + response.getOrDefault(authenticationConfiguration.getAuthTokenPath(), null);
    }

    private String replaceLookupFieldQuery(String query, ExternalReferenceCriteria externalReferenceCriteria, List<QueryConfig<QueryCaseConfig>> queryConfigs) {
        String finalQuery = query;
        String likeValue = this.conventionService.isNullOrEmpty(externalReferenceCriteria.getLike()) ? "" : externalReferenceCriteria.getLike();
        List<Reference> referenceList = this.conventionService.isListNullOrEmpty(externalReferenceCriteria.getDependencyReferences()) ? new ArrayList<>() : externalReferenceCriteria.getDependencyReferences().stream()
                .filter(x-> x.getType() != null).toList();

        if (this.conventionService.isListNullOrEmpty(queryConfigs)) return query;

        for (QueryConfig<?> queryConfig : queryConfigs){

            Comparator<QueryCaseConfig> queryCaseConfigcomparator = Comparator.comparing(x->  x.getReferenceTypeId() == null ? 0 : 1); //Reference QueryCaseConfig are more important
            QueryCaseConfig caseConfig = this.conventionService.isListNullOrEmpty(queryConfig.getCases()) ? null : queryConfig.getCases().stream().filter(x ->
		            (this.conventionService.isNullOrEmpty(x.getLikePattern()) || likeValue.matches(x.getLikePattern()))
				            && ((x.getReferenceTypeId() == null && this.conventionService.isNullOrEmpty(x.getReferenceTypeSourceKey())) || referenceList.stream().anyMatch(y -> Objects.equals(y.getType().getId(), x.getReferenceTypeId()) && Objects.equals(y.getSource(), x.getReferenceTypeSourceKey())))
            ).max(queryCaseConfigcomparator).orElse(null);

            String filterValue = queryConfig.getDefaultValue();

            if (caseConfig != null){
                filterValue = caseConfig.getValue();

                if (caseConfig.getReferenceTypeId() != null && !this.conventionService.isNullOrEmpty(caseConfig.getReferenceTypeSourceKey()) ){
                    Reference dependencyReference = referenceList.stream()
                            .filter(x-> Objects.equals(x.getType().getId(), caseConfig.getReferenceTypeId()) && Objects.equals(x.getSource() ,caseConfig.getReferenceTypeSourceKey())).findFirst().orElse(null);
                    if (dependencyReference != null){

                        if (dependencyReference.getDefinition() != null && dependencyReference.getDefinition().getFields() != null) {
                            for (Field field : dependencyReference.getDefinition().getFields()){
                                filterValue = filterValue.replaceAll("\\{" + Matcher.quoteReplacement(field.getCode()) + "}", Matcher.quoteReplacement(field.getValue()));
                            }
                        }
                        filterValue = filterValue.replaceAll("\\{" + Reference._reference + "}", Matcher.quoteReplacement(dependencyReference.getReference()));
                        filterValue = filterValue.replaceAll("\\{" + Reference._label + "}", Matcher.quoteReplacement(dependencyReference.getLabel()));
                        filterValue = filterValue.replaceAll("\\{" + Reference._source + "}", Matcher.quoteReplacement(dependencyReference.getSource()));
                        
                    }
                } else  if (!this.conventionService.isNullOrEmpty(likeValue)) {
                    if (caseConfig.getSeparator() != null) {
                        String[] likes = likeValue.split(caseConfig.getSeparator());
                        for (int i = 0; i < likes.length; i++) {
                            filterValue = filterValue.replaceAll("\\{like" + (i + 1) + "}", Matcher.quoteReplacement(likes[i]));
                        }
                    } else {
                        filterValue = filterValue.replaceAll("\\{like}", Matcher.quoteReplacement(likeValue));
                    }
                } else {
                    filterValue = queryConfig.getDefaultValue() == null ? "" : queryConfig.getDefaultValue();
                }
            }
            finalQuery = finalQuery.replaceAll("\\{" + queryConfig.getName() + "}",  Matcher.quoteReplacement(filterValue));
        }
        
        return finalQuery;
    }

    protected String replaceLookupFields(String path, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration> apiSource, ExternalReferenceCriteria externalReferenceCriteria) {
        if (this.conventionService.isNullOrEmpty(path)) return path;
        String completedPath = path;

        if (!this.conventionService.isListNullOrEmpty(apiSource.getQueries())){
            completedPath = this.replaceLookupFieldQuery(completedPath, externalReferenceCriteria, apiSource.getQueries());
        }

        if (!this.conventionService.isNullOrEmpty(externalReferenceCriteria.getPage()))  completedPath = completedPath.replace("{page}", externalReferenceCriteria.getPage());
        else  if (!this.conventionService.isNullOrEmpty(apiSource.getFirstPage()))  completedPath = completedPath.replace("{page}", apiSource.getFirstPage());
        else completedPath = completedPath.replace("{page}", "1");

        completedPath = completedPath.replace("{pageSize}", !this.conventionService.isNullOrEmpty(externalReferenceCriteria.getPageSize()) ? externalReferenceCriteria.getPageSize() : "50");
        completedPath = completedPath.replace("{host}", !this.conventionService.isNullOrEmpty(externalReferenceCriteria.getHost()) ? externalReferenceCriteria.getHost() : "");
        completedPath = completedPath.replace("{path}", !this.conventionService.isNullOrEmpty(externalReferenceCriteria.getPath()) ? externalReferenceCriteria.getPath() : "");

        return completedPath;
    }

    private ExternalDataResult queryExternalData(final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig> , ExternalFetcherApiHeaderConfiguration> apiSource, ExternalReferenceCriteria externalReferenceCriteria, String auth) throws Exception {
        String replacedPath = this.replaceLookupFields(apiSource.getUrl(), apiSource, externalReferenceCriteria);
        String replacedBody = this.replaceLookupFields(apiSource.getRequestBody(), apiSource, externalReferenceCriteria);

        ExternalDataResult externalDataResult = this.getExternalDataResults(replacedPath, apiSource, replacedBody, auth);
        if(externalDataResult != null) {
            this.applyLocalFilter(externalDataResult, apiSource, externalReferenceCriteria);
            externalDataResult.setResults(externalDataResult.getResults().stream().peek(x -> x.put(ReferenceEntity.KnownFields.SourceLabel, apiSource.getLabel())).peek(x -> x.put(ReferenceEntity.KnownFields.Key, apiSource.getKey())).toList());
            return externalDataResult;
        }
        else {
            return new ExternalDataResult();
        }
    }

    private void applyLocalFilter(ExternalDataResult externalDataResult, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration> apiSource, ExternalReferenceCriteria externalReferenceCriteria) {
        if (apiSource.getFilterType() != null && "local".equals(apiSource.getFilterType()) && (externalReferenceCriteria.getLike() != null && !externalReferenceCriteria.getLike().isEmpty())) {
            externalDataResult.setResults(externalDataResult.getResults().stream()
                    .filter(r -> r.get(ReferenceEntity.KnownFields.Label).toLowerCase().contains(externalReferenceCriteria.getLike().toLowerCase()))
                    .collect(Collectors.toList()));
            externalDataResult.setTotal(null);
        }
    }

    protected ExternalDataResult getExternalDataResults(String urlString, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration>  apiSource, String requestBody, String auth) {

        try {
            ResponseEntity<String> response = this.exchange(urlString, apiSource, requestBody, auth);

            if (response == null || !response.getStatusCode().isSameCodeAs(HttpStatus.OK) || !response.hasBody() || response.getBody()  == null) return null;

            return this.parseResponseBody(response.getBody(), apiSource);
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }

        return null;
    }

    private ResponseEntity<String> exchange(String urlString, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration>  apiSource, String requestBody, String auth) {
        JsonNode jsonBody = new ObjectMapper().readTree(requestBody != null ? requestBody : "");
        HttpMethod method;
        switch (apiSource.getHttpMethod()) {
            case GET -> method = HttpMethod.GET;
            case POST -> method =HttpMethod.POST;
            default -> throw new MyApplicationException("unrecognized type " + apiSource.getHttpMethod());
        }

        logger.debug(new MapLogEntry("Fetch data")
                .And("url", urlString)
                .And("body", jsonBody));

        if (!this.conventionService.isNullOrEmpty(requestBody) && method == HttpMethod.POST) {
            return this.getWebClient().method(method).uri(urlString).headers(httpHeaders -> {
                this.setHeaders(apiSource, httpHeaders, auth);
            }).bodyValue(jsonBody).retrieve().toEntity(String.class).block();
        }
        return this.getWebClient().method(method).uri(urlString).headers(httpHeaders -> {
            this.setHeaders(apiSource, httpHeaders, auth);
        }).retrieve().toEntity(String.class).block();
    }

    private ExternalDataResult parseResponseBody(String body, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration>  apiSource) {
        ExternalFetcherApiResponseFormatType responseFormat = apiSource.getResponseFormat();
        if (responseFormat == null) responseFormat = ExternalFetcherApiResponseFormatType.JSON;

        switch (responseFormat) {
            case JSON: {
                DocumentContext jsonContext = JsonPath.parse(body);
                return this.jsonToExternalDataResult(jsonContext, apiSource.getResults(), apiSource.getPaginationPath());
            }
            case XML: {
                return this.xmlToExternalDataResult(body, apiSource.getResults(), apiSource.getNamespaces(), apiSource.getPaginationPath());
            }
            default:
                throw new MyApplicationException("Unsupported response format " + responseFormat);
        }
    }

    private void setHeaders( final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration> apiSource , HttpHeaders httpHeaders, String auth){
        try {
            if (!this.conventionService.isNullOrEmpty(apiSource.getContentType())) {
                httpHeaders.setContentType(MediaType.valueOf(apiSource.getContentType()));
            }
        } catch (Exception e) {
            logger.error("Wrong header content type input for value: " + apiSource.getContentType());
            throw e;
        }
        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.ACCEPT))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setAccept(Collections.singletonList(MediaType.valueOf(headerValueOptional.get())));
            } catch (Exception e) {
                logger.error("Wrong header accept input for value: " + headerValueOptional.get());
                throw e;
            }
        }

        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.CONNECTION))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setConnection((headerValueOptional.get()));
            } catch (Exception e) {
                logger.error("Wrong header Connection input for value: " + headerValueOptional.get());
                throw e;
            }
        }

        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.ALLOW_HEADER))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setAccessControlAllowHeaders(Collections.singletonList(headerValueOptional.get()));
            } catch (Exception e) {
                logger.error("Wrong header Access Control Header input for value: " + headerValueOptional.get());
                throw e;
            }
        }

        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.ORIGIN))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setOrigin(headerValueOptional.get());
            } catch (Exception e) {
                logger.error("Wrong header Origin input for value: " + headerValueOptional.get());
                throw e;
            }
        }

        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.ALLOW_ORIGIN))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setAccessControlAllowOrigin(headerValueOptional.get());
            } catch (Exception e) {
                logger.error("Wrong header Origin input for value: " + headerValueOptional.get());
                throw e;
            }
        }

        if (!this.conventionService.isListNullOrEmpty(apiSource.getHeaders())) {
            Optional<String> headerValueOptional = apiSource.getHeaders().stream()
                    .filter(x -> x.getKey().equals(ExternalFetcherApiHeaderType.ALLOW_CREDENTIALS))
                    .map(ExternalFetcherApiHeaderConfiguration::getValue)
                    .findFirst();

            try {
                if(headerValueOptional.isPresent() &&!this.conventionService.isNullOrEmpty(headerValueOptional.get()))
                    httpHeaders.setAccessControlAllowOrigin(headerValueOptional.get());
            } catch (Exception e) {
                logger.error("Wrong header Access Control Origin input for value: " + headerValueOptional.get());
                throw e;
            }
        }


        if (auth != null) {
            httpHeaders.set("Authorization", auth);
        }
    }

    private ExternalDataResult jsonToExternalDataResult(DocumentContext jsonContext, ResultsConfiguration<ResultFieldsMappingConfiguration> resultsConfigurationEntity, String paginationPath) {
        ExternalDataResult result = new ExternalDataResult();
        if (this.conventionService.isNullOrEmpty(resultsConfigurationEntity.getResultsArrayPath())) return new ExternalDataResult();
        Integer jsonTotal = readJsonTotal(jsonContext, paginationPath);
        if (jsonTotal != null) result.setTotal(jsonTotal);
        Object jsonData = jsonContext.read(resultsConfigurationEntity.getResultsArrayPath());
        List<Map<String, Object>> rawData = new ArrayList<>();
        if (jsonData instanceof List) {
            rawData = (List<Map<String, Object>>) jsonData;
        }else{
            rawData.add((Map<String, Object>)jsonData);
        }
        result.setRawData(rawData);

        if (this.conventionService.isListNullOrEmpty(rawData) || this.conventionService.isListNullOrEmpty(resultsConfigurationEntity.getFieldsMapping())) return new ExternalDataResult();

        List<Map<String, String>> parsedData = new ArrayList<>();
        for(Object resultItem : result.getRawData()){
            if (resultItem == null) continue;
            Map<String, String> map = new HashMap<>();
            boolean isValid = true;
            for(ResultFieldsMappingConfiguration field : resultsConfigurationEntity.getFieldsMapping()) {
                if (this.conventionService.isNullOrEmpty(field.getResponsePath()) || this.conventionService.isNullOrEmpty(field.getCode())) continue;
                boolean getFirst = field.getResponsePath().endsWith(".first()");
                String responsePath = new String(field.getResponsePath());
                if (getFirst) responsePath = responsePath.substring(0, responsePath.length() - ".first()".length());
                try {
                    if (responsePath.contains("@{{")){
                        String rePattern = "@\\{\\{(.*?)}}";
                        Pattern p = Pattern.compile(rePattern);
                        Matcher m = p.matcher(responsePath);
                        String value = responsePath;
                        while (m.find()) {
                            if (m.groupCount() < 1) continue;
                            String normalizedValue;
                            if (m.group(1).contains(".first()")) {
                                String[] responsePathParts = null;
                                responsePathParts = m.group(1).split(Pattern.quote(".first()"));
                                normalizedValue = normalizeJsonValue(this.buildJsonPathResponseFromParts(responsePathParts, JsonPath.parse(resultItem)), getFirst);
                            } else {
                                Object partValue = JsonPath.parse(resultItem).read(m.group(1));
                                normalizedValue = normalizeJsonValue(partValue, getFirst);
                            }

                            if (normalizedValue != null) value = value.replace("@{{" + m.group(1) + "}}", normalizedValue);
                            else value = value.replace("@{{" + m.group(1) + "}}","");
                        }
                        map.put(field.getCode(), normalizeJsonValue(value, getFirst));
                    } else {
                        String[] responsePathParts = null;
                        if (field.getResponsePath().contains(".first()")) responsePathParts = field.getResponsePath().split(Pattern.quote(".first()"));
                        if (responsePathParts != null) responsePath = this.buildJsonPathResponseFromParts(responsePathParts, JsonPath.parse(resultItem));
                        Object value = JsonPath.parse(resultItem).read(responsePath);
                        map.put(field.getCode(), normalizeJsonValue(value, getFirst));
                    }
                }catch (PathNotFoundException e){
                    logger.debug("Json Path Error: " + e.getMessage() + " on source " + this.jsonHandlingService.toJsonSafe(resultItem));
                    if (ReferenceEntity.KnownFields.ReferenceId.equals(field.getCode())) {
                        isValid = false;
                        break;
                    }
                }
            }
            if (this.conventionService.isNullOrEmpty(map.getOrDefault(ReferenceEntity.KnownFields.ReferenceId, null))){
                logger.warn("Invalid reference on source " + this.jsonHandlingService.toJsonSafe(resultItem));
            }
            if (isValid) parsedData.add(map);
        }

        result.setResults(parsedData);
        return result;
    }

    private String buildJsonPathResponseFromParts(String[] responsePaths, DocumentContext jsonContext) {
        StringBuilder jsonPathResponse = new StringBuilder();

        for (String responsePath: responsePaths) {
            try {
                jsonPathResponse.append(responsePath);
                Object value = jsonContext.read(jsonPathResponse.toString());
                if (value instanceof JSONArray jsonArray) {
                    jsonPathResponse.append("[0]");
                }
            } catch (PathNotFoundException e){
                logger.debug("Json Path Error: " + e.getMessage() + " on source " + this.jsonHandlingService.toJsonSafe(responsePath));
            }
        }

        return jsonPathResponse.toString();
    }

    private ExternalDataResult xmlToExternalDataResult(String body, ResultsConfiguration<ResultFieldsMappingConfiguration> resultsConfiguration, List<? extends XmlNamespaceConfiguration> namespaces, String paginationPath) {
        ExternalDataResult result = new ExternalDataResult();
        if (this.conventionService.isNullOrEmpty(resultsConfiguration.getResultsArrayPath())) return new ExternalDataResult();

        Document document;
        try {
            document = parseXmlSecurely(body);
        } catch (Exception e) {
            logger.error("Could not parse XML response: " + e.getMessage(), e);
            return new ExternalDataResult();
        }

        XPath xpath = XPATH_FACTORY.newXPath();
        boolean hasNamespaces = !this.conventionService.isListNullOrEmpty(namespaces);
        if (hasNamespaces) {
            xpath.setNamespaceContext(buildNamespaceContext(namespaces));
        }

        Integer xmlTotal = readXmlTotal(xpath, document, paginationPath);
        if (xmlTotal != null) result.setTotal(xmlTotal);

        String resultsArrayPath = resultsConfiguration.getResultsArrayPath();
        List<Element> itemElements = null;
        if (!hasNamespaces) {
            itemElements = resolveItemNodesFast(document, resultsArrayPath);
        }

        NodeList itemNodes = null;
        int itemCount;
        if (itemElements != null) {
            itemCount = itemElements.size();
        } else {
            try {
                itemNodes = (NodeList) xpath.compile(resultsArrayPath).evaluate(document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                logger.error("Invalid XPath for resultsArrayPath '" + resultsArrayPath + "': " + e.getMessage(), e);
                return new ExternalDataResult();
            }
            if (itemNodes == null) return new ExternalDataResult();
            itemCount = itemNodes.getLength();
        }
        if (itemCount == 0) return new ExternalDataResult();

        if (this.conventionService.isListNullOrEmpty(resultsConfiguration.getFieldsMapping())) {
            return new ExternalDataResult();
        }

        List<CompiledFieldXPath> compiledFields = compileFieldXPaths(xpath, resultsConfiguration.getFieldsMapping());

        List<Map<String, String>> parsedData = new ArrayList<>(itemCount);

        for (int i = 0; i < itemCount; i++) {
            Node itemNode = itemElements != null ? itemElements.get(i) : itemNodes.item(i);
            if (itemNode == null) continue;
            Element itemElement = itemNode.getNodeType() == Node.ELEMENT_NODE ? (Element) itemNode : null;

            Map<String, String> map = new HashMap<>();
            boolean isValid = true;
            for (CompiledFieldXPath cf : compiledFields) {
                try {
                    String value;
                    switch (cf.mode) {
                        case CHILD_TEXT, CHILD_ATTR, SELF_ATTR -> {
                            value = itemElement != null ? resolveFastField(itemElement, cf) : evaluateFastFallback(xpath, itemNode, cf);
                        }
                        case PLACEHOLDER -> {
                            StringBuilder out = new StringBuilder();
                            for (int s = 0; s < cf.literalSegments.size(); s++) {
                                out.append(cf.literalSegments.get(s));
                                if (s < cf.compiledPlaceholders.size()) {
                                    String partValue = evaluateXPathString(cf.compiledPlaceholders.get(s), itemNode);
                                    if (partValue != null) out.append(partValue);
                                }
                            }
                            value = out.toString();
                        }
                        default -> value = evaluateXPathString(cf.compiledPath, itemNode);
                    }
                    map.put(cf.code, value);
                } catch (XPathExpressionException e) {
                    logger.debug("XPath Error: " + e.getMessage() + " on path " + cf.responsePath);
                    if (ReferenceEntity.KnownFields.ReferenceId.equals(cf.code)) {
                        isValid = false;
                        break;
                    }
                }
            }
            if (this.conventionService.isNullOrEmpty(map.getOrDefault(ReferenceEntity.KnownFields.ReferenceId, null))) {
                logger.warn("Invalid reference on XML source");
                isValid = false;
            }
            if (isValid) parsedData.add(map);
        }

        result.setResults(parsedData);
        return result;
    }

    private static String evaluateFastFallback(XPath xpath, Node itemNode, CompiledFieldXPath cf) throws XPathExpressionException {
        // Reached when item is not an Element (shouldn't normally happen) — fall back to a one-off XPath compile.
        return evaluateXPathString(xpath.compile(cf.responsePath), itemNode);
    }

    private enum FieldMode { CHILD_TEXT, CHILD_ATTR, SELF_ATTR, XPATH, PLACEHOLDER }

    private static final class CompiledFieldXPath {
        final String code;
        final String responsePath;
        final FieldMode mode;
        // CHILD_TEXT/CHILD_ATTR: child element name; SELF_ATTR: null; XPATH/PLACEHOLDER: null
        final String childName;
        // CHILD_ATTR/SELF_ATTR: attribute name
        final String attrName;
        // XPATH fallback
        final XPathExpression compiledPath;
        // PLACEHOLDER mode
        final List<String> literalSegments;
        final List<XPathExpression> compiledPlaceholders;

        CompiledFieldXPath(String code, String responsePath, FieldMode mode, String childName, String attrName,
                           XPathExpression compiledPath, List<String> literalSegments, List<XPathExpression> compiledPlaceholders) {
            this.code = code;
            this.responsePath = responsePath;
            this.mode = mode;
            this.childName = childName;
            this.attrName = attrName;
            this.compiledPath = compiledPath;
            this.literalSegments = literalSegments;
            this.compiledPlaceholders = compiledPlaceholders;
        }
    }

    private List<CompiledFieldXPath> compileFieldXPaths(XPath xpath, List<? extends ResultFieldsMappingConfiguration> fields) {
        List<CompiledFieldXPath> compiled = new ArrayList<>(fields.size());
        for (ResultFieldsMappingConfiguration field : fields) {
            if (this.conventionService.isNullOrEmpty(field.getResponsePath()) || this.conventionService.isNullOrEmpty(field.getCode())) continue;

            String rawPath = field.getResponsePath();
            String responsePath = rawPath.endsWith(FIRST_SUFFIX) ? rawPath.substring(0, rawPath.length() - FIRST_SUFFIX.length()) : rawPath;

            try {
                if (responsePath.contains("@{{")) {
                    Matcher m = AT_PLACEHOLDER_PATTERN.matcher(responsePath);
                    List<String> literals = new ArrayList<>();
                    List<XPathExpression> placeholders = new ArrayList<>();
                    int last = 0;
                    while (m.find()) {
                        literals.add(responsePath.substring(last, m.start()));
                        String inner = m.group(1);
                        String innerStripped = inner.endsWith(FIRST_SUFFIX) ? inner.substring(0, inner.length() - FIRST_SUFFIX.length()) : inner;
                        placeholders.add(xpath.compile(innerStripped));
                        last = m.end();
                    }
                    literals.add(responsePath.substring(last));
                    compiled.add(new CompiledFieldXPath(field.getCode(), responsePath, FieldMode.PLACEHOLDER, null, null, null, literals, placeholders));
                    continue;
                }

                Matcher selfAttr = SIMPLE_SELF_ATTR_PATTERN.matcher(responsePath);
                if (selfAttr.matches()) {
                    compiled.add(new CompiledFieldXPath(field.getCode(), responsePath, FieldMode.SELF_ATTR, null, selfAttr.group(1), null, null, null));
                    continue;
                }

                Matcher simple = SIMPLE_CHILD_PATTERN.matcher(responsePath);
                if (simple.matches()) {
                    String child = simple.group(1);
                    String attr = simple.group(2);
                    FieldMode mode = attr != null ? FieldMode.CHILD_ATTR : FieldMode.CHILD_TEXT;
                    compiled.add(new CompiledFieldXPath(field.getCode(), responsePath, mode, child, attr, null, null, null));
                    continue;
                }

                compiled.add(new CompiledFieldXPath(field.getCode(), responsePath, FieldMode.XPATH, null, null, xpath.compile(responsePath), null, null));
            } catch (XPathExpressionException e) {
                logger.error("Invalid XPath for field '" + field.getCode() + "': " + e.getMessage(), e);
            }
        }
        return compiled;
    }

    private static String resolveFastField(Element item, CompiledFieldXPath cf) {
        switch (cf.mode) {
            case SELF_ATTR -> {
                String v = item.getAttribute(cf.attrName);
                return v.isEmpty() ? null : v;
            }
            case CHILD_TEXT, CHILD_ATTR -> {
                Node child = firstChildElement(item, cf.childName);
                if (child == null) return null;
                if (cf.mode == FieldMode.CHILD_ATTR) {
                    String v = ((Element) child).getAttribute(cf.attrName);
                    return v.isEmpty() ? null : v;
                }
                String text = child.getTextContent();
                if (text == null) return null;
                text = text.trim();
                return text.isEmpty() ? null : text;
            }
            default -> {
                return null;
            }
        }
    }

    private static Element firstChildElement(Element parent, String localName) {
        Node n = parent.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE && localName.equals(n.getNodeName())) {
                return (Element) n;
            }
            n = n.getNextSibling();
        }
        return null;
    }

    private static List<Element> resolveItemNodesFast(Document document, String resultsArrayPath) {
        Element root = document.getDocumentElement();
        if (root == null) return null;

        Matcher descendant = SIMPLE_DESCENDANT_PATTERN.matcher(resultsArrayPath);
        if (descendant.matches()) {
            String name = descendant.group(1);
            List<Element> result = new ArrayList<>();
            collectDescendantsByName(root, name, result);
            return result;
        }

        if (!SIMPLE_ABSOLUTE_PATTERN.matcher(resultsArrayPath).matches()) return null;
        String[] parts = resultsArrayPath.substring(1).split("/");
        if (parts.length == 0) return null;
        if (!parts[0].equals(root.getNodeName())) return null;
        List<Element> current = Collections.singletonList(root);
        for (int i = 1; i < parts.length; i++) {
            String name = parts[i];
            List<Element> next = new ArrayList<>();
            for (Element parent : current) {
                Node n = parent.getFirstChild();
                while (n != null) {
                    if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                        next.add((Element) n);
                    }
                    n = n.getNextSibling();
                }
            }
            current = next;
            if (current.isEmpty()) break;
        }
        return current;
    }

    private static void collectDescendantsByName(Element element, String name, List<Element> out) {
        if (name.equals(element.getNodeName())) {
            out.add(element);
        }
        Node n = element.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                collectDescendantsByName((Element) n, name, out);
            }
            n = n.getNextSibling();
        }
    }

    private static Integer readJsonTotal(DocumentContext jsonContext, String paginationPath) {
        if (paginationPath == null || paginationPath.isBlank()) return null;
        try {
            Object value = jsonContext.read(paginationPath);
            return coerceToInteger(value);
        } catch (Exception e) {
            logger.debug("Could not evaluate paginationPath '" + paginationPath + "': " + e.getMessage());
            return null;
        }
    }

    private static Integer readXmlTotal(XPath xpath, Document document, String paginationPath) {
        if (paginationPath == null || paginationPath.isBlank()) return null;
        try {
            String value = (String) xpath.compile(paginationPath).evaluate(document, XPathConstants.STRING);
            return coerceToInteger(value);
        } catch (Exception e) {
            logger.debug("Could not evaluate paginationPath '" + paginationPath + "': " + e.getMessage());
            return null;
        }
    }

    private static Integer coerceToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof CharSequence cs) {
            String s = cs.toString().trim();
            if (s.isEmpty()) return null;
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                try {
                    return (int) Math.round(Double.parseDouble(s));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static NamespaceContext buildNamespaceContext(List<? extends XmlNamespaceConfiguration> namespaces) {
        Map<String, String> prefixToUri = new HashMap<>();
        Map<String, String> uriToPrefix = new HashMap<>();
        for (XmlNamespaceConfiguration ns : namespaces) {
            if (ns == null || ns.getPrefix() == null || ns.getPrefix().isEmpty() || ns.getUri() == null || ns.getUri().isEmpty()) continue;
            prefixToUri.put(ns.getPrefix(), ns.getUri());
            uriToPrefix.putIfAbsent(ns.getUri(), ns.getPrefix());
        }
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null) throw new IllegalArgumentException("prefix is null");
                if (XMLConstants.XML_NS_PREFIX.equals(prefix)) return XMLConstants.XML_NS_URI;
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                String uri = prefixToUri.get(prefix);
                return uri != null ? uri : XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String uri) {
                if (uri == null) throw new IllegalArgumentException("uri is null");
                if (XMLConstants.XML_NS_URI.equals(uri)) return XMLConstants.XML_NS_PREFIX;
                if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri)) return XMLConstants.XMLNS_ATTRIBUTE;
                return uriToPrefix.get(uri);
            }

            @Override
            public Iterator<String> getPrefixes(String uri) {
                String prefix = getPrefix(uri);
                return prefix != null ? Collections.singletonList(prefix).iterator() : Collections.emptyIterator();
            }
        };
    }

    private static Document parseXmlSecurely(String body) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(body)));
    }

    private static String evaluateXPathString(XPathExpression expression, Node context) throws XPathExpressionException {
        String value = (String) expression.evaluate(context, XPathConstants.STRING);
        if (value == null) return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private static String normalizeJsonValue(Object value, boolean getfirst) {
        if (value instanceof JSONArray jsonArray) {
	        if (getfirst) {
                return jsonArray.getFirst().toString();
            } else {
                return value.toString();
            }
        }
        return value != null ? value.toString() : null;
    }

    private String parseBodyString(String bodyString) {
        String finalBodyString = bodyString;
        if (bodyString.contains("{env:")) {
            int index = bodyString.indexOf("{env: ");
            while (index >= 0) {
                int endIndex = bodyString.indexOf("}", index + 6);
                String envName = bodyString.substring(index + 6, endIndex);
                finalBodyString = finalBodyString.replace("{env: " + envName + "}", System.getenv(envName));
                index = bodyString.indexOf("{env: ", index + 6);
            }
        }
        return finalBodyString;
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.debug(new MapLogEntry("Request").And("method", clientRequest.method().toString()).And("url", clientRequest.url()));
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.mutate().build().bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error(new MapLogEntry("Response").And("method", response.request().getMethod().toString()).And("url", response.request().getURI()).And("status", response.statusCode().toString()).And("body", body));
                            return Mono.just(response);
                        });
            }
            return Mono.just(response);

        });
    }

}
