package org.opencdmp.service.externalfetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import net.minidev.json.JSONArray;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;
import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.types.externalfetcher.StaticOptionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.persist.externalfetcher.StaticOptionPersist;
import org.opencdmp.model.reference.Field;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.service.externalfetcher.config.entities.*;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExternalFetcherServiceImpl implements ExternalFetcherService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ExternalFetcherServiceImpl.class));

    private WebClient webClient;
	private final ConventionService conventionService;
    private final JsonHandlingService jsonHandlingService;
    @Autowired
    public ExternalFetcherServiceImpl(ConventionService conventionService, JsonHandlingService jsonHandlingService) {
	    this.conventionService = conventionService;
        this.jsonHandlingService = jsonHandlingService;
    }

    private WebClient getWebClient()  {
        if (this.webClient == null) {
            this.webClient = WebClient.builder().filters(exchangeFilterFunctions -> {
                exchangeFilterFunctions.add(logRequest());
                exchangeFilterFunctions.add(logResponse());
            }).codecs(clientCodecConfigurer -> {
                clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON));
                clientCodecConfigurer.defaultCodecs().maxInMemorySize(2 * ((int) Math.pow(1024, 3))); //GK: Why here???
            }
            ).clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true))).build();
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
        return this.getExternalData(sources, externalReferenceCriteria, key,isTestMode).getResults().size();
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

        logger.debug(new MapLogEntry("Authentication").And("url", authenticationConfiguration.getAuthUrl()));
        Map<String, Object> response = this.getWebClient().method(method).uri(authenticationConfiguration.getAuthUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(this.parseBodyString(authenticationConfiguration.getAuthRequestBody()))
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
                .filter(x-> x.getDefinition() != null && x.getType() != null && !this.conventionService.isListNullOrEmpty(x.getDefinition().getFields())).toList();

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

                        for (Field field : dependencyReference.getDefinition().getFields()){
                            filterValue = filterValue.replaceAll("\\{" + Matcher.quoteReplacement(field.getCode()) + "}", Matcher.quoteReplacement(field.getValue()));
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
            if (apiSource.getFilterType() != null && "local".equals(apiSource.getFilterType()) && (externalReferenceCriteria.getLike() != null && !externalReferenceCriteria.getLike().isEmpty())) {
                externalDataResult.setResults(externalDataResult.getResults().stream()
                        .filter(r -> r.get(ReferenceEntity.KnownFields.Label).toLowerCase().contains(externalReferenceCriteria.getLike().toLowerCase()))
                        .collect(Collectors.toList()));
            }
            externalDataResult.setResults(externalDataResult.getResults().stream().peek(x -> x.put(ReferenceEntity.KnownFields.SourceLabel, apiSource.getLabel())).peek(x -> x.put(ReferenceEntity.KnownFields.Key, apiSource.getKey())).toList());
            return externalDataResult;
        }
        else {
            return new ExternalDataResult();
        }
    }

    protected ExternalDataResult getExternalDataResults(String urlString, final SourceExternalApiConfiguration<ResultsConfiguration<ResultFieldsMappingConfiguration>, AuthenticationConfiguration, QueryConfig<QueryCaseConfig>, ExternalFetcherApiHeaderConfiguration>  apiSource, String requestBody, String auth) {

        try {
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
            ResponseEntity<String> response;

            if (!this.conventionService.isNullOrEmpty(requestBody) && method == HttpMethod.POST) {
                response = this.getWebClient().method(method).uri(urlString).headers(httpHeaders -> {
                    this.setHeaders(apiSource, httpHeaders, auth);
                }).bodyValue(jsonBody).retrieve().toEntity(String.class).block();
            }else {
                response = this.getWebClient().method(method).uri(urlString).headers(httpHeaders -> {
                    this.setHeaders(apiSource, httpHeaders, auth);
                }).retrieve().toEntity(String.class).block();
            }

            if (response == null || !response.getStatusCode().isSameCodeAs(HttpStatus.OK) || !response.hasBody() || response.getBody()  == null) return null;

            //do here all the parsing
            List<String> responseContentTypeHeader = response.getHeaders().getOrDefault("Content-Type", null);
            String responseContentType =  !this.conventionService.isListNullOrEmpty(responseContentTypeHeader)  && responseContentTypeHeader.getFirst() != null ? responseContentTypeHeader.getFirst() : "";

            if (responseContentType.contains("json") ) {
                DocumentContext jsonContext = JsonPath.parse(response.getBody());
                return this.jsonToExternalDataResult(jsonContext, apiSource.getResults());
            } else {
                throw new MyApplicationException("Unsupported response type" + responseContentType);
            }

        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }

        return null;
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

    private ExternalDataResult jsonToExternalDataResult(DocumentContext jsonContext, ResultsConfiguration<ResultFieldsMappingConfiguration> resultsConfigurationEntity) {
        ExternalDataResult result = new ExternalDataResult();
        if (this.conventionService.isNullOrEmpty(resultsConfigurationEntity.getResultsArrayPath())) return new ExternalDataResult();
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
