package org.opencdmp.service.accounting;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.model.accounting.AccountingAggregateResults;
import org.opencdmp.query.lookup.accounting.AccountingInfoLookup;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class AccountingClientImpl implements AccountingClient {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AccountingClientImpl.class));

    private final WebClient accountingClient;

    public AccountingClientImpl(WebClient accountingClient) {
        this.accountingClient = accountingClient;
    }

    @Override
    public AccountingAggregateResults calculate(AccountingInfoLookup lookup) throws Exception {
        logger.debug(new MapLogEntry("calculate").And("lookup", lookup));
        return this.accountingClient.post().uri("/accounting/calculate", uriBuilder -> uriBuilder.build()).bodyValue(lookup).exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(AccountingAggregateResults.class)).block();

    }
}
