package org.opencdmp.service.deposit;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commonmodels.models.deposit.DepositPreviewModel;
import org.opencdmp.commonmodels.models.deposit.PreviewFieldOption;
import org.opencdmp.depositbase.repository.DepositClient;

import java.util.List;
import org.opencdmp.depositbase.repository.DepositConfiguration;
import org.opencdmp.depositbase.repository.PlanDepositModel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

public class DepositClientImpl implements DepositClient {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DepositClientImpl.class));

    private final WebClient depositClient;

    public DepositClientImpl(WebClient depositClient) {
        this.depositClient = depositClient;
    }

    @Override
    public String deposit(PlanDepositModel planDepositModel) {
        logger.debug(new MapLogEntry("deposit").And("planDepositModel", planDepositModel));
        return this.depositClient.post().uri("", UriBuilder::build).bodyValue(planDepositModel).exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(String.class)).block();
    }

    @Override
    public DepositPreviewModel preview(PlanDepositModel planDepositModel) {
        logger.debug(new MapLogEntry("preview").And("planDepositModel", planDepositModel));
        return this.depositClient.post().uri("/preview", UriBuilder::build).bodyValue(planDepositModel).exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<DepositPreviewModel>() {})).block();
    }

    @Override
    public List<PreviewFieldOption> searchVocabulary(String fieldCode, String like) {
        logger.debug(new MapLogEntry("searchVocabulary").And("fieldCode", fieldCode));
        return this.depositClient.get().uri("/vocab/" + fieldCode, uriBuilder -> like != null && !like.isBlank() ? uriBuilder.queryParam("like", like).build() : uriBuilder.build()).exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<List<PreviewFieldOption>>() {})).block();
    }

    @Override
    public String authenticate(String code) {
        logger.debug(new MapLogEntry("code"));
        return this.depositClient.get().uri("/authenticate", uriBuilder -> uriBuilder.queryParam("authToken", code).build()).exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(String.class)).block();
    }

    @Override
    public DepositConfiguration getConfiguration() {
        logger.debug(new MapLogEntry("getConfiguration"));
        return this.depositClient.get().uri("/configuration").exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<DepositConfiguration>() {})).block();
    }

    @Override
    public String getLogo() {
        logger.debug(new MapLogEntry("getLogo"));
        return this.depositClient.get().uri("/logo").exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(String.class)).block();
    }
}
